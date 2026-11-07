package com.azavern.aviplayer.data.model

/**
 * Mode putar antrean (fitur Veo 2026-11-07), dipilih dari menu
 * tombol titik tiga tetap di pojok kanan atas player.
 *
 * Mode hidup di state PlayerViewModel selama sesi player; belum
 * disimpan ke preferensi (menyusul bila Veo menghendaki — pola yang
 * sama seperti mode orientasi).
 */
enum class PlaybackMode {
    /** Berurutan; sesudah video terakhir, kembali ke video pertama. Ini bawaan. */
    LOOP_ALL,

    /** Berikutnya dipilih acak dari antrean (selain video yang sedang diputar). */
    SHUFFLE,

    /** Video yang sama diulang terus-menerus (repeat bawaan ExoPlayer). */
    SINGLE_LOOP,

    /** Player ditutup sesudah video berakhir — tidak ada lompatan ke video lain. */
    SINGLE_PLAY
}
