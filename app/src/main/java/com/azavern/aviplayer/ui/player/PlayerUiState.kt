package com.azavern.aviplayer.ui.player

import android.net.Uri
import com.azavern.aviplayer.data.model.OrientationMode
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.player.core.PlayerState

/** Hasil tangkapan frame terakhir (Milestone 6). */
sealed class CaptureResult {
    data class Success(val uri: Uri) : CaptureResult()
    data class Error(val message: String) : CaptureResult()
}

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
    val playbackSpeed: Float = 1.0f,
    /** Level kecerahan jendela 0f..1f; -1f = ikuti sistem (Milestone 5). */
    val brightnessLevel: Float = -1f,
    /**
     * True hanya setelah pengguna mengubah kecerahan lewat gesture.
     * Selama false, PlayerActivity TIDAK menerapkan brightnessLevel
     * ke window — nilai state hanya baseline awal gesture. (Perbaikan
     * 2026-11-04: penerapan otomatis saat video mulai membuat
     * kecerahan melonjak sendiri; atas perintah Veo fungsi penerapan
     * otomatis itu dihapus.)
     */
    val brightnessOverrideActive: Boolean = false,
    /** Level volume media 0f..1f sebagai fraksi volume maksimum (Milestone 5). */
    val volumeLevel: Float = 1f,
    /** Posisi pratinjau saat gesture seek aktif; null = tidak ada pratinjau. */
    val seekPreviewMs: Long? = null,
    /** Hasil capture frame terakhir; null = belum ada / sudah ditampilkan. */
    val captureResult: CaptureResult? = null
)
