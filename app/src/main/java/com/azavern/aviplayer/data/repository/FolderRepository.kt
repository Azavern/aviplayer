package com.azavern.aviplayer.data.repository

import com.azavern.aviplayer.data.model.FolderFilterMode
import com.azavern.aviplayer.data.model.FolderItem
import com.azavern.aviplayer.data.source.FileScannerDataSource
import com.azavern.aviplayer.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Daftar folder untuk tab Folder (Blueprint Bagian 7).
 *
 * Filter whitelist diterapkan DI SINI (Milestone 9), bukan di
 * ViewModel atau UI: [getVisibleFolders] menggabungkan hasil scan
 * dengan FolderVisibilityConfig dari SettingsRepository. Mode
 * WHITELIST_ONLY hanya meloloskan folder yang path-nya (sesudah
 * normalizePath di kedua sisi) ada di daftar whitelist; setiap
 * folder ditandai isWhitelisted sesuai config.
 */
class FolderRepository @Inject constructor(
    private val fileScanner: FileScannerDataSource,
    private val settingsRepository: SettingsRepository
) {

    /** Semua folder hasil scan, tanpa filter (untuk editor whitelist). */
    fun getAllFolders(): Flow<List<FolderItem>> = flow {
        emit(fileScanner.scanAllFolders())
    }.flowOn(Dispatchers.IO)

    /** Folder yang boleh tampil di tab Folder, sesudah filter whitelist. */
    fun getVisibleFolders(): Flow<List<FolderItem>> = combine(
        getAllFolders(),
        settingsRepository.getFolderVisibilityConfig()
    ) { folders, config ->
        val normalizedWhitelist = config.whitelistedPaths
            .map { FileUtils.normalizePath(it) }
            .toSet()
        val tagged = folders.map { folder ->
            folder.copy(
                isWhitelisted = FileUtils.normalizePath(folder.path) in normalizedWhitelist
            )
        }
        when (config.mode) {
            FolderFilterMode.ALL -> tagged
            FolderFilterMode.WHITELIST_ONLY -> tagged.filter { it.isWhitelisted }
        }
    }
}
