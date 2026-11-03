package com.azavern.aviplayer.data.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Baris tabel `playlists` (Blueprint Bagian 9): satu playlist manual
 * buatan pengguna (Milestone 10).
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long
)
