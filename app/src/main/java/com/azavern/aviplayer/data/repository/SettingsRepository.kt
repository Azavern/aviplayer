package com.azavern.aviplayer.data.repository

import com.azavern.aviplayer.data.model.ControlLayoutConfig
import com.azavern.aviplayer.data.model.PlayerUiConfig
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.data.model.ThemeMode
import com.azavern.aviplayer.data.model.TitleConfig
import com.azavern.aviplayer.data.source.PreferenceDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
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

    // ---- Judul & layout kontrol (Milestone 7) ----

    private val layoutJson = Json { ignoreUnknownKeys = true }

    /** Gaya judul player, digabung dari lima key DataStore. */
    fun getTitleConfig(): Flow<TitleConfig> = combine(
        preferenceDataSource.getTitleFontSize(),
        preferenceDataSource.getTitleMaxLines(),
        preferenceDataSource.getTitleColor(),
        preferenceDataSource.getTitleBold(),
        preferenceDataSource.getTitleMarquee()
    ) { fontSize, maxLines, color, bold, marquee ->
        TitleConfig(
            fontSizeSp = fontSize.coerceIn(10f, 22f),
            maxLines = if (maxLines == 2) 2 else 1,
            color = color,
            isBold = bold,
            marqueeEnabled = marquee
        )
    }

    suspend fun updateTitleConfig(config: TitleConfig) {
        preferenceDataSource.setTitleFontSize(config.fontSizeSp.coerceIn(10f, 22f))
        preferenceDataSource.setTitleMaxLines(if (config.maxLines == 2) 2 else 1)
        preferenceDataSource.setTitleColor(config.color)
        preferenceDataSource.setTitleBold(config.isBold)
        preferenceDataSource.setTitleMarquee(config.marqueeEnabled)
    }

    /** Layout kontrol player dari JSON; JSON rusak/kosong → Default. */
    fun getControlLayoutConfig(): Flow<ControlLayoutConfig> =
        preferenceDataSource.getControlLayoutJson().map { json ->
            if (json.isNullOrBlank()) {
                ControlLayoutConfig.Default
            } else {
                runCatching {
                    layoutJson.decodeFromString<ControlLayoutConfig>(json)
                }.getOrDefault(ControlLayoutConfig.Default)
            }
        }

    suspend fun updateControlLayoutConfig(config: ControlLayoutConfig) {
        preferenceDataSource.setControlLayoutJson(
            layoutJson.encodeToString(ControlLayoutConfig.serializer(), config)
        )
    }

    suspend fun resetControlLayoutToDefault() {
        updateControlLayoutConfig(ControlLayoutConfig.Default)
    }

    /** Semua konfigurasi tampilan player dalam satu Flow. */
    fun getPlayerUiConfig(): Flow<PlayerUiConfig> = combine(
        getTitleConfig(),
        getControlLayoutConfig(),
        getThemeConfig()
    ) { titleConfig, controlLayoutConfig, themeConfig ->
        PlayerUiConfig(
            titleConfig = titleConfig,
            controlLayoutConfig = controlLayoutConfig,
            themeConfig = themeConfig
        )
    }
}
