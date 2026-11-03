package com.azavern.aviplayer.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Token warna aplikasi. Nilai persis UI.md §2 (keputusan PT-25/PT-26,
 * 3 Nov 2026): aksen bawaan lavender, netral Gelap dari Neumorphism Android.
 * Komponen tidak boleh menulis literal warna sendiri (AGENTS.md §6) —
 * semua lewat skema di file ini.
 */

// ---- Aksen bawaan ----
val AccentLight = Color(0xFF6655F5)
val AccentDark = Color(0xFF8B7FFF)

// ---- Palet Terang (UI.md §2.1) ----
val LightBackground = Color(0xFFE9EBF8)
val LightBackgroundSoft = Color(0xFFEFF0FA)
val LightSurface = Color(0xFFEAECF8)
val LightSurfaceLight = Color(0xFFF4F5FC)
val LightSurfaceDark = Color(0xFFDDE1F1)
val LightSurfacePressed = Color(0xFFE4E7F6)
val LightTextPrimary = Color(0xFF171A36)
val LightTextSecondary = Color(0xFF515A8C)
val LightTextDisabled = Color(0xFFAEB3CA)
val LightBorderSubtle = Color(0xFFDCE0F0)
val LightDivider = Color(0xFFD7DBED)

// ---- Palet Gelap (UI.md §2.2) ----
val DarkBackground = Color(0xFF191B2B)
val DarkSurface = Color(0xFF1C1E30)
val DarkSurfaceLight = Color(0xFF262842)
val DarkSurfaceDark = Color(0xFF12131F)
val DarkSurfacePressed = Color(0xFF23253E)
val DarkTextPrimary = Color(0xFFF1F2FA)
val DarkTextSecondary = Color(0xFFB4B9D4)
val DarkBorderSubtle = Color(0xFF2C2F4D)
val DarkDivider = Color(0xFF272A44)

// ---- AMOLED (UI.md §2.2): seperti Gelap, latar & permukaan hitam murni ----
val AmoledOutline = Color(0xFF1E1E28)

// ---- Semantik (UI.md §2.1; hanya isian/ikon, bukan teks — PT-32) ----
val InfoBlue = Color(0xFF438BFA)
val SuccessGreen = Color(0xFF25B993)
val WarningOrange = Color(0xFFF5A443)
val ErrorRed = Color(0xFFE76B82)

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = AccentLight,
    onPrimary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceDark,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerHighest = LightSurfaceLight,
    outline = LightBorderSubtle,
    outlineVariant = LightDivider,
    error = ErrorRed,
    onError = Color.White
)

val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = AccentDark,
    onPrimary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceDark,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerHighest = DarkSurfaceLight,
    outline = DarkBorderSubtle,
    outlineVariant = DarkDivider,
    error = ErrorRed,
    onError = Color.White
)

/** Identik [DarkColorScheme] kecuali background & surface hitam murni (Blueprint Bagian 7). */
val AmoledColorScheme: ColorScheme = DarkColorScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    outlineVariant = AmoledOutline
)

/**
 * Bangun skema dari warna aksen pilihan pengguna (Blueprint Bagian 7):
 * skema dasar Terang/Gelap dengan [accentColor] sebagai primary.
 * Teks di atas aksen dipilih dari luminansi agar kontras terjaga untuk
 * aksen kustom apa pun.
 */
fun buildDynamicColorScheme(accentColor: Color, isDark: Boolean): ColorScheme {
    val base = if (isDark) DarkColorScheme else LightColorScheme
    val onAccent = if (accentColor.luminance() > 0.45f) LightTextPrimary else Color.White
    return base.copy(primary = accentColor, onPrimary = onAccent)
}
