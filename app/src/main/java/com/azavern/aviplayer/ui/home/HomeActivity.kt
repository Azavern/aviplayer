package com.azavern.aviplayer.ui.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Layar utama (launcher). Shell Milestone 0: menampilkan tema kosong;
 * daftar video/folder dan permintaan izin dibangun pada Milestone 3.
 */
@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme(themeConfig = ThemeConfig.Default) {
                // Konten Beranda — Milestone 3.
            }
        }
    }
}
