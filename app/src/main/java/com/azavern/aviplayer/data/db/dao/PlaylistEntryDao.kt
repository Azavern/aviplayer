package com.azavern.aviplayer.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.azavern.aviplayer.data.db.entity.PlaylistEntryEntity
import kotlinx.coroutines.flow.Flow

/** DAO entri playlist (Blueprint Bagian 7 & Milestone 10). */
@Dao
interface PlaylistEntryDao {

    @Insert
    suspend fun insert(entity: PlaylistEntryEntity): Long

    /** Entri satu playlist sesuai urutan tampilnya. */
    @Query("SELECT * FROM playlist_entries WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getByPlaylist(playlistId: Long): Flow<List<PlaylistEntryEntity>>

    /** Semua entri semua playlist — untuk ringkasan PlaylistInfo. */
    @Query("SELECT * FROM playlist_entries ORDER BY orderIndex ASC")
    fun getAll(): Flow<List<PlaylistEntryEntity>>

    @Query("DELETE FROM playlist_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)

    @Query("UPDATE playlist_entries SET orderIndex = :orderIndex WHERE id = :entryId")
    suspend fun updateOrderIndex(entryId: Long, orderIndex: Int)

    /** Tulis ulang urutan satu playlist dalam satu transaksi. */
    @Transaction
    suspend fun reorder(entries: List<PlaylistEntryEntity>) {
        entries.forEach { entry -> updateOrderIndex(entry.id, entry.orderIndex) }
    }
}
