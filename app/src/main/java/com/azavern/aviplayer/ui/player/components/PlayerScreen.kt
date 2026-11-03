package com.azavern.aviplayer.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.azavern.aviplayer.player.core.PlayerState
import com.azavern.aviplayer.ui.player.CaptureResult
import com.azavern.aviplayer.ui.player.PlayerUiState
import kotlinx.coroutines.delay

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
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSeekPreview: (Long) -> Unit,
    onSeekCommit: () -> Unit,
    onSeekClear: () -> Unit,
    onGestureSeekForward: () -> Unit,
    onGestureSeekBackward: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onCaptureClick: () -> Unit,
    onCaptureResultConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Lencana hasil capture tampil ±1,5 detik, lalu state dibersihkan
    // lewat onCaptureResultConsumed supaya tidak muncul dua kali.
    LaunchedEffect(uiState.captureResult) {
        if (uiState.captureResult != null) {
            delay(1500)
            onCaptureResultConsumed()
        }
    }

    val controlActions = PlayerControlActions(
        onPlayPause = onPlayPauseClick,
        onPrevious = onPreviousClick,
        onNext = {},
        onRotate = onRotateClick,
        onCapture = onCaptureClick,
        onSpeed = onSpeedClick
    )
    val layout = uiState.playerUiConfig.controlLayoutConfig

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VideoSurface(
            player = player,
            modifier = Modifier.fillMaxSize()
        )

        // Lapisan gesture Milestone 5: di atas video, di bawah kontrol,
        // sehingga tombol tetap menerima sentuhannya sendiri.
        GestureOverlay(
            uiState = uiState,
            onBrightnessChange = onBrightnessChange,
            onVolumeChange = onVolumeChange,
            onSeekPreview = onSeekPreview,
            onSeekCommit = onSeekCommit,
            onSeekClear = onSeekClear,
            onSeekForward = onGestureSeekForward,
            onSeekBackward = onGestureSeekBackward,
            onTogglePlayPause = onTogglePlayPause,
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
            titleConfig = uiState.playerUiConfig.titleConfig,
            topLeftSlot = layout.topLeftSlot,
            topRightSlots = layout.topRightSlots,
            uiState = uiState,
            actions = controlActions,
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Notifikasi hasil capture (perintah Veo 2026-11-04, mengganti
        // Snackbar kotak teks): lingkaran berisi centang (atau silang
        // bila gagal) yang menempel di tepi kanan layar, nyaris keluar
        // — hanya disisakan margin beberapa piksel. Meluncur masuk
        // dari kanan dan menghilang sendiri.
        AnimatedVisibility(
            visible = uiState.captureResult != null,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.captureResult is CaptureResult.Success) {
                        Icons.Filled.Check
                    } else {
                        Icons.Filled.Close
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            SeekBarComponent(
                positionMs = uiState.currentPositionMs,
                durationMs = uiState.durationMs,
                onSeekTo = onSeekTo,
                previewPositionMs = uiState.seekPreviewMs
            )
            PlayerControls(
                layout = layout,
                uiState = uiState,
                actions = controlActions
            )
        }
    }
}
