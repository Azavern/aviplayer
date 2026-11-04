package com.azavern.aviplayer.data.repository

import android.content.IntentSender
import com.azavern.aviplayer.data.model.SortConfig
import com.azavern.aviplayer.data.model.SortField
import com.azavern.aviplayer.data.model.VideoItem
import com.azavern.aviplayer.data.source.FileScannerDataSource
import com.azavern.aviplayer.data.source.MediaWriteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Mengorkestrasi FileScannerDataSource untuk daftar video
 * (Blueprint Bagian 7). Query MediaStore berjalan di Dispatchers.IO.
 *
 * Sorting global diterapkan DI SINI (permintaan Veo 2026-11-04):
 * semua daftar keluar dari repository ini sudah terurut mengikuti
 * SortConfig dari SettingsRepository — Beranda, isi folder, dan
 * hasil pencarian sama-sama mengalir lewat jalur ini. Antrean
 * prev/next player, yang mengikuti daftar yang ditampilkan, ikut
 * terurut dengan sendirinya.
 */
class VideoRepository @Inject constructor(
    private val fileScanner: FileScannerDataSource,
    private val settingsRepository: SettingsRepository
) {

    fun getAllVideos(): Flow<List<VideoItem>> = flow {
        emit(fileScanner.scanAllVideos())
    }.flowOn(Dispatchers.IO).applySort()

    fun getVideosByFolder(folderPath: String): Flow<List<VideoItem>> = flow {
        emit(fileScanner.scanVideosByFolder(folderPath))
    }.flowOn(Dispatchers.IO).applySort()

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

    /** Ganti nama file di perangkat (lihat FileScannerDataSource.renameVideo). */
    suspend fun renameVideo(path: String, newDisplayName: String): MediaWriteResult =
        withContext(Dispatchers.IO) { fileScanner.renameVideo(path, newDisplayName) }

    /** Hapus file dari perangkat (lihat FileScannerDataSource.deleteVideo). */
    suspend fun deleteVideo(path: String): MediaWriteResult =
        withContext(Dispatchers.IO) { fileScanner.deleteVideo(path) }

    /** Satu dialog persetujuan sistem untuk banyak file sekaligus (Android 11+). */
    suspend fun createWriteConsentSender(paths: List<String>): IntentSender? =
        withContext(Dispatchers.IO) { fileScanner.createWriteConsentSender(paths) }

    /** Gabungkan daftar mentah dengan SortConfig aktif, lalu urutkan. */
    private fun Flow<List<VideoItem>>.applySort(): Flow<List<VideoItem>> =
        combine(settingsRepository.getSortConfig()) { videos, config ->
            videos.sortedWith(comparatorFor(config))
        }

    private fun comparatorFor(config: SortConfig): Comparator<VideoItem> {
        val base: Comparator<VideoItem> = when (config.field) {
            SortField.NAME -> compareBy { it.name.lowercase() }
            SortField.TIME -> compareBy { it.dateAdded }
            SortField.SIZE -> compareBy { it.sizeBytes }
            SortField.DURATION -> compareBy { it.durationMs }
        }
        // Pemutus seri: nama, supaya urutan stabil dan terprediksi
        // (mis. banyak video berdurasi sama).
        val withTieBreak = base.thenBy { it.name.lowercase() }
        return if (config.ascending) withTieBreak else withTieBreak.reversed()
    }
}
