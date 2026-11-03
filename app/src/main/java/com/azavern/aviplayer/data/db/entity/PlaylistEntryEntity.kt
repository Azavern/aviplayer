package com.azavern.aviplayer.data.db.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Baris tabel `playlist_entries` (Blueprint Bagian 9): satu video di
 * dalam playlist, dengan urutan tampil [orderIndex]. Menghapus
 * playlist menghapus semua entrinya (ON DELETE CASCADE).
 */
@Entity(
    tableName = "playlist_entries",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playlistId"])]
)
data class PlaylistEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val videoPath: String,
    val videoTitle: String,
    val durationMs: Long,
    val orderIndex: Int
)
