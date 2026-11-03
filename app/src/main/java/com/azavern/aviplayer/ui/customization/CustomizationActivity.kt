package com.azavern.aviplayer.ui.customization

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Layar kustomisasi (tema, judul, kontrol, folder). Shell Milestone 0 —
 * isi dibangun pada milestone kustomisasi.
 */
@AndroidEntryPoint
class CustomizationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme(themeConfig = ThemeConfig.Default) {
                // Konten Kustomisasi — milestone kustomisasi.
            }
        }
    }
}
