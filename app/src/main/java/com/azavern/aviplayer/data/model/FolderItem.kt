package com.azavern.aviplayer.data.model

/**
 * Satu folder berisi video (Blueprint Bagian 8). Dibangun oleh
 * FileScannerDataSource dari pengelompokan MediaStore per folder induk.
 */
data class FolderItem(
    val path: String,               // full path folder
    val name: String,               // BUCKET_DISPLAY_NAME dari MediaStore
    val videoCount: Int,            // jumlah video dalam folder
    val thumbnailPath: String?,     // path video representatif untuk thumbnail
    val isWhitelisted: Boolean = false
)
