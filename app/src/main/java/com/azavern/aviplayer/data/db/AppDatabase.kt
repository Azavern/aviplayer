package com.azavern.aviplayer.data.db

import androidx.room3.AutoMigration
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.azavern.aviplayer.data.db.dao.PlaybackStateDao
import com.azavern.aviplayer.data.db.dao.PlaylistDao
import com.azavern.aviplayer.data.db.dao.PlaylistEntryDao
import com.azavern.aviplayer.data.db.entity.PlaybackStateEntity
import com.azavern.aviplayer.data.db.entity.PlaylistEntity
import com.azavern.aviplayer.data.db.entity.PlaylistEntryEntity

/**
 * Database Room aplikasi (Blueprint Bagian 7).
 *
 * Versi 1 (Milestone 2): PlaybackStateEntity.
 * Versi 2 (Milestone 10): + PlaylistEntity & PlaylistEntryEntity.
 * Migrasi 1→2 memakai @AutoMigration karena hanya menambah tabel —
 * data posisi putar (playback_state) tidak disentuh sama sekali.
 * Modul ini Android-only (bukan KMP), jadi `@ConstructedBy` tidak
 * diperlukan — builder memakai `Room.databaseBuilder` klasik dengan
 * driver `AndroidSQLiteDriver` (lihat AppModule).
 */
@Database(
    entities = [
        PlaybackStateEntity::class,
        PlaylistEntity::class,
        PlaylistEntryEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playbackStateDao(): PlaybackStateDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun playlistEntryDao(): PlaylistEntryDao
}
