package com.azavern.aviplayer.ui.player.components

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azavern.aviplayer.R
import com.azavern.aviplayer.data.model.ControlSlot
import com.azavern.aviplayer.data.model.TitleConfig
import com.azavern.aviplayer.ui.player.PlayerUiState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Bar atas player — mengikuti referensi Veo (2026-11-04):
 * panah kembali, judul satu baris, lalu indikator baterai + jam
 * di kanan.
 *
 * Semua tombol DATAR: tanpa latar di balik ikon. Indikator baterai
 * duduk di dalam bar dengan padding normal (di mockup referensi ia
 * terlalu mepet ke pojok sampai nyaris terpotong — di sini dirapikan).
 * Tombol rotasi tinggal di pojok kanan bawah (PlayerControls),
 * sesuai koreksi Veo 2026-11-04.
 *
 * Tombol-tombol referensi yang fiturnya belum ada (PiP, subtitle,
 * volume, A-B, menu titik tiga) sengaja belum digambar; mereka datang
 * bersama milestone fiturnya masing-masing.
 *
 * Milestone 7: gaya teks judul mengikuti [TitleConfig] (ukuran,
 * baris, warna, tebal, marquee), dan slot atas-kanan layout kontrol
 * dirender di sini sesudah indikator baterai/jam.
 *
 * Area KIRI ATAS TERKUNCI (perintah Veo 2026-11-04): panah kembali
 * selalu yang pertama dan tidak ada slot yang bisa menempati posisi
 * sebelum/sesudahnya — area ini tetap, hanya gaya judulnya yang
 * bisa diubah (tab Judul). Slot kiri atas sudah tidak dirender di
 * sini; layout lama yang menyimpannya dibersihkan SettingsRepository.
 */
@Composable
fun TitleDisplay(
    title: String,
    titleConfig: TitleConfig,
    topRightSlots: List<ControlSlot>,
    uiState: PlayerUiState,
    actions: PlayerControlActions,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // Margin tepi diperlebar (perintah Veo 2026-11-07): elemen
            // melayang tidak lagi mepet tepi layar.
            .padding(horizontal = 10.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.player_action_back),
                tint = Color.White
            )
        }
        Text(
            text = title,
            color = Color(titleConfig.color),
            fontSize = titleConfig.fontSizeSp.sp,
            fontWeight = if (titleConfig.isBold) FontWeight.Bold else FontWeight.Normal,
            maxLines = titleConfig.maxLines,
            overflow = if (titleConfig.marqueeEnabled) {
                TextOverflow.Visible
            } else {
                TextOverflow.Ellipsis
            },
            modifier = Modifier
                .weight(1f)
                .then(
                    if (titleConfig.marqueeEnabled && titleConfig.maxLines == 1) {
                        Modifier.basicMarquee()
                    } else {
                        Modifier
                    }
                )
        )
        SystemStatusIndicator()
        topRightSlots.forEach { slot ->
            if (slot != ControlSlot.EMPTY) {
                ControlSlotButton(slot = slot, uiState = uiState, actions = actions)
            }
        }
    }
}

/**
 * Baterai + jam dinding untuk bar atas player. Murni tampilan:
 * dibaca ulang tiap 20 detik dari sticky intent baterai dan jam
 * sistem; tidak menyentuh state player.
 */
@Composable
private fun SystemStatusIndicator(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var batteryPercent by remember { mutableIntStateOf(-1) }
    var timeLabel by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            val batteryIntent = context.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            batteryPercent = if (level >= 0 && scale > 0) level * 100 / scale else -1
            timeLabel = format.format(Date())
            delay(20_000)
        }
    }

    Row(
        modifier = modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (batteryPercent >= 0) {
            BatteryGlyph(percent = batteryPercent)
            Text(
                text = "$batteryPercent%",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp, end = 8.dp)
            )
        }
        Text(
            text = timeLabel,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/** Baterai horizontal kecil: garis tepi + isi sesuai persen + ujung knob. */
@Composable
private fun BatteryGlyph(percent: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(11.dp)
                .clip(RoundedCornerShape(3.dp))
                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                .padding(1.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent.coerceIn(0, 100) / 100f)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color.White)
            )
        }
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .height(4.dp)
                .background(
                    Color.White.copy(alpha = 0.7f),
                    RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
        )
    }
}
