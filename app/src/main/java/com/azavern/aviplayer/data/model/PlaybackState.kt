package com.azavern.aviplayer.data.model

/**
 * Posisi terakhir pemutaran satu video (Blueprint Bagian 8 & 9,
 * tabel `playback_state`). Bentuk domain dari PlaybackStateEntity.
 */
data class PlaybackState(
    val videoId: String,        // FileUtils.generateVideoId(path)
    val videoPath: String,      // full path absolut
    val positionMs: Long,       // posisi terakhir dalam milliseconds
    val updatedAt: Long         // System.currentTimeMillis() saat disimpan
)
