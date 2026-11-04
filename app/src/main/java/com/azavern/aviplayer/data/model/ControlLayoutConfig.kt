package com.azavern.aviplayer.data.model

import kotlinx.serialization.Serializable

/**
 * Tata letak slot tombol player (Blueprint Bagian 8), disimpan
 * sebagai JSON di DataStore (key CONTROL_LAYOUT_JSON).
 *
 * Nilai bawaannya mengikuti layout player yang sudah terverifikasi
 * Veo (referensi 2026-11-04 + koreksinya): klaster tengah
 * prev/play/next, di kanan bawah kamera–kecepatan–rotasi dengan
 * rotasi paling pojok, dan bisu di kiri bawah. Ini MENGGANTIKAN
 * susunan bawaan harfiah blueprint (yang menaruh rotasi &
 * kecepatan di bar atas) karena keputusan pengguna lebih tinggi
 * dari dokumen.
 *
 * Area KIRI ATAS TERKUNCI (perintah Veo 2026-11-04): tombol kembali
 * + judul bersifat tetap dan tidak bisa diganggu gugat dari editor.
 * Field [topLeftSlot] dipertahankan hanya demi kompatibilitas JSON
 * lama — isinya selalu dikosongkan oleh [sanitized], dan tombol
 * yang terlanjur tersimpan di sana dipindah ke kanan bawah.
 */
@Serializable
data class ControlLayoutConfig(
    val topLeftSlot: ControlSlot = ControlSlot.EMPTY,
    val topRightSlots: List<ControlSlot> = emptyList(),
    val centerSlots: List<ControlSlot> = listOf(
        ControlSlot.PREVIOUS, ControlSlot.PLAY_PAUSE, ControlSlot.NEXT
    ),
    val bottomLeftSlots: List<ControlSlot> = listOf(ControlSlot.MUTE),
    val bottomRightSlots: List<ControlSlot> = listOf(
        ControlSlot.CAPTURE, ControlSlot.SPEED, ControlSlot.ORIENTATION
    )
) {

    /**
     * Paksa area kiri atas kosong: tombol yang terlanjur tersimpan
     * di sana (dari layout sebelum area ini dikunci) dipindah ke
     * akhir area kanan bawah, kecuali tombolnya sudah ada di area
     * lain. LOCK/SCREEN_RATIO yang ikut tersimpan dibuang — keduanya
     * memang tidak pernah dirender.
     */
    fun sanitized(): ControlLayoutConfig {
        val stray = topLeftSlot
        var result = copy(topLeftSlot = ControlSlot.EMPTY)
        if (stray != ControlSlot.EMPTY &&
            stray != ControlSlot.LOCK && stray != ControlSlot.SCREEN_RATIO
        ) {
            val placed = result.topRightSlots + result.centerSlots +
                result.bottomLeftSlots + result.bottomRightSlots
            if (stray !in placed) {
                result = result.copy(bottomRightSlots = result.bottomRightSlots + stray)
            }
        }
        return result
    }

    companion object {
        val Default = ControlLayoutConfig()
    }
}
