package com.azavern.aviplayer.data.repository

import com.azavern.aviplayer.data.db.dao.PlaybackStateDao
import com.azavern.aviplayer.data.db.entity.PlaybackStateEntity
import com.azavern.aviplayer.data.model.PlaybackState
import com.azavern.aviplayer.utils.Constants
import javax.inject.Inject

/**
 * Menyimpan dan mengambil posisi terakhir pemutaran per video
 * (Blueprint Bagian 7). Satu-satunya pintu UI/ViewModel ke
 * PlaybackStateDao.
 */
class PlaybackRepository @Inject constructor(
    private val playbackStateDao: PlaybackStateDao
) {

    /**
     * Saring posisi menjadi titik resume yang sah (spek Veo
     * 2026-11-07): posisi ≤ 2 detik (+ toleransi 0,1) dianggap belum
     * pernah diputar → 0; posisi di dalam 3 detik terakhir
     * (+ toleransi 0,1) dianggap sudah selesai → 0. Di luar itu
     * posisi lolos apa adanya. [durationMs] ≤ 0 berarti durasi tidak
     * diketahui: hanya batas bawah yang bisa dinilai.
     *
     * Berlaku dua arah: saat MENYIMPAN (posisi beracun tidak pernah
     * masuk database) dan saat MEMBACA untuk resume (baris lama yang
     * terlanjur beracun — mis. berhenti di 1 detik terakhir — sembuh
     * sendiri pada pemutaran berikutnya).
     */
    fun sanitizeResumePosition(positionMs: Long, durationMs: Long): Long {
        val position = positionMs.coerceAtLeast(0L)
        if (position <= Constants.RESUME_MIN_POSITION_MS + Constants.RESUME_TOLERANCE_MS) {
            return 0L
        }
        if (durationMs > 0L &&
            position >= durationMs - Constants.RESUME_END_MARGIN_MS - Constants.RESUME_TOLERANCE_MS
        ) {
            return 0L
        }
        return position
    }

    /**
     * Simpan (atau timpa) posisi terakhir satu video. Posisi selalu
     * melewati [sanitizeResumePosition] dulu; [durationMs] adalah
     * durasi video yang posisinya disimpan (0 bila tidak diketahui).
     */
    suspend fun savePosition(
        videoId: String,
        videoPath: String,
        positionMs: Long,
        durationMs: Long = 0L
    ) {
        playbackStateDao.upsert(
            PlaybackStateEntity(
                videoId = videoId,
                videoPath = videoPath,
                positionMs = sanitizeResumePosition(positionMs, durationMs),
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

    /** Hapus riwayat posisi satu video (mis. file-nya dihapus dari perangkat). */
    suspend fun deletePosition(videoId: String) {
        playbackStateDao.deleteByVideoId(videoId)
    }

    /** Hapus semua riwayat posisi. */
    suspend fun clearAllPositions() {
        playbackStateDao.deleteAll()
    }
}
