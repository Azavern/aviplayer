package com.azavern.aviplayer.ui.player

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
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
import com.azavern.aviplayer.data.model.ThemeConfig
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        enterImmersiveMode()

        val videoPath = intent.getStringExtra(Constants.KEY_VIDEO_PATH).orEmpty()
        val videoId = intent.getStringExtra(Constants.KEY_VIDEO_ID).orEmpty()
        val videoTitle = intent.getStringExtra(Constants.KEY_VIDEO_TITLE).orEmpty()
        viewModel.initPlayer(videoPath, videoId, videoTitle)
        viewModel.setSystemAutoRotate(readSystemAutoRotate())

        setContent {
            AppTheme(themeConfig = ThemeConfig.Default) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Orientasi adalah state runtime (Bagian 10F): terapkan setiap berubah.
                LaunchedEffect(uiState.orientationMode) {
                    requestedOrientation = when (uiState.orientationMode) {
                        OrientationMode.AUTO -> ActivityInfo.SCREEN_ORIENTATION_FULL_USER
                        OrientationMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        OrientationMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }

                PlayerScreen(
                    uiState = uiState,
                    player = viewModel.player,
                    onPreviousClick = viewModel::onPrevious,
                    onPlayPauseClick = viewModel::togglePlayPause,
                    onSeekTo = viewModel::seekTo,
                    onRotateClick = viewModel::cycleOrientationMode
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Siapkan ulang player bila ter-release di onStop (mis. kembali dari Home).
        viewModel.onStart()
        viewModel.setSystemAutoRotate(readSystemAutoRotate())
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
}
