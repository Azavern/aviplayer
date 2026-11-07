package com.azavern.aviplayer.ui.player.components

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
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
import com.azavern.aviplayer.data.model.PlaybackMode
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
 * dirender di sini.
 *
 * Area KIRI ATAS TERKUNCI (perintah Veo 2026-11-04): panah kembali
 * selalu yang pertama dan tidak ada slot yang bisa menempati posisi
 * sebelum/sesudahnya — area ini tetap, hanya gaya judulnya yang
 * bisa diubah (tab Judul). Slot kiri atas sudah tidak dirender di
 * sini; layout lama yang menyimpannya dibersihkan SettingsRepository.
 *
 * KONSEP FIX KANAN ATAS (perintah Veo 2026-11-08), urutan dari
 * pojok ke tengah: (1) kotak baterai + jam — tetap, paling pojok;
 * (2) tombol TITIK TIGA — tetap, bukan slot layout, isinya panel
 * cepat (slider volume + kecerahan dan bar segmentasi mode
 * pemutaran, mengikuti referensi Veo 2026-11-08); (3) slot kanan
 * atas layout, bisa ditukar dari editor (bawaan Bisu).
 *
 * Mode [lockedOnly] (player terkunci): elemen tetap dan slot
 * selain LOCK diganti spacer polos berukuran sama, sehingga bila
 * LOCK kebetulan tinggal di kanan atas ia tetap duduk persis di
 * posisi slotnya sebagai tombol buka kunci. Spacer polos — BUKAN
 * lapisan alpha — karena tumpukan lapisan transparan di dalam
 * animasi bar atas terbukti memicu crash RenderThread (SIGSEGV)
 * di perangkat Veo saat tombol kunci ditekan (logcat 2026-11-08).
 */
@Composable
fun TitleDisplay(
    title: String,
    titleConfig: TitleConfig,
    topRightSlots: List<ControlSlot>,
    uiState: PlayerUiState,
    actions: PlayerControlActions,
    onBackClick: () -> Unit,
    playbackMode: PlaybackMode,
    onPlaybackModeSelected: (PlaybackMode) -> Unit,
    volumeLevel: Float,
    brightnessLevel: Float,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    lockedOnly: Boolean = false
) {
    var modeMenuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            // Margin tepi diperlebar (perintah Veo 2026-11-07): elemen
            // melayang tidak lagi mepet tepi layar.
            .padding(horizontal = 10.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (lockedOnly) {
            // Spacer polos pengganti tombol kembali (jejak 48dp).
            Box(Modifier.size(48.dp))
        } else {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.player_action_back),
                    tint = Color.White
                )
            }
        }
        if (lockedOnly) {
            // Spacer polos pengganti judul; bobot sama supaya slot
            // kanan atas tidak bergeser dari posisi aslinya.
            Box(Modifier.weight(1f))
        } else {
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
        }
        topRightSlots.forEach { slot ->
            if (lockedOnly && slot != ControlSlot.LOCK) {
                ControlSlotSpacer(slot = slot)
            } else if (slot != ControlSlot.EMPTY) {
                ControlSlotButton(slot = slot, uiState = uiState, actions = actions)
            }
        }
        // Titik tiga tetap: jangkar menu mode putar.
        Box {
            if (lockedOnly) {
                // Spacer polos pengganti titik tiga (jejak 48dp).
                Box(Modifier.size(48.dp))
            } else {
                IconButton(onClick = { modeMenuOpen = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(R.string.player_action_playback_mode),
                        tint = Color.White
                    )
                }
            }
            DropdownMenu(
                expanded = modeMenuOpen && !lockedOnly,
                onDismissRequest = { modeMenuOpen = false }
            ) {
                // Panel cepat (referensi Veo 2026-11-08): slider
                // volume & kecerahan supaya bisa diatur tanpa
                // gesture, lalu mode pemutaran sebagai bar
                // segmentasi 4 ikon. Panel tetap terbuka selama
                // diatur; tutup dengan mengetuk di luar panel.
                Column(
                    modifier = Modifier
                        .width(256.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.player_menu_volume),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = volumeLevel.coerceIn(0f, 1f),
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f
                    )
                    Text(
                        text = stringResource(R.string.player_menu_brightness),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Slider(
                        value = brightnessLevel.coerceIn(0f, 1f),
                        onValueChange = onBrightnessChange,
                        valueRange = 0f..1f
                    )
                    Text(
                        text = stringResource(R.string.player_menu_playback_mode),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Urutan simbol dari referensi Veo: Loop All,
                        // Single Loop, Shuffle, Single Play.
                        PLAYBACK_MODE_MENU_ORDER.forEach { mode ->
                            val selected = mode == playbackMode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .clickable { onPlaybackModeSelected(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = playbackModeIcon(mode),
                                    contentDescription = stringResource(
                                        playbackModeLabelRes(mode)
                                    ),
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        // Kotak baterai + jam: tetap, paling pojok kanan atas.
        if (lockedOnly) {
            // Spacer polos pengganti kotak baterai/jam (lebar ±38dp).
            Box(Modifier.width(38.dp))
        } else {
            SystemStatusIndicator()
        }
    }
}

/** Label menu untuk tiap mode putar. */
private fun playbackModeLabelRes(mode: PlaybackMode): Int = when (mode) {
    PlaybackMode.LOOP_ALL -> R.string.playback_mode_loop_all
    PlaybackMode.SHUFFLE -> R.string.playback_mode_shuffle
    PlaybackMode.SINGLE_LOOP -> R.string.playback_mode_single_loop
    PlaybackMode.SINGLE_PLAY -> R.string.playback_mode_single_play
}

/** Ikon bar segmentasi untuk tiap mode putar. */
private fun playbackModeIcon(mode: PlaybackMode) = when (mode) {
    PlaybackMode.LOOP_ALL -> Icons.Rounded.Repeat
    PlaybackMode.SINGLE_LOOP -> Icons.Rounded.RepeatOne
    PlaybackMode.SHUFFLE -> Icons.Rounded.Shuffle
    PlaybackMode.SINGLE_PLAY -> Icons.Rounded.SkipNext
}

/** Urutan ikon mode di panel titik tiga, persis referensi Veo 2026-11-08. */
private val PLAYBACK_MODE_MENU_ORDER = listOf(
    PlaybackMode.LOOP_ALL,
    PlaybackMode.SINGLE_LOOP,
    PlaybackMode.SHUFFLE,
    PlaybackMode.SINGLE_PLAY
)

/**
 * Kotak baterai + jam dinding untuk pojok kanan atas player
 * (konsep fix Veo 2026-11-08): SATU kotak bertumpuk — glyph
 * baterai di atas TANPA angka persen, jam 24 jam di bawahnya.
 * Murni tampilan: dibaca ulang tiap 20 detik dari sticky intent
 * baterai dan jam sistem; tidak menyentuh state player.
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

    Column(
        modifier = modifier.padding(start = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (batteryPercent >= 0) {
            BatteryGlyph(percent = batteryPercent)
        }
        Text(
            text = timeLabel,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 2.dp)
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
