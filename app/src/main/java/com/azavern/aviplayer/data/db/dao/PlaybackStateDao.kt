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

    @Query("DELETE FROM playback_state")
    suspend fun deleteAll()
}
