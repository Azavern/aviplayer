package com.azavern.aviplayer.ui.player.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.player.gesture.GestureZone
import com.azavern.aviplayer.player.gesture.GestureZoneCalculator
import com.azavern.aviplayer.ui.player.PlayerUiState
import com.azavern.aviplayer.utils.TimeUtils
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Lapisan gesture di atas permukaan video (Blueprint Bagian 7 &
 * Milestone 5). Mendeteksi:
 *
 * - Drag vertikal zona kiri  → kecerahan (feedback ikon + persen)
 * - Drag vertikal zona kanan → volume (feedback ikon + persen)
 * - Drag horizontal zona tengah → pratinjau seek; seek beneran
 *   terjadi saat jari dilepas (uiState.seekPreviewMs)
 * - Double tap kiri/kanan → mundur/maju 10 detik + ripple animasi
 *
 * Sentuhan yang sudah dikonsumsi tombol/slider (down.isConsumed)
 * diabaikan sepenuhnya, jadi gesture tidak berebut dengan kontrol.
 * Nilai level diterapkan PlayerActivity (window & AudioManager);
 * komponen ini hanya membaca state dan memanggil callback.
 */
@Composable
fun GestureOverlay(
    uiState: PlayerUiState,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSeekPreview: (Long) -> Unit,
    onSeekCommit: () -> Unit,
    onSeekClear: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stateRef = rememberUpdatedState(uiState)
    var levelFeedback by remember { mutableStateOf<LevelFeedback?>(null) }
    var ripple by remember { mutableStateOf<RippleState?>(null) }
    var lastTap by remember { mutableStateOf<TapRecord?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                val slop = viewConfiguration.touchSlop
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (down.isConsumed) return@awaitEachGesture

                    val snapshot = stateRef.value
                    val startX = down.position.x
                    val startY = down.position.y
                    val zone = GestureZoneCalculator.getZone(startX, size.width.toFloat())
                    val startBrightness = snapshot.brightnessLevel.coerceIn(0f, 1f)
                    val startVolume = snapshot.volumeLevel
                    val startPositionMs = snapshot.currentPositionMs
                    val durationMs = snapshot.durationMs

                    var totalDx = 0f
                    var totalDy = 0f
                    var moved = false
                    var horizontal: Boolean? = null
                    var released: PointerInputChange? = null

                    while (released == null) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            released = change
                            break
                        }
                        totalDx = change.position.x - startX
                        totalDy = change.position.y - startY
                        if (!moved && (abs(totalDx) > slop || abs(totalDy) > slop)) {
                            moved = true
                            horizontal = abs(totalDx) > abs(totalDy)
                        }
                        if (moved) {
                            change.consume()
                            when (horizontal) {
                                true -> if (zone == GestureZone.SEEK) {
                                    val deltaMs = GestureZoneCalculator.calculateSeekDeltaMs(
                                        totalDx, size.width.toFloat(), durationMs
                                    )
                                    onSeekPreview(startPositionMs + deltaMs)
                                }
                                false -> when (zone) {
                                    GestureZone.BRIGHTNESS -> {
                                        val level = (startBrightness +
                                            GestureZoneCalculator.calculateBrightnessDelta(
                                                totalDy, size.height.toFloat()
                                            )).coerceIn(0f, 1f)
                                        onBrightnessChange(level)
                                        levelFeedback = LevelFeedback(LevelKind.BRIGHTNESS, level)
                                    }
                                    GestureZone.VOLUME -> {
                                        val level = (startVolume +
                                            GestureZoneCalculator.calculateVolumeDelta(
                                                totalDy, size.height.toFloat()
                                            )).coerceIn(0f, 1f)
                                        onVolumeChange(level)
                                        levelFeedback = LevelFeedback(LevelKind.VOLUME, level)
                                    }
                                    else -> Unit
                                }
                                null -> Unit
                            }
                        }
                    }

                    if (released == null) {
                        // Sentuhan hilang di tengah jalan: batalkan pratinjau.
                        onSeekClear()
                    } else if (!moved) {
                        // Tap: cek double tap pada zona yang sama.
                        val previous = lastTap
                        val isDouble = previous != null &&
                            released.uptimeMillis - previous.uptimeMillis < 300L &&
                            GestureZoneCalculator.getZone(startX, size.width.toFloat()) == previous.zone
                        if (isDouble) {
                            lastTap = null
                            when (zone) {
                                GestureZone.BRIGHTNESS -> {
                                    onSeekBackward()
                                    ripple = RippleState(
                                        forward = false,
                                        offset = DpOffset(
                                            (startX / density).dp,
                                            (startY / density).dp
                                        ),
                                        id = released.uptimeMillis
                                    )
                                }
                                GestureZone.VOLUME -> {
                                    onSeekForward()
                                    ripple = RippleState(
                                        forward = true,
                                        offset = DpOffset(
                                            (startX / density).dp,
                                            (startY / density).dp
                                        ),
                                        id = released.uptimeMillis
                                    )
                                }
                                else -> Unit
                            }
                        } else {
                            lastTap = TapRecord(released.uptimeMillis, zone)
                        }
                    } else if (horizontal == true && zone == GestureZone.SEEK) {
                        onSeekCommit()
                    }
                    levelFeedback = null
                }
            }
    ) {
        // Feedback drag vertikal: ikon + bar + persen, di tengah atas.
        levelFeedback?.let { feedback ->
            LevelFeedbackIndicator(
                feedback = feedback,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp)
            )
        }

        // Feedback pratinjau seek: posisi tujuan + selisihnya, di tengah.
        val preview = uiState.seekPreviewMs
        if (preview != null) {
            SeekPreviewIndicator(
                previewMs = preview,
                currentMs = uiState.currentPositionMs,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Ripple double tap di titik ketukan.
        ripple?.let { state ->
            TapRipple(
                state = state,
                onFinished = { if (ripple?.id == state.id) ripple = null }
            )
        }
    }
}

