package com.azavern.aviplayer.ui.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.R
import com.azavern.aviplayer.data.model.OrientationMode

/**
 * Kontrol player versi Milestone 1 — HARDCODED (Blueprint Milestone 1
 * butir 9), belum sistem slot: previous, play/pause, next, dan tombol
 * rotasi slot ORIENTATION beserta label mode aktifnya (Bagian 10F).
 *
 * Tombol NEXT sengaja nonaktif di milestone ini: belum ada antrean
 * (folder/playlist datang di milestone Beranda & Playlist).
 */
@Composable
fun PlayerControls(
    isPlaying: Boolean,
    orientationMode: OrientationMode,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onRotateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = stringResource(R.string.player_action_previous),
                tint = Color.White
            )
        }

        IconButton(onClick = onPlayPauseClick) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(
                    if (isPlaying) R.string.player_action_pause else R.string.player_action_play
                ),
                tint = Color.White
            )
        }

        IconButton(onClick = {}, enabled = false) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = stringResource(R.string.player_action_next),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onRotateClick) {
            Icon(
                imageVector = Icons.Rounded.ScreenRotation,
                contentDescription = stringResource(R.string.player_action_rotate),
                tint = Color.White
            )
        }
        Text(
            text = stringResource(
                when (orientationMode) {
                    OrientationMode.AUTO -> R.string.player_mode_auto
                    OrientationMode.LANDSCAPE -> R.string.player_mode_landscape
                    OrientationMode.PORTRAIT -> R.string.player_mode_portrait
                }
            ),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
