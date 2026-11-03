package com.azavern.aviplayer.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Player
import com.azavern.aviplayer.data.model.OrientationMode
import com.azavern.aviplayer.data.repository.PlaybackRepository
import com.azavern.aviplayer.data.repository.SettingsRepository
import com.azavern.aviplayer.player.core.PlayerEvent
import com.azavern.aviplayer.player.core.PlayerEventListener
import com.azavern.aviplayer.player.core.PlayerState
import com.azavern.aviplayer.player.capture.VideoFrameCapturer
import com.azavern.aviplayer.player.core.VideoPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import com.azavern.aviplayer.utils.Constants
import com.azavern.aviplayer.utils.FileUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel Player (Blueprint Milestone 1 & Bagian 10A/10F).
 * Satu-satunya penghubung UI dengan [VideoPlayerManager].
 *
 * Siklus hidup: [initPlayer] menyiapkan video dari posisi terakhir
 * yang tersimpan di database (Milestone 2, lewat [PlaybackRepository]);
 * [onStop] (dari PlayerActivity.onStop) menyimpan posisi ke database
 * lalu me-release player. [onStart] menyiapkan ulang dari posisi di
 * state bila player sudah ter-release (mis. kembali dari Home ke
 * Activity yang sama, tanpa proses mati).
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: VideoPlayerManager,
    private val playbackRepository: PlaybackRepository,
    private val settingsRepository: SettingsRepository,
    private val frameCapturer: VideoFrameCapturer
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        // Tema mengikuti SettingsRepository (Milestone 4): PlayerActivity
        // meneruskan themeConfig dari state ini ke AppTheme.
        viewModelScope.launch {
            settingsRepository.getThemeConfig().collect { config ->
                _uiState.update { it.copy(themeConfig = config) }
            }
        }
    }

    /** Diekspos ke VideoSurface lewat PlayerScreen (bukan untuk dimanipulasi UI). */
    val player get() = playerManager.player

    private var positionTicker: Job? = null
    private var initializedPath: String? = null

    fun initPlayer(path: String, id: String, title: String) {
        if (path.isBlank()) {
            _uiState.update {
                it.copy(playerState = PlayerState.Error("Path video tidak diberikan"))
            }
            return
        }
        if (initializedPath == path && !playerManager.isReleased) return
        initializedPath = path
        // ID kosong (mis. dibuka lewat intent manual) → turunkan dari path,
        // supaya resume tetap bekerja dengan kunci yang sama seperti Beranda.
        val effectiveId = id.ifBlank { FileUtils.generateVideoId(path) }
        _uiState.update {
            it.copy(
                videoPath = path,
                videoId = effectiveId,
                videoTitle = title,
                playerState = PlayerState.Loading,
                currentPositionMs = 0L,
                durationMs = 0L
            )
        }
        playerManager.setEventListener(PlayerEventListener { event -> onPlayerEvent(event) })
        viewModelScope.launch {
            val lastPosition = playbackRepository.getLastPosition(effectiveId)
            _uiState.update { it.copy(currentPositionMs = lastPosition) }
            playerManager.prepare(path, startPositionMs = lastPosition)
            playerManager.setSpeed(_uiState.value.playbackSpeed)
            startPositionTicker()
        }
    }

    /** Dipanggil PlayerActivity.onStart: siapkan ulang bila player ter-release di onStop. */
    fun onStart() {
        val state = _uiState.value
        if (playerManager.isReleased && state.videoPath.isNotBlank()) {
            playerManager.setEventListener(PlayerEventListener { event -> onPlayerEvent(event) })
            playerManager.prepare(state.videoPath, startPositionMs = state.currentPositionMs)
            playerManager.setSpeed(state.playbackSpeed)
            startPositionTicker()
        }
    }

    fun play() {
        if (_uiState.value.playerState == PlayerState.Ended) {
            playerManager.seekTo(0L)
        }
        playerManager.play()
    }

    fun pause() {
        playerManager.pause()
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) pause() else play()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
        _uiState.update { it.copy(currentPositionMs = positionMs.coerceAtLeast(0L)) }
    }

    /** Tombol PREVIOUS (Milestone 1): mulai ulang video dari awal. */
    fun onPrevious() {
        seekTo(0L)
        play()
    }

    // ---- Gesture (Milestone 5) ----

    /** Maju satu langkah seek (bawaan 10 detik, Constants.DEFAULT_SEEK_STEP_MS). */
    fun seekForward() {
        val state = _uiState.value
        val target = state.currentPositionMs + Constants.DEFAULT_SEEK_STEP_MS
        seekTo(if (state.durationMs > 0) target.coerceAtMost(state.durationMs) else target)
    }

    /** Mundur satu langkah seek. */
    fun seekBackward() {
        seekTo(_uiState.value.currentPositionMs - Constants.DEFAULT_SEEK_STEP_MS)
    }

    /**
     * Terapkan level kecerahan baru dari gesture (diteruskan
     * PlayerActivity ke window). Ini SATU-SATUNYA jalan kecerahan
     * window diubah aplikasi — tidak ada penerapan otomatis.
     */
    fun adjustBrightness(level: Float) {
        _uiState.update {
            it.copy(
                brightnessLevel = level.coerceIn(0f, 1f),
                brightnessOverrideActive = true
            )
        }
    }

    /** Terapkan level volume baru (diteruskan PlayerActivity ke AudioManager). */
    fun adjustVolume(level: Float) {
        _uiState.update { it.copy(volumeLevel = level.coerceIn(0f, 1f)) }
    }

    /**
     * Tangkap frame video pada posisi putar sekarang dan simpan ke
     * galeri (Milestone 6). Frame mentah dari decoder lewat
     * MediaMetadataRetriever — bukan screenshot. Hasilnya masuk
     * uiState.captureResult untuk ditampilkan sebagai Snackbar.
     */
    fun captureFrame() {
        val state = _uiState.value
        if (state.videoPath.isBlank()) return
        val positionMs = playerManager.getCurrentPosition()
        viewModelScope.launch {
            val captureResult = frameCapturer.captureFrame(state.videoPath, positionMs).fold(
                onSuccess = { bitmap ->
                    frameCapturer.saveFrameToGallery(bitmap, state.videoTitle).fold(
                        onSuccess = { uri -> CaptureResult.Success(uri) },
                        onFailure = { error ->
                            CaptureResult.Error(error.message ?: "Gagal menyimpan frame")
                        }
                    )
                },
                onFailure = { error ->
                    CaptureResult.Error(error.message ?: "Gagal menangkap frame")
                }
            )
            _uiState.update { it.copy(captureResult = captureResult) }
        }
    }

    /** Dipanggil PlayerScreen setelah Snackbar capture ditampilkan. */
    fun onCaptureResultConsumed() {
        _uiState.update { it.copy(captureResult = null) }
    }

    /**
     * Sinkronisasi level awal dari sistem, dipanggil PlayerActivity
     * sebelum konten digambar dan saat kembali ke layar.
     */
    fun syncSystemLevels(brightness: Float, volume: Float) {
        _uiState.update {
            it.copy(
                brightnessLevel = brightness.coerceIn(0f, 1f),
                volumeLevel = volume.coerceIn(0f, 1f)
            )
        }
    }

    /** Pratinjau posisi selama gesture seek — belum seek beneran. */
    fun onSeekPreview(previewMs: Long) {
        val duration = _uiState.value.durationMs
        val coerced = if (duration > 0) {
            previewMs.coerceIn(0L, duration)
        } else {
            previewMs.coerceAtLeast(0L)
        }
        _uiState.update { it.copy(seekPreviewMs = coerced) }
    }

    /** Jari dilepas saat gesture seek: loncat ke posisi pratinjau. */
    fun commitSeekPreview() {
        val preview = _uiState.value.seekPreviewMs ?: return
        _uiState.update { it.copy(seekPreviewMs = null) }
        seekTo(preview)
    }

    /** Gesture seek batal (mis. sentuhan terputus): buang pratinjau. */
    fun clearSeekPreview() {
        if (_uiState.value.seekPreviewMs != null) {
            _uiState.update { it.copy(seekPreviewMs = null) }
        }
    }

    /** Tombol kecepatan: bersiklus 0.5 → 0.75 → 1.0 → 1.25 → 1.5 → 2.0 → 0.5. */
    fun cycleSpeed() {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val currentIndex = speeds.indexOf(_uiState.value.playbackSpeed)
        val next = speeds[(currentIndex + 1) % speeds.size]
        playerManager.setSpeed(next)
        _uiState.update { it.copy(playbackSpeed = next) }
    }

    /** Siklus mode orientasi AUTO → LANDSCAPE → PORTRAIT → AUTO (Bagian 10F). */
    fun cycleOrientationMode() {
        _uiState.update {
            val next = when (it.orientationMode) {
                OrientationMode.AUTO -> OrientationMode.LANDSCAPE
                OrientationMode.LANDSCAPE -> OrientationMode.PORTRAIT
                OrientationMode.PORTRAIT -> OrientationMode.AUTO
            }
            it.copy(orientationMode = next)
        }
        // Penyimpanan ke DataStore (PREF_ORIENTATION_MODE) menyusul bersama
        // SettingsRepository; di Milestone 1 mode hidup di state saja.
    }

    /** Status kunci rotasi HP, dibaca PlayerActivity dari Settings.System. */
    fun setSystemAutoRotate(enabled: Boolean) {
        _uiState.update { it.copy(systemAutoRotate = enabled) }
    }

    /**
     * Dipanggil PlayerActivity.onStop() (Blueprint Bagian 10A):
     * catat posisi terakhir, simpan ke database (Milestone 2),
     * hentikan ticker, release player.
     *
     * Penyimpanan dibungkus [NonCancellable]: onStop bisa diikuti
     * penghancuran ViewModel, dan posisi tidak boleh hilang hanya
     * karena scope dibatalkan.
     */
    fun onStop() {
        val position = playerManager.getCurrentPosition()
        val state = _uiState.value
        _uiState.update { it.copy(currentPositionMs = position) }
        if (state.videoId.isNotBlank() && state.videoPath.isNotBlank()) {
            viewModelScope.launch(NonCancellable) {
                playbackRepository.savePosition(state.videoId, state.videoPath, position)
            }
        }
        positionTicker?.cancel()
        positionTicker = null
        playerManager.release()
        _uiState.update { it.copy(isPlaying = false) }
    }

    override fun onCleared() {
        positionTicker?.cancel()
        playerManager.release()
        super.onCleared()
    }

    private fun onPlayerEvent(event: PlayerEvent) {
        when (event) {
            is PlayerEvent.PlaybackStateChanged -> {
                val newState = when (event.playbackState) {
                    Player.STATE_IDLE -> PlayerState.Idle
                    Player.STATE_BUFFERING -> PlayerState.Loading
                    Player.STATE_READY ->
                        if (_uiState.value.isPlaying) PlayerState.Playing else PlayerState.Paused
                    Player.STATE_ENDED -> PlayerState.Ended
                    else -> _uiState.value.playerState
                }
                _uiState.update {
                    it.copy(
                        playerState = newState,
                        durationMs = normalizedDuration()
                    )
                }
            }
            is PlayerEvent.IsPlayingChanged -> {
                _uiState.update {
                    val state = when {
                        event.isPlaying -> PlayerState.Playing
                        it.playerState == PlayerState.Playing -> PlayerState.Paused
                        else -> it.playerState
                    }
                    it.copy(isPlaying = event.isPlaying, playerState = state)
                }
            }
            is PlayerEvent.Error -> {
                _uiState.update {
                    it.copy(playerState = PlayerState.Error(event.message), isPlaying = false)
                }
            }
            is PlayerEvent.VideoSizeChanged -> {
                val size = event.videoSize
                val ratio = if (size.height > 0) {
                    (size.width * size.pixelWidthHeightRatio) / size.height
                } else {
                    null
                }
                _uiState.update { it.copy(videoAspectRatio = ratio) }
            }
            is PlayerEvent.PositionDiscontinuity -> {
                _uiState.update { it.copy(currentPositionMs = playerManager.getCurrentPosition()) }
            }
        }
    }

    private fun startPositionTicker() {
        positionTicker?.cancel()
        positionTicker = viewModelScope.launch {
            while (isActive) {
                _uiState.update {
                    it.copy(
                        currentPositionMs = playerManager.getCurrentPosition(),
                        durationMs = normalizedDuration()
                    )
                }
                delay(500)
            }
        }
    }

    /** `C.TIME_UNSET` dan nilai negatif dinormalisasi menjadi 0. */
    private fun normalizedDuration(): Long {
        val duration = playerManager.getDuration()
        return if (duration == C.TIME_UNSET || duration < 0) 0L else duration
    }
}
