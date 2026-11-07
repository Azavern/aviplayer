package com.azavern.aviplayer.ui.player

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import kotlin.math.roundToInt
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azavern.aviplayer.data.model.OrientationMode
import com.azavern.aviplayer.ui.player.components.PlayerScreen
import com.azavern.aviplayer.ui.theme.AppTheme
import com.azavern.aviplayer.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

/**
 * Layar pemutar video (Blueprint Milestone 1, Bagian 6 & 10F).
 *
 * Menerima path video dari Intent ([Constants.KEY_VIDEO_PATH]),
 * menerapkan orientasi runtime dari state (BUKAN kunci Manifest),
 * immersive + keep screen on, dan me-release player di [onStop]
 * sesuai lifecycle kritis Bagian 10A.
 */
@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        enterImmersiveMode()

        val videoPath = intent.getStringExtra(Constants.KEY_VIDEO_PATH).orEmpty()
        val videoId = intent.getStringExtra(Constants.KEY_VIDEO_ID).orEmpty()
        val videoTitle = intent.getStringExtra(Constants.KEY_VIDEO_TITLE).orEmpty()
        // Antrean konteks putar (Milestone 10): kelompok video asal buka.
        val queuePaths = intent.getStringArrayListExtra(Constants.KEY_QUEUE_PATHS)
            .orEmpty()
        val queueTitles = intent.getStringArrayListExtra(Constants.KEY_QUEUE_TITLES)
            .orEmpty()
        // Durasi antrean (Milestone 10+): syarat saringan titik resume
        // di PlayerViewModel/PlaybackRepository (spek Veo 2026-11-07).
        val queueDurations = intent.getLongArrayExtra(Constants.KEY_QUEUE_DURATIONS)
            ?.toList().orEmpty()
        viewModel.initPlayer(
            videoPath, videoId, videoTitle, queuePaths, queueTitles, queueDurations
        )
        viewModel.setSystemAutoRotate(readSystemAutoRotate())
        // Level awal gesture (Milestone 5) disinkronkan sebelum konten
        // digambar, supaya efek penerapan tidak melompat dari nilai bawaan.
        viewModel.syncSystemLevels(readEffectiveBrightness(), readVolumeFraction())

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AppTheme(themeConfig = uiState.themeConfig) {
                // Orientasi adalah state runtime (Bagian 10F): terapkan setiap berubah.
                // Mode sensor (bukan FULL_USER) supaya arah layar membaca
                // gerak perangkat terlepas dari kunci rotasi HP: mode
                // LANDSCAPE membalik antara landscape normal dan reverse
                // landscape mengikuti pegangan (kepala HP di kiri/kanan).
                LaunchedEffect(uiState.orientationMode) {
                    requestedOrientation = when (uiState.orientationMode) {
                        OrientationMode.AUTO -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
                        OrientationMode.LANDSCAPE ->
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        OrientationMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }

                // Gesture Milestone 5: volume di state diterapkan ke
                // AudioManager di sini; ViewModel tidak menyentuh sistem.
                // Kecerahan HANYA diterapkan bila pengguna mengubahnya
                // lewat gesture (brightnessOverrideActive) — penerapan
                // otomatis saat mulai diputar dihapus atas perintah Veo
                // 2026-11-04 karena membuat kecerahan melonjak sendiri.
                LaunchedEffect(uiState.brightnessLevel, uiState.brightnessOverrideActive) {
                    val level = uiState.brightnessLevel
                    if (uiState.brightnessOverrideActive && level >= 0f) {
                        val attrs = window.attributes
                        if (attrs.screenBrightness != level) {
                            attrs.screenBrightness = level
                            window.attributes = attrs
                        }
                    }
                }
                LaunchedEffect(uiState.volumeLevel) {
                    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val target = (uiState.volumeLevel * max).roundToInt().coerceIn(0, max)
                    if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != target) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                    }
                }

                PlayerScreen(
                    uiState = uiState,
                    player = viewModel.player,
                    onPreviousClick = viewModel::onPrevious,
                    onNextClick = viewModel::onNext,
                    onPlayPauseClick = viewModel::togglePlayPause,
                    onSeekTo = viewModel::seekTo,
                    onRotateClick = viewModel::cycleOrientationMode,
                    onBackClick = { finish() },
                    onSpeedClick = viewModel::cycleSpeed,
                    onBrightnessChange = viewModel::adjustBrightness,
                    onVolumeChange = viewModel::adjustVolume,
                    onSeekPreview = viewModel::onSeekPreview,
                    onSeekCommit = viewModel::commitSeekPreview,
                    onSeekClear = viewModel::clearSeekPreview,
                    onGestureSeekForward = viewModel::seekForward,
                    onGestureSeekBackward = viewModel::seekBackward,
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onCaptureClick = viewModel::captureFrame,
                    onCaptureResultConsumed = viewModel::onCaptureResultConsumed,
                    onMuteClick = viewModel::toggleMute
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Siapkan ulang player bila ter-release di onStop (mis. kembali dari Home).
        viewModel.onStart()
        viewModel.setSystemAutoRotate(readSystemAutoRotate())
        viewModel.syncSystemLevels(readEffectiveBrightness(), readVolumeFraction())
    }

    override fun onStop() {
        // Blueprint Bagian 10A: posisi dicatat ViewModel, player di-release di sini.
        viewModel.onStop()
        super.onStop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    /** Sembunyikan system bars; muncul sesaat saat di-swipe dari tepi. */
    private fun enterImmersiveMode() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /** True bila kunci rotasi HP mati (Settings.System, tanpa izin khusus — Bagian 10F). */
    private fun readSystemAutoRotate(): Boolean =
        Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 1

    /**
     * Kecerahan efektif saat ini (0f..1f): atribut window bila sudah
     * diatur aplikasi, jika tidak (-1) nilai Settings.System.
     */
    private fun readEffectiveBrightness(): Float {
        val windowBrightness = window.attributes.screenBrightness
        if (windowBrightness >= 0f) return windowBrightness
        val systemValue = Settings.System.getInt(
            contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128
        )
        return (systemValue / 255f).coerceIn(0f, 1f)
    }

    /** Volume musik saat ini sebagai fraksi 0f..1f dari maksimum. */
    private fun readVolumeFraction(): Float {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (max <= 0) return 0f
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max
    }
}
