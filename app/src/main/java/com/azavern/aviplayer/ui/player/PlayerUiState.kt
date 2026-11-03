package com.azavern.aviplayer.ui.player

import com.azavern.aviplayer.data.model.OrientationMode
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.player.core.PlayerState

/**
 * State layar Player (Blueprint Bagian 7 & Milestone 1 — versi minimal:
 * field inti pemutaran + orientasi Bagian 10F. Field kustomisasi,
 * gesture, dan capture menyusul pada milestone masing-masing).
 */
data class PlayerUiState(
    val playerState: PlayerState = PlayerState.Idle,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val videoTitle: String = "",
    val videoPath: String = "",
    val videoId: String = "",
    /** Rasio aspek video (lebar/tinggi); null sampai ukuran video diketahui. */
    val videoAspectRatio: Float? = null,
    /** Mode orientasi runtime (Bagian 10F), diterapkan Activity sebagai requestedOrientation. */
    val orientationMode: OrientationMode = OrientationMode.AUTO,
    /** Status kunci rotasi HP (dibaca Activity dari Settings.System). */
    val systemAutoRotate: Boolean = true,
    /** Konfigurasi tema berjalan dari SettingsRepository (Milestone 4). */
    val themeConfig: ThemeConfig = ThemeConfig.Default,
    /** Kecepatan putar aktif (tombol "1.0×" di bar kontrol bawah). */
    val playbackSpeed: Float = 1.0f
)
