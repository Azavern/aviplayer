package com.azavern.aviplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.data.model.ThemeMode

/**
 * Tema utama aplikasi (Blueprint Bagian 7): memilih skema dari
 * [ThemeConfig] — mode SYSTEM/LIGHT/DARK/AMOLED, aksen kustom lewat
 * [buildDynamicColorScheme], atau dynamic color sistem (API 31+)
 * bila pengguna mengaktifkannya.
 */
@Composable
fun AppTheme(
    themeConfig: ThemeConfig = ThemeConfig.Default,
    content: @Composable () -> Unit
) {
    val isDarkSystem = isSystemInDarkTheme()
    val isDark = when (themeConfig.themeMode) {
        ThemeMode.SYSTEM -> isDarkSystem
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }

    val context = LocalContext.current
    val colorScheme = if (themeConfig.useSystemAccent && Build.VERSION.SDK_INT >= 31) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        val scheme = buildDynamicColorScheme(Color(themeConfig.accentColor), isDark)
        if (themeConfig.themeMode == ThemeMode.AMOLED) {
            scheme.copy(
                background = Color.Black,
                surface = Color.Black,
                outlineVariant = AmoledOutline
            )
        } else {
            scheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = AppTypography) {
        Surface(color = MaterialTheme.colorScheme.background) { content() }
    }
}
