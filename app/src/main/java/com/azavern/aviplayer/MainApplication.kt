package com.azavern.aviplayer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Entry point global aplikasi. Anotasi [HiltAndroidApp] memicu pembuatan
 * component DI tingkat aplikasi (Blueprint Milestone 0 butir 4).
 */
@HiltAndroidApp
class MainApplication : Application()
