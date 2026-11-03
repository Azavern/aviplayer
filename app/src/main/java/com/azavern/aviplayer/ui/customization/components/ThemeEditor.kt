package com.azavern.aviplayer.ui.customization.components

import android.os.Build
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.data.model.ThemeMode
import java.util.Locale

/** Swatch warna aksen bawaan editor (yang pertama = lavender bawaan). */
private val AccentColorPresets: List<Long> = listOf(
    0xFF6655F5, // lavender (bawaan)
    0xFF2196F3, // biru
    0xFF00BCD4, // sian
    0xFF009688, // teal
    0xFF4CAF50, // hijau
    0xFFFFC107, // amber
    0xFFFF5722, // oranye tua
    0xFFF44336, // merah
    0xFFE91E63, // merah muda
    0xFF9C27B0  // ungu
)

/**
 * Editor tema aplikasi (Milestone 8): mode tema (Sistem/Terang/
 * Gelap/AMOLED), warna dinamis sistem (API 31+), dan warna aksen
 * (swatch + input HEX). Memilih swatch/HEX mematikan warna dinamis
 * supaya pilihan pengguna langsung berlaku.
 *
 * Pratinjau tidak perlu kartu khusus yang terpisah jauh: seluruh
 * layar Kustomisasi sendiri dibungkus AppTheme dengan config
 * berjalan, jadi perubahan terlihat seketika di semua elemen;
 * kartu pratinjau di bawah menegaskan latar/permukaan/aksen.
 */
@Composable
fun ThemeEditor(
    themeConfig: ThemeConfig,
    onThemeConfigChanged: (ThemeConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode tema
        Column {
            Text(
                text = stringResource(R.string.theme_editor_mode),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = themeConfig.themeMode == mode,
                        onClick = { onThemeConfigChanged(themeConfig.copy(themeMode = mode)) },
                        label = {
                            Text(
                                stringResource(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> R.string.theme_mode_system
                                        ThemeMode.LIGHT -> R.string.theme_mode_light
                                        ThemeMode.DARK -> R.string.theme_mode_dark
                                        ThemeMode.AMOLED -> R.string.theme_mode_amoled
                                    }
                                )
                            )
                        }
                    )
                }
            }
        }

        // Warna dinamis (hanya API 31+; di bawah itu tidak ditawarkan)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.theme_editor_dynamic),
                    style = MaterialTheme.typography.titleSmall
                )
                Switch(
                    checked = themeConfig.useSystemAccent,
                    onCheckedChange = {
                        onThemeConfigChanged(themeConfig.copy(useSystemAccent = it))
                    }
                )
            }
        }

        // Warna aksen: swatch + HEX
        Column {
            Text(
                text = stringResource(R.string.theme_editor_accent),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AccentColorPresets.forEach { preset ->
                    val selected = !themeConfig.useSystemAccent &&
                        themeConfig.accentColor == preset
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
                            .clickable {
                                onThemeConfigChanged(
                                    themeConfig.copy(accentColor = preset, useSystemAccent = false)
                                )
                            }
                    )
                }
            }
            var hexText by remember(themeConfig.accentColor) {
                mutableStateOf(String.format(Locale.US, "%06X", themeConfig.accentColor and 0xFFFFFFL))
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
                    onValueChange = {
                        hexText = it.uppercase().filter { c -> c.isDigit() || c in 'A'..'F' }.take(6)
                    },
                    label = { Text(stringResource(R.string.title_editor_hex_hint)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val rgb = hexText.toLongOrNull(16)
                        if (rgb != null && hexText.length == 6) {
                            onThemeConfigChanged(
                                themeConfig.copy(
                                    accentColor = 0xFF000000L or rgb,
                                    useSystemAccent = false
                                )
                            )
                        }
                    }
                ) {
                    Text(stringResource(R.string.title_editor_apply))
                }
            }
        }

        // Pratinjau: latar / permukaan / aksen dari skema berjalan.
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PreviewSwatch(
                    label = stringResource(R.string.theme_preview_background),
                    color = MaterialTheme.colorScheme.background
                )
                PreviewSwatch(
                    label = stringResource(R.string.theme_preview_surface),
                    color = MaterialTheme.colorScheme.surface
                )
                PreviewSwatch(
                    label = stringResource(R.string.theme_preview_accent),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PreviewSwatch(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
