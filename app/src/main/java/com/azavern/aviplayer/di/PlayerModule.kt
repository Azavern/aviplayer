package com.azavern.aviplayer.di

import android.content.Context
import com.azavern.aviplayer.player.core.VideoPlayerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

/**
 * Modul DI untuk player (Blueprint Bagian 7): menyediakan
 * [VideoPlayerManager] satu instance per sesi PlayerActivity.
 *
 * Penyimpangan sadar dari blueprint: blueprint menulis
 * `@InstallIn(ActivityComponent)` + `@ActivityScoped`, tetapi
 * `@HiltViewModel` hidup di ViewModelComponent yang TIDAK bisa melihat
 * binding ActivityComponent — manager tidak akan bisa di-inject ke
 * PlayerViewModel. Karena itu dipakai ActivityRetainedComponent +
 * `@ActivityRetainedScoped`: umurnya sama dengan sesi player di sini
 * (PlayerActivity menangani configChanges sendiri, tidak dibuat ulang
 * saat rotasi) dan bindingnya terjangkau ViewModel. Dilaporkan ke
 * pemilik proyek pada laporan Milestone 1.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
object PlayerModule {

    @Provides
    @ActivityRetainedScoped
    fun provideVideoPlayerManager(
        @ApplicationContext context: Context
    ): VideoPlayerManager = VideoPlayerManager(context)
}
