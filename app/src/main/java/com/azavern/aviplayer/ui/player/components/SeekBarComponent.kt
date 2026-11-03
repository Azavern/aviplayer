package com.azavern.aviplayer.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.utils.TimeUtils

/**
 * Seekbar player — mengikuti referensi Veo (2026-11-04): waktu kini di
 * kiri, waktu total di kanan, rel tipis abu-abu dengan isian PUTIH dan
 * ibu jari lingkaran putih besar (gaya klasik, digambar lewat slot
 * thumb/track sendiri karena slider Material 3 bawaan kini berbentuk
 * pil vertikal). Selama digeser, nilai lokal yang ditampilkan; seek
 * beneran dikirim saat geser selesai (onValueChangeFinished).
 *
 * Catatan: UI.md menetapkan isian seekbar lavender — warna putih di
 * sini mengikuti referensi terbaru Veo, yang kedudukannya di atas
 * dokumen (urutan sumber kebenaran di AGENTS.md).
 *
 * Slot thumb/track kustom Slider masih berstatus eksperimental di
 * Material 3, maka perlu @OptIn di bawah.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
            thumb = {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            },
            track = { sliderState ->
                val range = sliderState.valueRange
                val fraction = if (range.endInclusive > range.start) {
                    (sliderState.value - range.start) / (range.endInclusive - range.start)
                } else {
                    0f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.35f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                            .height(3.dp)
                            .background(Color.White)
                    )
                }
            },
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
