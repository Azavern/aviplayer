package com.azavern.aviplayer.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.azavern.aviplayer.player.core.PlayerState
import com.azavern.aviplayer.ui.player.PlayerUiState

/**
 * Layar Player — mengikuti referensi Veo (2026-11-04): permukaan video
 * memenuhi layar, bar atas dan gugus kontrol bawah MELAYANG di atasnya
 * (overlay), semua datar tanpa latar di balik tombol.
 *
 * Stateless: semua state dari [uiState], semua aksi lewat callback.
 */
@Composable
fun PlayerScreen(
    uiState: PlayerUiState,
    player: ExoPlayer,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onRotateClick: () -> Unit,
    onBackClick: () -> Unit,
    onSpeedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VideoSurface(
            player = player,
            modifier = Modifier.fillMaxSize()
        )

        when (val state = uiState.playerState) {
            PlayerState.Loading -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
            is PlayerState.Error -> Text(
                text = state.message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
            )
            else -> Unit
        }

        TitleDisplay(
            title = uiState.videoTitle,
            orientationMode = uiState.orientationMode,
            onBackClick = onBackClick,
            onRotateClick = onRotateClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            SeekBarComponent(
                positionMs = uiState.currentPositionMs,
                durationMs = uiState.durationMs,
                onSeekTo = onSeekTo
            )
            PlayerControls(
                isPlaying = uiState.isPlaying,
                playbackSpeed = uiState.playbackSpeed,
                onPreviousClick = onPreviousClick,
                onPlayPauseClick = onPlayPauseClick,
                onSpeedClick = onSpeedClick
            )
        }
    }
}
