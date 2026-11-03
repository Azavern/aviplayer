package com.azavern.aviplayer.data.model

import kotlinx.serialization.Serializable

/**
 * Tata letak slot tombol player (Blueprint Bagian 8), disimpan
 * sebagai JSON di DataStore (key CONTROL_LAYOUT_JSON).
 *
 * Nilai bawaannya mengikuti layout player yang sudah terverifikasi
 * Veo (referensi 2026-11-04 + koreksinya): klaster tengah
 * prev/play/next, dan di kanan bawah kamera–kecepatan–rotasi dengan
 * rotasi paling pojok. Ini MENGGANTIKAN susunan bawaan harfiah
 * blueprint (yang menaruh rotasi & kecepatan di bar atas) karena
 * keputusan pengguna lebih tinggi dari dokumen.
 */
@Serializable
data class ControlLayoutConfig(
    val topLeftSlot: ControlSlot = ControlSlot.EMPTY,
    val topRightSlots: List<ControlSlot> = emptyList(),
    val centerSlots: List<ControlSlot> = listOf(
        ControlSlot.PREVIOUS, ControlSlot.PLAY_PAUSE, ControlSlot.NEXT
    ),
    val bottomLeftSlots: List<ControlSlot> = emptyList(),
    val bottomRightSlots: List<ControlSlot> = listOf(
        ControlSlot.CAPTURE, ControlSlot.SPEED, ControlSlot.ORIENTATION
    )
) {
    companion object {
        val Default = ControlLayoutConfig()
    }
}
