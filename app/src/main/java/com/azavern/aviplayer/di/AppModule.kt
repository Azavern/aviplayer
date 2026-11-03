package com.azavern.aviplayer.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.azavern.aviplayer.data.db.AppDatabase
import com.azavern.aviplayer.data.db.dao.PlaybackStateDao
import com.azavern.aviplayer.data.db.dao.PlaylistDao
import com.azavern.aviplayer.data.db.dao.PlaylistEntryDao
import com.azavern.aviplayer.data.repository.FolderRepository
import com.azavern.aviplayer.data.repository.PlaybackRepository
import com.azavern.aviplayer.data.repository.PlaylistRepository
import com.azavern.aviplayer.data.repository.SettingsRepository
import com.azavern.aviplayer.data.repository.VideoRepository
import com.azavern.aviplayer.data.source.FileScannerDataSource
import com.azavern.aviplayer.data.source.PreferenceDataSource
import com.azavern.aviplayer.player.capture.VideoFrameCapturer
import com.azavern.aviplayer.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Modul DI tingkat aplikasi (Blueprint Bagian 7).
 *
 * Milestone 2: DataStore, AppDatabase (Room 3 + AndroidSQLiteDriver),
 * PlaybackStateDao, PlaybackRepository.
 * Milestone 3: FileScannerDataSource, VideoRepository, FolderRepository.
 * Milestone 4: SettingsRepository (tema).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(Constants.DATASTORE_NAME) }
    )

    @Provides
    @Singleton
    fun providePreferenceDataSource(
        dataStore: DataStore<Preferences>
    ): PreferenceDataSource = PreferenceDataSource(dataStore)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        Constants.DATABASE_NAME
    )
        .setDriver(AndroidSQLiteDriver())
        .build()

    @Provides
    fun providePlaybackStateDao(database: AppDatabase): PlaybackStateDao =
        database.playbackStateDao()

    @Provides
    fun providePlaylistDao(database: AppDatabase): PlaylistDao =
        database.playlistDao()

    @Provides
    fun providePlaylistEntryDao(database: AppDatabase): PlaylistEntryDao =
        database.playlistEntryDao()

    @Provides
    @Singleton
    fun providePlaylistRepository(
        playlistDao: PlaylistDao,
        playlistEntryDao: PlaylistEntryDao,
        fileScanner: FileScannerDataSource
    ): PlaylistRepository = PlaylistRepository(playlistDao, playlistEntryDao, fileScanner)

    @Provides
    @Singleton
    fun providePlaybackRepository(dao: PlaybackStateDao): PlaybackRepository =
        PlaybackRepository(dao)

    @Provides
    @Singleton
    fun provideFileScannerDataSource(
        @ApplicationContext context: Context
    ): FileScannerDataSource = FileScannerDataSource(context)

    @Provides
    @Singleton
    fun provideVideoRepository(fileScanner: FileScannerDataSource): VideoRepository =
        VideoRepository(fileScanner)

    @Provides
    @Singleton
    fun provideFolderRepository(
        fileScanner: FileScannerDataSource,
        settingsRepository: SettingsRepository
    ): FolderRepository = FolderRepository(fileScanner, settingsRepository)

    @Provides
    @Singleton
    fun provideSettingsRepository(
        preferenceDataSource: PreferenceDataSource
    ): SettingsRepository = SettingsRepository(preferenceDataSource)

    @Provides
    @Singleton
    fun provideVideoFrameCapturer(
        @ApplicationContext context: Context
    ): VideoFrameCapturer = VideoFrameCapturer(context)
}
