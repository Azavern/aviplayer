package com.azavern.aviplayer.ui.player.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.utils.TimeUtils

/**
 * Seekbar sederhana (Blueprint Milestone 1): slider posisi + label waktu
 * kini/total. Selama digeser, nilai lokal yang ditampilkan; seek beneran
 * dikirim saat geser selesai (onValueChangeFinished).
 */
@Composable
fun SeekBarComponent(
    positionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var scrubbingValue by remember { mutableStateOf<Float?>(null) }
    val displayPosition = scrubbingValue?.toLong() ?: positionMs

    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = TimeUtils.msToTimestamp(displayPosition),
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
        )
        Slider(
            value = displayPosition.toFloat(),
            onValueChange = { value -> scrubbingValue = value },
            onValueChangeFinished = {
                scrubbingValue?.let { onSeekTo(it.toLong()) }
                scrubbingValue = null
            },
            valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )
        Text(
            text = TimeUtils.msToTimestamp(durationMs),
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
