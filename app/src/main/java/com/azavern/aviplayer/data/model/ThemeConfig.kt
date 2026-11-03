package com.azavern.aviplayer.data.model

/** Mode tema aplikasi (Blueprint Bagian 8). */
enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

/**
 * Konfigurasi tema yang dapat diubah pengguna (Blueprint Bagian 8).
 *
 * [accentColor] adalah nilai warna ARGB; bawaan lavender `0xFF6655F5`
 * (keputusan aksen 3 Nov 2026, UI.md PT-25). [useSystemAccent] memakai
 * dynamic color sistem dan hanya berlaku di API 31+.
 */
data class ThemeConfig(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: Long = 0xFF6655F5,
    val useSystemAccent: Boolean = false
) {
    companion object { val Default = ThemeConfig() }
}
