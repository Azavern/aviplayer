package com.azavern.aviplayer.ui.player.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.StayCurrentLandscape
import androidx.compose.material.icons.rounded.StayCurrentPortrait
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.R
import com.azavern.aviplayer.data.model.ControlSlot
import com.azavern.aviplayer.data.model.OrientationMode
import com.azavern.aviplayer.ui.player.PlayerUiState
import java.util.Locale

/**
 * Kumpulan aksi kontrol player, diteruskan sebagai satu objek ke
 * perender slot (Blueprint Milestone 7 — pengganti PlayerAction
 * sealed yang tidak dipakai di basis kode ini).
 */
data class PlayerControlActions(
    val onPlayPause: () -> Unit,
    val onPrevious: () -> Unit,
    val onNext: () -> Unit,
    val onRotate: () -> Unit,
    val onCapture: () -> Unit,
    val onSpeed: () -> Unit,
    val onMute: () -> Unit,
    val onLock: () -> Unit
)

/**
 * Render satu [ControlSlot] menjadi tombol aslinya (Milestone 7).
 * Semua tombol datar tanpa latar, sesuai referensi Veo.
 *
 * [prominent] hanya untuk PLAY_PAUSE di klaster tengah: tombol besar
 * 64dp dengan ikon 44dp. Di slot lain, putar/jeda berukuran normal.
 *
 * NEXT aktif hanya bila antrean konteks masih punya video
 * berikutnya (uiState.hasNext, Milestone 10); di ujung antrean ia
 * kembali redup dan nonaktif. SCREEN_RATIO dan EMPTY tidak
 * dirender (fitur belum ada / slot kosong).
 */
@Composable
fun ControlSlotButton(
    slot: ControlSlot,
    uiState: PlayerUiState,
    actions: PlayerControlActions,
    modifier: Modifier = Modifier,
    prominent: Boolean = false
) {
    when (slot) {
        ControlSlot.PLAY_PAUSE -> IconButton(
            onClick = actions.onPlayPause,
            modifier = if (prominent) modifier.size(64.dp) else modifier
        ) {
            Icon(
                imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(
                    if (uiState.isPlaying) R.string.player_action_pause else R.string.player_action_play
                ),
                tint = Color.White,
                modifier = Modifier.size(if (prominent) 44.dp else 32.dp)
            )
        }

        ControlSlot.PREVIOUS -> IconButton(onClick = actions.onPrevious, modifier = modifier) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = stringResource(R.string.player_action_previous),
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        ControlSlot.NEXT -> IconButton(
            onClick = actions.onNext,
            enabled = uiState.hasNext,
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = stringResource(R.string.player_action_next),
                tint = if (uiState.hasNext) Color.White else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(32.dp)
            )
        }

        ControlSlot.ORIENTATION -> IconButton(onClick = actions.onRotate, modifier = modifier) {
            Icon(
                imageVector = when (uiState.orientationMode) {
                    OrientationMode.AUTO -> Icons.Rounded.ScreenRotation
                    OrientationMode.LANDSCAPE -> Icons.Rounded.StayCurrentLandscape
                    OrientationMode.PORTRAIT -> Icons.Rounded.StayCurrentPortrait
                },
                contentDescription = stringResource(
                    when (uiState.orientationMode) {
                        OrientationMode.AUTO -> R.string.player_mode_auto
                        OrientationMode.LANDSCAPE -> R.string.player_mode_landscape
                        OrientationMode.PORTRAIT -> R.string.player_mode_portrait
                    }
                ),
                tint = Color.White
            )
        }

        ControlSlot.CAPTURE -> IconButton(onClick = actions.onCapture, modifier = modifier) {
            Icon(
                imageVector = Icons.Rounded.PhotoCamera,
                contentDescription = stringResource(R.string.player_action_capture),
                tint = Color.White
            )
        }

        ControlSlot.SPEED -> TextButton(onClick = actions.onSpeed, modifier = modifier) {
            Text(
                text = String.format(Locale.US, "%.1f×", uiState.playbackSpeed),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )
        }

        ControlSlot.MUTE -> IconButton(onClick = actions.onMute, modifier = modifier) {
            Icon(
                imageVector = if (uiState.isMuted) {
                    Icons.Rounded.VolumeOff
                } else {
                    Icons.Rounded.VolumeUp
                },
                contentDescription = stringResource(
                    if (uiState.isMuted) {
                        R.string.player_action_unmute
                    } else {
                        R.string.player_action_mute
                    }
                ),
                tint = Color.White
            )
        }

        ControlSlot.LOCK -> IconButton(onClick = actions.onLock, modifier = modifier) {
            Icon(
                imageVector = if (uiState.isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                contentDescription = stringResource(
                    if (uiState.isLocked) {
                        R.string.player_action_unlock
                    } else {
                        R.string.player_action_lock
                    }
                ),
                tint = Color.White
            )
        }

        ControlSlot.SCREEN_RATIO, ControlSlot.EMPTY -> Unit
    }
}
