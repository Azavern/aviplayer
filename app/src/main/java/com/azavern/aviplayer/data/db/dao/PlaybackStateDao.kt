package com.azavern.aviplayer.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.azavern.aviplayer.data.db.entity.PlaybackStateEntity

/**
 * DAO posisi pemutaran (Blueprint Bagian 7).
 * Room 3: semua fungsi non-Flow wajib `suspend`.
 */
@Dao
interface PlaybackStateDao {

    @Upsert
    suspend fun upsert(entity: PlaybackStateEntity)

    @Query("SELECT * FROM playback_state WHERE videoId = :videoId")
    suspend fun getByVideoId(videoId: String): PlaybackStateEntity?

    /** Semua posisi tersimpan — untuk bar progres di daftar video. */
    @Query("SELECT * FROM playback_state")
    suspend fun getAll(): List<PlaybackStateEntity>

    /** Video yang paling terakhir diputar (updatedAt terbesar). */
    @Query("SELECT * FROM playback_state ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getMostRecent(): PlaybackStateEntity?

    @Query("DELETE FROM playback_state WHERE videoId = :videoId")
    suspend fun deleteByVideoId(videoId: String)

    @Query("DELETE FROM playback_state")
    suspend fun deleteAll()
}