private enum class LevelKind { BRIGHTNESS, VOLUME }

private data class LevelFeedback(val kind: LevelKind, val fraction: Float)

private data class TapRecord(val uptimeMillis: Long, val zone: GestureZone)

private data class RippleState(val forward: Boolean, val offset: DpOffset, val id: Long)

@Composable
private fun LevelFeedbackIndicator(feedback: LevelFeedback, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = when (feedback.kind) {
                LevelKind.BRIGHTNESS -> Icons.Filled.BrightnessMedium
                LevelKind.VOLUME ->
                    if (feedback.fraction <= 0f) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .width(120.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(feedback.fraction.coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(Color.White)
            )
        }
        Text(
            text = "${(feedback.fraction * 100).roundToInt()}%",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun SeekPreviewIndicator(previewMs: Long, currentMs: Long, modifier: Modifier = Modifier) {
    val deltaMs = previewMs - currentMs
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (deltaMs >= 0) Icons.Filled.FastForward else Icons.Filled.FastRewind,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = TimeUtils.msToTimestamp(previewMs),
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = (if (deltaMs >= 0) "+" else "−") +
                TimeUtils.msToTimestamp(abs(deltaMs)),
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TapRipple(state: RippleState, onFinished: () -> Unit) {
    val progress = remember(state.id) { Animatable(0f) }
    LaunchedEffect(state.id) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 450))
        onFinished()
    }
    val alpha = (1f - progress.value) * 0.35f
    val sizeDp = (80 + 50 * progress.value).dp
    Box(
        modifier = Modifier
            .offset(x = state.offset.x - sizeDp / 2, y = state.offset.y - sizeDp / 2)
            .size(sizeDp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (state.forward) Icons.Filled.Forward10 else Icons.Filled.Replay10,
            contentDescription = null,
            tint = Color.White.copy(alpha = (1f - progress.value).coerceIn(0f, 1f)),
            modifier = Modifier.size(36.dp)
        )
    }
}
