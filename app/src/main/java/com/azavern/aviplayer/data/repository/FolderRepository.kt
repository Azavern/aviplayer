package com.azavern.aviplayer.data.repository

import com.azavern.aviplayer.data.model.FolderItem
import com.azavern.aviplayer.data.source.FileScannerDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Daftar folder untuk tab Folder (Blueprint Bagian 7).
 * Milestone 3: semua folder dikembalikan apa adanya — filter
 * whitelist (FolderVisibilityConfig) diterapkan di sini pada
 * milestone Kustomisasi, bukan di ViewModel/UI.
 */
class FolderRepository @Inject constructor(
    private val fileScanner: FileScannerDataSource
) {

    fun getVisibleFolders(): Flow<List<FolderItem>> = flow {
        emit(fileScanner.scanAllFolders())
    }.flowOn(Dispatchers.IO)
}
