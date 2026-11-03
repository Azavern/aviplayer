package com.azavern.aviplayer

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.video.VideoFrameDecoder
import dagger.hilt.android.HiltAndroidApp

/**
 * Entry point global aplikasi. Anotasi [HiltAndroidApp] memicu pembuatan
 * component DI tingkat aplikasi (Blueprint Milestone 0 butir 4).
 *
 * Milestone 3: mendaftarkan VideoFrameDecoder Coil 3 supaya thumbnail
 * daftar video diambil dari frame video lokal itu sendiri.
 */
@HiltAndroidApp
class MainApplication : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}
