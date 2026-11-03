package com.azavern.aviplayer.ui.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Layar pemutar video. Shell Milestone 0 — implementasi pemutaran,
 * kontrol, dan orientasi bebas (Blueprint Bagian 10F) pada Milestone 1.
 * Orientasi TIDAK dikunci di Manifest; diatur runtime dari state.
 */
@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme(themeConfig = ThemeConfig.Default) {
                // Konten Player — Milestone 1.
            }
        }
    }
}
