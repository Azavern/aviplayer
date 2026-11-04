package com.azavern.aviplayer.data.repository

import com.azavern.aviplayer.data.db.dao.PlaylistDao
import com.azavern.aviplayer.data.db.dao.PlaylistEntryDao
import com.azavern.aviplayer.data.db.entity.PlaylistEntity
import com.azavern.aviplayer.data.db.entity.PlaylistEntryEntity
import com.azavern.aviplayer.data.model.PlaylistEntry
import com.azavern.aviplayer.data.model.PlaylistInfo
import com.azavern.aviplayer.data.source.FileScannerDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Manajemen playlist manual (Blueprint Bagian 7 & Milestone 10).
 * Judul dan durasi entri diambil dari MediaStore saat video
 * ditambahkan (lewat FileScannerDataSource), supaya daftar tetap
 * informatif walau file video kelak dipindah/dihapus.
 */
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val entryDao: PlaylistEntryDao,
    private val fileScanner: FileScannerDataSource
) {

    suspend fun createPlaylist(name: String): Long = playlistDao.insert(
        PlaylistEntity(name = name, createdAt = System.currentTimeMillis())
    )

    suspend fun deletePlaylist(id: Long) = playlistDao.deleteById(id)

    suspend fun renamePlaylist(id: Long, newName: String) =
        playlistDao.rename(id, newName)

    /** Semua playlist + ringkasannya (jumlah video, total durasi, maks 3 thumbnail). */
    fun getAllPlaylists(): Flow<List<PlaylistInfo>> = combine(
        playlistDao.getAll(),
        entryDao.getAll()
    ) { playlists, entries ->
        val byPlaylist = entries.groupBy { it.playlistId }
        playlists.map { playlist ->
            val own = byPlaylist[playlist.id].orEmpty()
            PlaylistInfo(
                id = playlist.id,
                name = playlist.name,
                videoCount = own.size,
                totalDurationMs = own.sumOf { it.durationMs },
                createdAt = playlist.createdAt,
                thumbnailPaths = own.take(3).map { it.videoPath }
            )
        }
    }

    /**
     * Tambah video ke akhir playlist. Judul/durasi dibaca dari
     * MediaStore; bila video tidak ditemukan (mis. izin dicabut),
     * judul diturunkan dari nama file dan durasi 0.
     */
    suspend fun addVideoToPlaylist(playlistId: Long, videoPath: String) {
        val video = withContext(Dispatchers.IO) { fileScanner.getVideoByPath(videoPath) }
        val currentCount = countEntries(playlistId)
        entryDao.insert(
            PlaylistEntryEntity(
                playlistId = playlistId,
                videoPath = videoPath,
                videoTitle = video?.name
                    ?: videoPath.substringAfterLast('/').substringBeforeLast('.'),
                durationMs = video?.durationMs ?: 0L,
                orderIndex = currentCount
            )
        )
    }

    suspend fun removeVideoFromPlaylist(entryId: Long) = entryDao.deleteById(entryId)

    /** Keluarkan file ini dari semua playlist (file dihapus dari perangkat). */
    suspend fun removeAllEntriesForVideo(videoPath: String) =
        entryDao.deleteByVideoPath(videoPath)

    /** File diganti nama → semua entri yang menunjuknya mengikuti. */
    suspend fun updateEntriesForRenamedVideo(oldPath: String, newPath: String, newTitle: String) =
        entryDao.updateVideoPath(oldPath, newPath, newTitle)

    fun getPlaylistEntries(playlistId: Long): Flow<List<PlaylistEntry>> =
        entryDao.getByPlaylist(playlistId).map { entries ->
            entries.map { it.toDomain() }
        }

    /** Simpan urutan baru (urutannya = urutan list yang diberikan). */
    suspend fun reorderEntries(entries: List<PlaylistEntry>) {
        entryDao.reorder(
            entries.mapIndexed { index, entry ->
                PlaylistEntryEntity(
                    id = entry.id,
                    playlistId = entry.playlistId,
                    videoPath = entry.videoPath,
                    videoTitle = entry.videoTitle,
                    durationMs = entry.durationMs,
                    orderIndex = index
                )
            }
        )
    }

    /** Jumlah entri saat ini — emisi pertama Flow DAO sudah cukup. */
    private suspend fun countEntries(playlistId: Long): Int =
        entryDao.getByPlaylist(playlistId).first().size

    private fun PlaylistEntryEntity.toDomain(): PlaylistEntry = PlaylistEntry(
        id = id,
        playlistId = playlistId,
        videoPath = videoPath,
        videoTitle = videoTitle,
        durationMs = durationMs,
        orderIndex = orderIndex
    )
}
