package com.azavern.aviplayer.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.azavern.aviplayer.data.db.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

/** DAO playlist (Blueprint Bagian 7 & Milestone 10). */
@Dao
interface PlaylistDao {

    @Insert
    suspend fun insert(entity: PlaylistEntity): Long

    /** Semua playlist, yang paling lama dibuat lebih dulu. */
    @Query("SELECT * FROM playlists ORDER BY createdAt ASC")
    fun getAll(): Flow<List<PlaylistEntity>>

    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    suspend fun rename(playlistId: Long, newName: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deleteById(playlistId: Long)
}
