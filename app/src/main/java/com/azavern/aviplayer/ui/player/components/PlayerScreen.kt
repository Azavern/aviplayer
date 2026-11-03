package com.azavern.aviplayer.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.exoplayer.ExoPlayer
import com.azavern.aviplayer.player.core.PlayerState
import com.azavern.aviplayer.ui.player.PlayerUiState

/**
 * Layar Player (Blueprint Milestone 1): permukaan video hitam memenuhi
 * area tengah (letterbox oleh PlayerView), judul di atas, seekbar dan
 * kontrol di bawah — tetap terjangkau di portrait maupun landscape.
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TitleDisplay(title = uiState.videoTitle)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            VideoSurface(
                player = player,
                modifier = Modifier.fillMaxSize()
            )
            when (val state = uiState.playerState) {
                PlayerState.Loading -> CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
                is PlayerState.Error -> Text(
                    text = state.message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                else -> Unit
            }
        }

        SeekBarComponent(
            positionMs = uiState.currentPositionMs,
            durationMs = uiState.durationMs,
            onSeekTo = onSeekTo
        )
        PlayerControls(
            isPlaying = uiState.isPlaying,
            orientationMode = uiState.orientationMode,
            onPreviousClick = onPreviousClick,
            onPlayPauseClick = onPlayPauseClick,
            onRotateClick = onRotateClick
        )
    }
}
