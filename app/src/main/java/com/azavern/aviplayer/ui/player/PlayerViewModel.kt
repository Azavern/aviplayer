package com.azavern.aviplayer.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Player
import com.azavern.aviplayer.data.model.OrientationMode
import com.azavern.aviplayer.player.core.PlayerEvent
import com.azavern.aviplayer.player.core.PlayerEventListener
import com.azavern.aviplayer.player.core.PlayerState
import com.azavern.aviplayer.player.core.VideoPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
 * Siklus hidup: [initPlayer] menyiapkan video; [onStop] (dari
 * PlayerActivity.onStop) menyimpan posisi terakhir di state lalu
 * me-release player — penyimpanan persisten ke database adalah
 * Milestone 2. [onStart] menyiapkan ulang dari posisi terakhir bila
 * player sudah ter-release (mis. kembali dari Home ke Activity yang sama).
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: VideoPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

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
        _uiState.update {
            it.copy(
                videoPath = path,
                videoId = id,
                videoTitle = title,
                playerState = PlayerState.Loading,
                currentPositionMs = 0L,
                durationMs = 0L
            )
        }
        playerManager.setEventListener(PlayerEventListener { event -> onPlayerEvent(event) })
        playerManager.prepare(path, startPositionMs = 0L)
        startPositionTicker()
    }

    /** Dipanggil PlayerActivity.onStart: siapkan ulang bila player ter-release di onStop. */
    fun onStart() {
        val state = _uiState.value
        if (playerManager.isReleased && state.videoPath.isNotBlank()) {
            playerManager.setEventListener(PlayerEventListener { event -> onPlayerEvent(event) })
            playerManager.prepare(state.videoPath, startPositionMs = state.currentPositionMs)
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
     * catat posisi terakhir, hentikan ticker, release player.
     */
    fun onStop() {
        _uiState.update { it.copy(currentPositionMs = playerManager.getCurrentPosition()) }
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
