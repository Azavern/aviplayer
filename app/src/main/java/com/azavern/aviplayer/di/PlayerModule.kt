package com.azavern.aviplayer.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

/**
 * Modul DI tingkat Activity (Blueprint Bagian 7). Shell Milestone 0:
 * VideoPlayerManager (@ActivityScoped) disediakan di sini pada Milestone 1
 * karena siklus hidupnya terikat PlayerActivity.
 */
@Module
@InstallIn(ActivityComponent::class)
object PlayerModule
