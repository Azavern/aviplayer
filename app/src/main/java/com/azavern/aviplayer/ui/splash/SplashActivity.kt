package com.azavern.aviplayer.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.azavern.aviplayer.ui.home.HomeActivity
import com.azavern.aviplayer.ui.splash.components.SplashScreen

/**
 * Pintu masuk aplikasi (launcher): memainkan animasi splash
 * AviPlay, lalu meneruskan ke HomeActivity dan selesai. Tema
 * jendelanya hitam polos (Theme.Aviplayer.Splash) supaya splash
 * bawaan sistem menyambung mulus ke animasi ini.
 */
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashScreen(onFinished = { goHome() })
        }
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
