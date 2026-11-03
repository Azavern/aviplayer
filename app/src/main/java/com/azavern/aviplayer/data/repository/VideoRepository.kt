package com.azavern.aviplayer.data.repository

import com.azavern.aviplayer.data.model.VideoItem
import com.azavern.aviplayer.data.source.FileScannerDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Mengorkestrasi FileScannerDataSource untuk daftar video
 * (Blueprint Bagian 7). Query MediaStore berjalan di Dispatchers.IO.
 */
class VideoRepository @Inject constructor(
    private val fileScanner: FileScannerDataSource
) {

    fun getAllVideos(): Flow<List<VideoItem>> = flow {
        emit(fileScanner.scanAllVideos())
    }.flowOn(Dispatchers.IO)

    fun getVideosByFolder(folderPath: String): Flow<List<VideoItem>> = flow {
        emit(fileScanner.scanVideosByFolder(folderPath))
    }.flowOn(Dispatchers.IO)

    /** Filter dari daftar semua video berdasarkan nama file. */
    fun searchVideos(query: String): Flow<List<VideoItem>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return getAllVideos()
        return getAllVideos().map { videos ->
            videos.filter {
                it.name.contains(trimmed, ignoreCase = true) ||
                    it.fileName.contains(trimmed, ignoreCase = true)
            }
        }
    }
}
