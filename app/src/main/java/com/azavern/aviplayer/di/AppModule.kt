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
import com.azavern.aviplayer.data.repository.PlaybackRepository
import com.azavern.aviplayer.data.source.PreferenceDataSource
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
 * PlaybackStateDao, PlaybackRepository. DataSource/Repository Beranda
 * ditambahkan di Milestone 3; SettingsRepository di Milestone 4.
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
    @Singleton
    fun providePlaybackRepository(dao: PlaybackStateDao): PlaybackRepository =
        PlaybackRepository(dao)
}
