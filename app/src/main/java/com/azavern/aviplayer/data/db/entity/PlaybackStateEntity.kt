package com.azavern.aviplayer.data.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Baris tabel `playback_state` (Blueprint Bagian 7 & 9):
 * satu baris per video, di-upsert setiap posisi disimpan.
 */
@Entity(tableName = "playback_state")
data class PlaybackStateEntity(
    @PrimaryKey val videoId: String,
    val videoPath: String,
    val positionMs: Long,
    val updatedAt: Long
)
