package com.azavern.aviplayer.data.repository

import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.data.model.ThemeMode
import com.azavern.aviplayer.data.source.PreferenceDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Single source of truth untuk konfigurasi dan pengaturan UI
 * (Blueprint Bagian 7). Semua akses ke PreferenceDataSource oleh
 * UI/ViewModel harus melalui repository ini.
 *
 * Milestone 4: versi awal — hanya metode tema. Metode judul,
 * layout kontrol, visibilitas folder, dan langkah seek ditambahkan
 * pada milestone Kustomisasi.
 */
class SettingsRepository @Inject constructor(
    private val preferenceDataSource: PreferenceDataSource
) {

    /** Konfigurasi tema berjalan, digabung dari tiga key DataStore. */
    fun getThemeConfig(): Flow<ThemeConfig> = combine(
        preferenceDataSource.getThemeMode(),
        preferenceDataSource.getAccentColor(),
        preferenceDataSource.getUseSystemAccent()
    ) { modeName, accentColor, useSystemAccent ->
        ThemeConfig(
            themeMode = runCatching { ThemeMode.valueOf(modeName) }
                .getOrDefault(ThemeMode.SYSTEM),
            accentColor = accentColor,
            useSystemAccent = useSystemAccent
        )
    }

    /** Tulis konfigurasi tema baru (dipakai layar Kustomisasi nanti). */
    suspend fun updateThemeConfig(config: ThemeConfig) {
        preferenceDataSource.setThemeMode(config.themeMode.name)
        preferenceDataSource.setAccentColor(config.accentColor)
        preferenceDataSource.setUseSystemAccent(config.useSystemAccent)
    }
}
