package com.azavern.aviplayer.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Modul DI tingkat aplikasi (Blueprint Bagian 7). Shell Milestone 0:
 * binding DataStore, AppDatabase, DAO, DataSource, dan Repository
 * ditambahkan pada milestone masing-masing.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
