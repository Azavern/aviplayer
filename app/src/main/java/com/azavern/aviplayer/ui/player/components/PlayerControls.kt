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
 * berikutnya; kiri bawah (tetap 2 slot, koreksi Veo 2026-11-07) =
 * bisu / kamera; kanan bawah (tetap 2 slot) = kecepatan / rotasi,
 * kecepatan di samping rotasi dan rotasi paling pojok. Pengguna
 * bisa menukar posisi tombol lewat layar Kustomisasi.
 */
@Composable
fun PlayerControls(
    layout: ControlLayoutConfig,
    uiState: PlayerUiState,
    actions: PlayerControlActions,
    modifier: Modifier = Modifier
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
                ControlSlotButton(slot = slot, uiState = uiState, actions = actions)
            }
        }
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            layout.centerSlots.forEach { slot ->
                ControlSlotButton(
                    slot = slot,
                    uiState = uiState,
                    actions = actions,
                    prominent = slot == ControlSlot.PLAY_PAUSE
                )
            }
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            layout.bottomRightSlots.forEach { slot ->
                ControlSlotButton(slot = slot, uiState = uiState, actions = actions)
            }
        }
    }
}
