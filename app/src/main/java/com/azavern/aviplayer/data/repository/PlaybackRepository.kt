package com.azavern.aviplayer.data.repository

import com.azavern.aviplayer.data.db.dao.PlaybackStateDao
import com.azavern.aviplayer.data.db.entity.PlaybackStateEntity
import com.azavern.aviplayer.data.model.PlaybackState
import javax.inject.Inject

/**
 * Menyimpan dan mengambil posisi terakhir pemutaran per video
 * (Blueprint Bagian 7). Satu-satunya pintu UI/ViewModel ke
 * PlaybackStateDao.
 */
class PlaybackRepository @Inject constructor(
    private val playbackStateDao: PlaybackStateDao
) {

    /** Simpan (atau timpa) posisi terakhir satu video. */
    suspend fun savePosition(videoId: String, videoPath: String, positionMs: Long) {
        playbackStateDao.upsert(
            PlaybackStateEntity(
                videoId = videoId,
                videoPath = videoPath,
                positionMs = positionMs.coerceAtLeast(0L),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /** Posisi terakhir video ini, atau 0 bila belum pernah ditonton. */
    suspend fun getLastPosition(videoId: String): Long {
        return playbackStateDao.getByVideoId(videoId)?.positionMs ?: 0L
    }

    /** Semua posisi tersimpan sebagai peta videoId → positionMs (bar progres daftar). */
    suspend fun getAllPositions(): Map<String, Long> {
        return playbackStateDao.getAll().associate { it.videoId to it.positionMs }
    }

    /** Video yang paling terakhir diputar, atau null bila belum ada riwayat. */
    suspend fun getLastPlayed(): PlaybackState? {
        return playbackStateDao.getMostRecent()?.let { entity ->
            PlaybackState(
                videoId = entity.videoId,
                videoPath = entity.videoPath,
                positionMs = entity.positionMs,
                updatedAt = entity.updatedAt
            )
        }
    }

    /** Hapus semua riwayat posisi. */
    suspend fun clearAllPositions() {
        playbackStateDao.deleteAll()
    }
}
