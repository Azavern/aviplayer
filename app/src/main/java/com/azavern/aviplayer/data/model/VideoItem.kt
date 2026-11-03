package com.azavern.aviplayer.data.model

/**
 * Satu video lokal (Blueprint Bagian 8). Diisi dari MediaStore oleh
 * FileScannerDataSource (Milestone 3); di Milestone 1 modelnya dibuat
 * lebih dulu karena menjadi tipe dasar alur player.
 */
data class VideoItem(
    val id: String,              // FileUtils.generateVideoId(path)
    val name: String,            // nama file tanpa ekstensi (untuk display)
    val fileName: String,        // nama file lengkap dengan ekstensi
    val path: String,            // full path absolut
    val durationMs: Long,        // durasi dalam milliseconds
    val sizeBytes: Long,         // ukuran file dalam bytes
    val folderName: String,      // BUCKET_DISPLAY_NAME dari MediaStore
    val folderPath: String,      // path folder induk
    val dateAdded: Long,         // timestamp dari MediaStore
    val thumbnailPath: String?   // path untuk load thumbnail via Coil (nullable)
)
