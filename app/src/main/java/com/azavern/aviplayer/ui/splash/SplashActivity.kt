package com.azavern.aviplayer.ui.splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.azavern.aviplayer.data.repository.VideoRepository
import com.azavern.aviplayer.ui.home.HomeActivity
import com.azavern.aviplayer.ui.splash.components.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Pintu masuk aplikasi (launcher): memainkan animasi splash
 * AviPlay, lalu meneruskan ke HomeActivity dan selesai. Tema
 * jendelanya hitam polos (Theme.Aviplayer.Splash) supaya splash
 * bawaan sistem menyambung mulus ke animasi ini.
 *
 * RP3 (perintah Veo 2026-11-13): selagi splash tampil, daftar video
 * sudah dipindai di latar (VideoRepository.preloadAllVideos) supaya
 * beranda kebuka sudah terisi — splash jadi pengalihan, bukan
 * waktu tunggu kosong.
 */
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject
    lateinit var videoRepository: VideoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashScreen(
                onFinished = { goHome() },
                onPreload = {
                    if (isReadPermissionGranted()) {
                        videoRepository.preloadAllVideos()
                    }
                    // Tanpa izin, preload dilewati: HomeActivity yang
                    // meminta izin seperti biasa, lalu memuat sendiri.
                }
            )
        }
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    /** Izin baca media yang sama dengan yang dipakai HomeActivity. */
    private fun isReadPermissionGranted(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
    }
}
