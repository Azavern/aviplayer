package com.azavern.aviplayer.data.model

/**
 * Model playlist (Blueprint Bagian 8). Dipakai HomeUiState sejak
 * Milestone 3 (daftarnya masih kosong); CRUD playlist dibangun pada
 * milestone Playlist.
 */
data class PlaylistInfo(
    val id: Long,
    val name: String,
    val videoCount: Int,
    val totalDurationMs: Long,
    val createdAt: Long,
    val thumbnailPaths: List<String>    // maks 3, untuk strip thumbnail
)

data class PlaylistEntry(
    val id: Long,
    val playlistId: Long,
    val videoPath: String,
    val videoTitle: String,
    val durationMs: Long,
    val orderIndex: Int
)
