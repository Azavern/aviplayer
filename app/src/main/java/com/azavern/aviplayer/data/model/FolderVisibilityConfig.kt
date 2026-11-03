package com.azavern.aviplayer.data.model

/** Mode penyaringan tab Folder (Blueprint Bagian 8). */
enum class FolderFilterMode { ALL, WHITELIST_ONLY }

/**
 * Konfigurasi visibilitas folder (Blueprint Bagian 8 & Milestone 9).
 * [whitelistedPaths] berisi path folder persis seperti dari
 * MediaStore; pencocokan di FolderRepository memakai
 * FileUtils.normalizePath di kedua sisi.
 */
data class FolderVisibilityConfig(
    val mode: FolderFilterMode = FolderFilterMode.ALL,
    val whitelistedPaths: Set<String> = emptySet()
) {
    companion object {
        val Default = FolderVisibilityConfig()
    }
}
