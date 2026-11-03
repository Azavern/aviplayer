package com.azavern.aviplayer.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azavern.aviplayer.ui.customization.CustomizationActivity
import com.azavern.aviplayer.ui.home.components.HomeScreen
import com.azavern.aviplayer.ui.player.PlayerActivity
import com.azavern.aviplayer.ui.theme.AppTheme
import com.azavern.aviplayer.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

/**
 * Layar utama (launcher) — Blueprint Milestone 3.
 *
 * Bertanggung jawab atas izin baca media (READ_MEDIA_VIDEO di
 * API 33+, READ_EXTERNAL_STORAGE di bawahnya): diminta sekali saat
 * pertama dibuka, hasilnya dilaporkan ke HomeViewModel. Izin yang
 * sudah diberikan dari Pengaturan HP terdeteksi di onStart, jadi
 * tidak perlu meminta ulang.
 */
@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    private val readPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AppTheme(themeConfig = uiState.themeConfig) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToPlayer = { path, id, title -> openPlayer(path, id, title) },
                    onRequestPermission = { permissionLauncher.launch(readPermission) },
                    onNavigateToCustomization = { openCustomization() }
                )
            }
        }
        if (!isPermissionGranted()) {
            permissionLauncher.launch(readPermission)
        }
    }

    override fun onStart() {
        super.onStart()
        // Izin bisa diberikan dari Pengaturan HP saat aplikasi di latar —
        // deteksi di sini supaya daftar langsung termuat.
        if (isPermissionGranted() && !viewModel.uiState.value.hasPermission) {
            viewModel.onPermissionResult(true)
        }
        // Segarkan info tontonan (progres & video terakhir) sepulang dari player.
        viewModel.onResume()
    }

    private fun openCustomization() {
        startActivity(Intent(this, CustomizationActivity::class.java))
    }

    private fun isPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(this, readPermission) ==
            PackageManager.PERMISSION_GRANTED

    private fun openPlayer(path: String, id: String, title: String) {
        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra(Constants.KEY_VIDEO_PATH, path)
                .putExtra(Constants.KEY_VIDEO_ID, id)
                .putExtra(Constants.KEY_VIDEO_TITLE, title)
        )
    }
}
