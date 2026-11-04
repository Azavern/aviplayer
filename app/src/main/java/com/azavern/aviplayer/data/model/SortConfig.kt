package com.azavern.aviplayer.data.model

/**
 * Bidang urutan daftar video (sorting global, permintaan Veo
 * 2026-11-04). Setiap bidang punya arah bawaan yang dipakai saat
 * bidang itu baru dipilih dari menu:
 *
 * - NAME     → naik (A–Z)
 * - TIME     → turun (terbaru dulu — perilaku aplikasi sejak awal)
 * - SIZE     → turun (terbesar dulu; keputusan Ryn yang Veo setujui:
 *              mengurutkan ukuran biasanya untuk memburu file besar)
 * - DURATION → turun (terpanjang dulu, alasan yang sama)
 *
 * Sesudah itu arah bebas dibalik pengguna lewat tombol naik/turun.
 */
enum class SortField(val defaultAscending: Boolean) {
    NAME(defaultAscending = true),
    TIME(defaultAscending = false),
    SIZE(defaultAscending = false),
    DURATION(defaultAscending = false)
}

/**
 * Konfigurasi urutan yang sedang berlaku. Berlaku global: daftar
 * Beranda, isi folder, dan hasil pencarian — dan karena antrean
 * prev/next player mengikuti daftar yang ditampilkan, urutan putar
 * ikut berubah (disetujui Veo). Urutan manual playlist TIDAK
 * terpengaruh.
 */
data class SortConfig(
    val field: SortField = SortField.TIME,
    val ascending: Boolean = false
) {
    companion object {
        /** Sama dengan perilaku sebelum fitur ini ada: terbaru dulu. */
        val Default = SortConfig()

        /** Config saat pengguna memilih bidang baru dari menu urutan. */
        fun forField(field: SortField): SortConfig =
            SortConfig(field = field, ascending = field.defaultAscending)
    }
}
