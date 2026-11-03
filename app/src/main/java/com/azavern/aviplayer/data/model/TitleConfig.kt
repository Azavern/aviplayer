package com.azavern.aviplayer.data.model

/**
 * Gaya judul video di bar atas player (Blueprint Bagian 8).
 * Disimpan sebagai key terpisah di DataStore (lihat PreferenceDataSource).
 */
data class TitleConfig(
    /** Ukuran font judul; rentang 10f–22f. */
    val fontSizeSp: Float = 14f,
    /** Jumlah baris maksimum judul: 1 atau 2. */
    val maxLines: Int = 1,
    /** Warna judul sebagai ARGB Long; bawaan putih. */
    val color: Long = 0xFFFFFFFF,
    val isBold: Boolean = false,
    /** Judul panjang berjalan (marquee) bila true dan muat satu baris. */
    val marqueeEnabled: Boolean = true
) {
    companion object {
        val Default = TitleConfig()
    }
}
