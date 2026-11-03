package com.azavern.aviplayer.ui.player.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.R
import java.util.Locale

/**
 * Baris kontrol bawah player — mengikuti referensi Veo (2026-11-04):
 * klaster tengah sebelumnya / putar-jeda besar / berikutnya, teks
 * kecepatan di kanan, semua ikon DATAR tanpa latar.
 *
 * Yang belum digambar dari referensi (menunggu milestone fiturnya):
 * kunci di kiri (kunci kontrol), ikon tangkapan di kanan (Milestone 6).
 * Tombol rotasi kini tinggal di bar atas (TitleDisplay).
 *
 * Tombol NEXT tetap nonaktif: belum ada antrean (folder/playlist
 * datang di milestone Beranda & Playlist).
 */
@Composable
fun PlayerControls(
    isPlaying: Boolean,
    playbackSpeed: Float,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSpeedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = stringResource(R.string.player_action_previous),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.player_action_pause else R.string.player_action_play
                    ),
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            IconButton(onClick = {}, enabled = false) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = stringResource(R.string.player_action_next),
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        TextButton(
            onClick = onSpeedClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                text = String.format(Locale.US, "%.1f×", playbackSpeed),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}
