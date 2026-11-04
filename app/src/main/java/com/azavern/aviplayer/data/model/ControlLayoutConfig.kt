package com.azavern.aviplayer.data.model

import kotlinx.serialization.Serializable

/**
 * Tata letak slot tombol player (Blueprint Bagian 8), disimpan
 * sebagai JSON di DataStore (key CONTROL_LAYOUT_JSON).
 *
 * Ukuran area bawah TETAP (spek Veo 2026-11-04): kiri bawah selalu
 * 3 slot, kanan bawah selalu 2 slot — kekurangan diisi EMPTY.
 * Susunan bawaan:
 *
 * | kiri bawah          | kanan bawah       |
 * |---------------------|-------------------|
 * | Bisu, Kamera, Kosong | Kecepatan, Rotasi |
 *
 * Kecepatan duduk di samping Rotasi, dan Rotasi selalu paling
 * pojok kanan. Klaster tengah tetap prev/play/next. Ini
 * MENGGANTIKAN susunan bawaan harfiah blueprint karena keputusan
 * pengguna lebih tinggi dari dokumen.
 *
 * Area KIRI ATAS TERKUNCI (perintah Veo 2026-11-04): tombol kembali
 * + judul bersifat tetap dan tidak bisa diganggu gugat dari editor.
 * Field [topLeftSlot] dipertahankan hanya demi kompatibilitas JSON
 * lama — isinya selalu dikosongkan oleh [sanitized].
 */
@Serializable
data class ControlLayoutConfig(
    val topLeftSlot: ControlSlot = ControlSlot.EMPTY,
    val topRightSlots: List<ControlSlot> = emptyList(),
    val centerSlots: List<ControlSlot> = listOf(
        ControlSlot.PREVIOUS, ControlSlot.PLAY_PAUSE, ControlSlot.NEXT
    ),
    val bottomLeftSlots: List<ControlSlot> = listOf(
        ControlSlot.MUTE, ControlSlot.CAPTURE, ControlSlot.EMPTY
    ),
    val bottomRightSlots: List<ControlSlot> = listOf(
        ControlSlot.SPEED, ControlSlot.ORIENTATION
    )
) {

    /**
     * Normalisasi layout tersimpan ke aturan berjalan:
     *
     * - Kiri atas selalu kosong; tombol yang terlanjur tersimpan di
     *   sana ikut antrean tombol samping di bawah.
     * - Setiap tombol fungsional muncul TEPAT satu kali di seluruh
     *   layout (duplikat dibuang, kemunculan pertama yang menang).
     *   Tombol yang belum ada di layout lama — mis. MUTE untuk
     *   layout yang disimpan sebelum tombol itu lahir — ditambahkan
     *   ke antrean samping, supaya selalu bisa dicapai editor
     *   (editor bekerja dengan tukar tempat, himpunan tombolnya
     *   memang selalu lengkap).
     * - Kanan bawah (maks [BOTTOM_RIGHT_SIZE]): Kecepatan dan Rotasi
     *   diprioritaskan — Kecepatan di samping Rotasi, Rotasi paling
     *   pojok; sisa tempat diisi antrean samping berurutan.
     * - Kiri bawah (maks [BOTTOM_LEFT_SIZE]): sisa antrean samping.
     *   Kelebihan di luar kedua area (tidak terjadi pada himpunan
     *   tombol sekarang) dipindah ke kanan atas, tidak dibuang.
     * - Kedua area bawah diisi EMPTY sampai ukurannya tetap; kanan
     *   bawah yang isinya cuma Rotasi mengisi EMPTY di DEPAN supaya
     *   Rotasi tetap di pojok.
     * - LOCK/SCREEN_RATIO dibuang — keduanya memang tidak dirender.
     */
    fun sanitized(): ControlLayoutConfig {
        val functional = listOf(
            ControlSlot.PLAY_PAUSE, ControlSlot.PREVIOUS, ControlSlot.NEXT,
            ControlSlot.ORIENTATION, ControlSlot.CAPTURE, ControlSlot.SPEED,
            ControlSlot.MUTE
        )
        val seen = mutableSetOf<ControlSlot>()
        fun take(slots: List<ControlSlot>): List<ControlSlot> =
            slots.filter { it in functional }.filter { seen.add(it) }

        val center = take(centerSlots)
        val topRight = take(topRightSlots).toMutableList()
        val sidePool = take(bottomLeftSlots) + take(bottomRightSlots) +
            take(listOf(topLeftSlot))
        // Lengkapi himpunan: tombol fungsional yang belum muncul di mana pun.
        val remaining = (sidePool + functional.filter { it !in seen }).toMutableList()

        val right = mutableListOf<ControlSlot>()
        if (ControlSlot.SPEED in remaining) {
            right += ControlSlot.SPEED
            remaining.remove(ControlSlot.SPEED)
        }
        if (ControlSlot.ORIENTATION in remaining) {
            right += ControlSlot.ORIENTATION
            remaining.remove(ControlSlot.ORIENTATION)
        }
        while (right.size < BOTTOM_RIGHT_SIZE && remaining.isNotEmpty()) {
            right += remaining.removeAt(0)
        }
        val left = mutableListOf<ControlSlot>()
        while (left.size < BOTTOM_LEFT_SIZE && remaining.isNotEmpty()) {
            left += remaining.removeAt(0)
        }
        topRight += remaining

        val rightPadded = when {
            right.size >= BOTTOM_RIGHT_SIZE -> right
            right.size == 1 && right[0] == ControlSlot.ORIENTATION ->
                listOf(ControlSlot.EMPTY) + right
            else -> right + List(BOTTOM_RIGHT_SIZE - right.size) { ControlSlot.EMPTY }
        }
        val leftPadded = left + List(BOTTOM_LEFT_SIZE - left.size) { ControlSlot.EMPTY }

        return copy(
            topLeftSlot = ControlSlot.EMPTY,
            topRightSlots = topRight,
            centerSlots = center,
            bottomLeftSlots = leftPadded,
            bottomRightSlots = rightPadded
        )
    }

    companion object {
        val Default = ControlLayoutConfig()

        /** Jumlah slot tetap area kiri bawah (spek Veo 2026-11-04). */
        const val BOTTOM_LEFT_SIZE = 3

        /** Jumlah slot tetap area kanan bawah (spek Veo 2026-11-04). */
        const val BOTTOM_RIGHT_SIZE = 2
    }
}
