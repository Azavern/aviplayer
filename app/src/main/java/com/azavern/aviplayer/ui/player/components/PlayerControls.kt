package com.azavern.aviplayer.ui.player.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.StayCurrentLandscape
import androidx.compose.material.icons.rounded.StayCurrentPortrait
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
import com.azavern.aviplayer.data.model.OrientationMode
import java.util.Locale

/**
 * Baris kontrol bawah player — mengikuti referensi Veo (2026-11-04):
 * klaster tengah sebelumnya / putar-jeda besar / berikutnya, semua
 * ikon DATAR tanpa latar. Di pojok kanan: teks kecepatan, lalu tombol
 * rotasi (Bagian 10F) PALING pojok — koreksi Veo 2026-11-04.
 *
 * Tombol rotasi TANPA label teks (perintah Veo): mode dibedakan dari
 * ikonnya — putar otomatis / ponsel landscape / ponsel portrait.
 * Nama mode tetap jadi contentDescription untuk aksesibilitas.
 *
 * Tombol capture frame (Milestone 6) duduk di kanan, tepat di samping
 * tombol kecepatan — dipindah ke sini dari tepi kiri tengah atas
 * perintah Veo 2026-11-04 karena posisi lama mengganggu.
 *
 * Yang belum digambar dari referensi (menunggu milestone fiturnya):
 * kunci di kiri (kunci kontrol).
 *
 * Tombol NEXT tetap nonaktif: belum ada antrean (folder/playlist
 * datang di milestone Beranda & Playlist).
 */
@Composable
fun PlayerControls(
    isPlaying: Boolean,
    playbackSpeed: Float,
    orientationMode: OrientationMode,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onRotateClick: () -> Unit,
    onCaptureClick: () -> Unit,
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
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCaptureClick) {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = stringResource(R.string.player_action_capture),
                    tint = Color.White
                )
            }
            TextButton(onClick = onSpeedClick) {
                Text(
                    text = String.format(Locale.US, "%.1f×", playbackSpeed),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            IconButton(onClick = onRotateClick) {
                Icon(
                    imageVector = when (orientationMode) {
                        OrientationMode.AUTO -> Icons.Rounded.ScreenRotation
                        OrientationMode.LANDSCAPE -> Icons.Rounded.StayCurrentLandscape
                        OrientationMode.PORTRAIT -> Icons.Rounded.StayCurrentPortrait
                    },
                    contentDescription = stringResource(
                        when (orientationMode) {
                            OrientationMode.AUTO -> R.string.player_mode_auto
                            OrientationMode.LANDSCAPE -> R.string.player_mode_landscape
                            OrientationMode.PORTRAIT -> R.string.player_mode_portrait
                        }
                    ),
                    tint = Color.White
                )
            }
        }
    }
}
