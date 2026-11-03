package com.azavern.aviplayer.ui.customization

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azavern.aviplayer.ui.customization.components.CustomizationScreen
import com.azavern.aviplayer.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Layar kustomisasi player (Milestone 7): editor gaya judul dan
 * penukar posisi tombol. Tema mengikuti konfigurasi berjalan dari
 * SettingsRepository lewat CustomizationViewModel.
 */
@AndroidEntryPoint
class CustomizationActivity : ComponentActivity() {

    private val viewModel: CustomizationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AppTheme(themeConfig = uiState.themeConfig) {
                CustomizationScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
