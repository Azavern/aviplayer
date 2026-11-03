package com.azavern.aviplayer.data.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.azavern.aviplayer.data.db.dao.PlaybackStateDao
import com.azavern.aviplayer.data.db.entity.PlaybackStateEntity

/**
 * Database Room aplikasi (Blueprint Bagian 7).
 *
 * Milestone 2: versi 1 hanya berisi PlaybackStateEntity. Entity playlist
 * ditambahkan pada milestone Playlist (skema diekspor ke app/schemas).
 * Modul ini Android-only (bukan KMP), jadi `@ConstructedBy` tidak
 * diperlukan — builder memakai `Room.databaseBuilder` klasik dengan
 * driver `AndroidSQLiteDriver` (lihat AppModule).
 */
@Database(
    entities = [PlaybackStateEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playbackStateDao(): PlaybackStateDao
}
