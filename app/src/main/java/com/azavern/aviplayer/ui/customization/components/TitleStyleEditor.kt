package com.azavern.aviplayer.ui.customization.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.R
import com.azavern.aviplayer.data.model.PlaybackMode
import com.azavern.aviplayer.data.model.TitleConfig
import com.azavern.aviplayer.ui.player.PlayerUiState
import com.azavern.aviplayer.ui.player.components.PlayerControlActions
import com.azavern.aviplayer.ui.player.components.TitleDisplay
import java.util.Locale

/** Swatch warna judul bawaan editor. */
private val TitleColorPresets: List<Long> = listOf(
    0xFFFFFFFF, // putih
    0xFF8B7FFF, // lavender terang
    0xFFFFD54F, // kuning
    0xFF4DD0E1, // sian
    0xFF81C784, // hijau
    0xFFFFB74D, // oranye
    0xFFF48FB1, // merah muda
    0xFFE57373  // merah
)

/**
 * Editor gaya judul player (Milestone 7): ukuran font (slider),
 * jumlah baris, tebal, marquee, dan warna (swatch + input HEX).
 * Setiap perubahan langsung disimpan ViewModel dan pratinjau di
 * bagian bawah memakai TitleDisplay asli dengan config berjalan.
 */
@Composable
fun TitleStyleEditor(
    titleConfig: TitleConfig,
    onTitleConfigChanged: (TitleConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ukuran font
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.title_editor_font_size),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = String.format(Locale.US, "%.1f sp", titleConfig.fontSizeSp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Slider(
                value = titleConfig.fontSizeSp,
                onValueChange = { onTitleConfigChanged(titleConfig.copy(fontSizeSp = it)) },
                valueRange = 10f..22f,
                steps = 23
            )
        }

        // Jumlah baris
        Column {
            Text(
                text = stringResource(R.string.title_editor_lines),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2).forEach { lines ->
                    FilterChip(
                        selected = titleConfig.maxLines == lines,
                        onClick = { onTitleConfigChanged(titleConfig.copy(maxLines = lines)) },
                        label = { Text("$lines") }
                    )
                }
            }
        }

        // Tebal & marquee
        EditorSwitchRow(
            label = stringResource(R.string.title_editor_bold),
            checked = titleConfig.isBold,
            onCheckedChange = { onTitleConfigChanged(titleConfig.copy(isBold = it)) }
        )
        EditorSwitchRow(
            label = stringResource(R.string.title_editor_marquee),
            checked = titleConfig.marqueeEnabled,
            onCheckedChange = { onTitleConfigChanged(titleConfig.copy(marqueeEnabled = it)) }
        )

        // Warna: swatch + HEX
        Column {
            Text(
                text = stringResource(R.string.title_editor_color),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TitleColorPresets.forEach { preset ->
                    val selected = titleConfig.color == preset
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(preset))
                            .then(
                                if (selected) {
                                    Modifier.border(
                                        3.dp, MaterialTheme.colorScheme.primary, CircleShape
                                    )
                                } else {
                                    Modifier.border(
                                        1.dp, MaterialTheme.colorScheme.outline, CircleShape
                                    )
                                }
                            )
                            .clickable { onTitleConfigChanged(titleConfig.copy(color = preset)) }
                    )
                }
            }
            var hexText by remember(titleConfig.color) {
                mutableStateOf(String.format(Locale.US, "%06X", titleConfig.color and 0xFFFFFFL))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = hexText,
                    onValueChange = { hexText = it.uppercase().filter { c -> c.isDigit() || c in 'A'..'F' }.take(6) },
                    label = { Text(stringResource(R.string.title_editor_hex_hint)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val rgb = hexText.toLongOrNull(16)
                        if (rgb != null && hexText.length == 6) {
                            onTitleConfigChanged(titleConfig.copy(color = 0xFF000000L or rgb))
                        }
                    }
                ) {
                    Text(stringResource(R.string.title_editor_apply))
                }
            }
        }

        // Pratinjau langsung: TitleDisplay asli di atas kartu gelap.
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            TitleDisplay(
                title = stringResource(R.string.title_editor_preview_sample),
                titleConfig = titleConfig,
                topRightSlots = emptyList(),
                uiState = PlayerUiState(),
                actions = PlayerControlActions({}, {}, {}, {}, {}, {}, {}, {}),
                onBackClick = {},
                playbackMode = PlaybackMode.LOOP_ALL,
                onPlaybackModeSelected = {}
            )
        }
    }
}

@Composable
private fun EditorSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
