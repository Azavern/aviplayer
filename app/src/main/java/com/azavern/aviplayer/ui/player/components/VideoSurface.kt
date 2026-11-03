package com.azavern.aviplayer.ui.player.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/**
 * Permukaan video (Blueprint Milestone 1): [PlayerView] Media3 di dalam
 * [AndroidView]. Kontrol bawaan PlayerView dimatikan — kontrol aplikasi
 * digambar Compose (PlayerControls). RESIZE_MODE_FIT menjaga rasio asli
 * video (letterbox), sesuai Blueprint Bagian 5.
 */
@Composable
fun VideoSurface(
    player: ExoPlayer,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        update = { view -> view.player = player },
        modifier = modifier
    )
}
