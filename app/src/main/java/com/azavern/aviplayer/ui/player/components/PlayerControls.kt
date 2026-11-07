package com.azavern.aviplayer.ui.player.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.data.model.ControlLayoutConfig
import com.azavern.aviplayer.data.model.ControlSlot
import com.azavern.aviplayer.ui.player.PlayerUiState

/**
 * Baris kontrol bawah player — dirender dari [ControlLayoutConfig]
 * (Milestone 7): tiga area dalam satu baris, kiri [bottomLeftSlots],
 * tengah [centerSlots], kanan [bottomRightSlots]. Slot EMPTY dan
 * slot yang fiturnya belum ada tidak digambar.
 *
 * Susunan bawaan: tengah = sebelumnya / putar-jeda besar /
 * berikutnya; kiri bawah (tetap 2 slot) = kunci / kamera (LOCK
 * pindah dari kiri tengah, konsep fix Veo 2026-11-08); kanan bawah
 * (tetap 2 slot) = kecepatan / rotasi, kecepatan di samping rotasi
 * dan rotasi paling pojok. Pengguna bisa menukar posisi tombol
 * lewat layar Kustomisasi.
 *
 * Mode [lockedOnly] (player terkunci): semua slot selain LOCK
 * digambar sebagai spacer tak terlihat berukuran sama, sehingga
 * tombol LOCK tetap duduk persis di posisi slotnya — yang
 * menghilang hanya tombol-tombol di sekelilingnya.
 */
@Composable
fun PlayerControls(
    layout: ControlLayoutConfig,
    uiState: PlayerUiState,
    actions: PlayerControlActions,
    modifier: Modifier = Modifier,
    lockedOnly: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Margin tepi diperlebar (perintah Veo 2026-11-07),
            // termasuk napas ekstra di tepi bawah layar.
            .padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            layout.bottomLeftSlots.forEach { slot ->
                if (lockedOnly && slot != ControlSlot.LOCK) {
                    ControlSlotSpacer(slot = slot)
                } else {
                    ControlSlotButton(slot = slot, uiState = uiState, actions = actions)
                }
            }
        }
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            layout.centerSlots.forEach { slot ->
                if (lockedOnly && slot != ControlSlot.LOCK) {
                    ControlSlotSpacer(slot = slot, prominent = slot == ControlSlot.PLAY_PAUSE)
                } else {
                    ControlSlotButton(
                        slot = slot,
                        uiState = uiState,
                        actions = actions,
                        prominent = slot == ControlSlot.PLAY_PAUSE
                    )
                }
            }
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            layout.bottomRightSlots.forEach { slot ->
                if (lockedOnly && slot != ControlSlot.LOCK) {
                    ControlSlotSpacer(slot = slot)
                } else {
                    ControlSlotButton(slot = slot, uiState = uiState, actions = actions)
                }
            }
        }
    }
}
