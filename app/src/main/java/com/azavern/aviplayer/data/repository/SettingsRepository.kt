package com.azavern.aviplayer.data.repository

import com.azavern.aviplayer.data.model.ControlLayoutConfig
import com.azavern.aviplayer.data.model.FolderFilterMode
import com.azavern.aviplayer.data.model.FolderVisibilityConfig
import com.azavern.aviplayer.data.model.SortConfig
import com.azavern.aviplayer.data.model.SortField
import com.azavern.aviplayer.data.model.PlayerUiConfig
import com.azavern.aviplayer.data.model.SeekConfig
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.data.model.ThemeMode
import com.azavern.aviplayer.data.model.TitleConfig
import com.azavern.aviplayer.data.source.PreferenceDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
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

    /**
     * Layout kontrol player dari JSON; JSON rusak/kosong → Default.
     * Hasil selalu lewat [ControlLayoutConfig.sanitized]: area kiri
     * atas terkunci (perintah Veo), tombol yang terlanjur tersimpan
     * di sana dipindah ke kanan bawah.
     */
    fun getControlLayoutConfig(): Flow<ControlLayoutConfig> =
        preferenceDataSource.getControlLayoutJson().map { json ->
            if (json.isNullOrBlank()) {
                ControlLayoutConfig.Default
            } else {
                runCatching {
                    layoutJson.decodeFromString<ControlLayoutConfig>(json)
                }.getOrDefault(ControlLayoutConfig.Default)
            }.sanitized()
        }

    suspend fun updateControlLayoutConfig(config: ControlLayoutConfig) {
        preferenceDataSource.setControlLayoutJson(
            layoutJson.encodeToString(ControlLayoutConfig.serializer(), config)
        )
    }

    suspend fun resetControlLayoutToDefault() {
        updateControlLayoutConfig(ControlLayoutConfig.Default)
    }

    // ---- Visibilitas folder (Milestone 9) ----

    /** Mode filter + daftar whitelist folder dari DataStore. */
    fun getFolderVisibilityConfig(): Flow<FolderVisibilityConfig> = combine(
        preferenceDataSource.getFolderFilterMode(),
        preferenceDataSource.getWhitelistedPathsJson()
    ) { modeName, pathsJson ->
        FolderVisibilityConfig(
            mode = runCatching { FolderFilterMode.valueOf(modeName) }
                .getOrDefault(FolderFilterMode.ALL),
            whitelistedPaths = if (pathsJson.isNullOrBlank()) {
                emptySet()
            } else {
                runCatching {
                    layoutJson.decodeFromString<List<String>>(pathsJson).toSet()
                }.getOrDefault(emptySet())
            }
        )
    }

    suspend fun updateFolderVisibilityConfig(config: FolderVisibilityConfig) {
        preferenceDataSource.setFolderFilterMode(config.mode.name)
        preferenceDataSource.setWhitelistedPathsJson(
            layoutJson.encodeToString(
                ListSerializer(String.serializer()), config.whitelistedPaths.toList()
            )
        )
    }

    // ---- Urutan daftar video (sorting global) ----

    /** Bidang urutan + arah dari DataStore; nama rusak → TIME. */
    fun getSortConfig(): Flow<SortConfig> = combine(
        preferenceDataSource.getSortField(),
        preferenceDataSource.getSortAscending()
    ) { fieldName, ascending ->
        SortConfig(
            field = runCatching { SortField.valueOf(fieldName) }
                .getOrDefault(SortField.TIME),
            ascending = ascending
        )
    }

    suspend fun updateSortConfig(config: SortConfig) {
        preferenceDataSource.setSortField(config.field.name)
        preferenceDataSource.setSortAscending(config.ascending)
    }

    // ---- Durasi lewati double-tap (maju/mundur terpisah) ----

    /** Durasi lewati dari DataStore; nilai di luar pilihan → 10 detik. */
    fun getSeekConfig(): Flow<SeekConfig> = combine(
        preferenceDataSource.getSeekForwardSeconds(),
        preferenceDataSource.getSeekBackwardSeconds()
    ) { forward, backward ->
        SeekConfig(
            forwardSeconds = SeekConfig.sanitize(forward),
            backwardSeconds = SeekConfig.sanitize(backward)
        )
    }

    suspend fun updateSeekConfig(config: SeekConfig) {
        preferenceDataSource.setSeekForwardSeconds(
            SeekConfig.sanitize(config.forwardSeconds)
        )
        preferenceDataSource.setSeekBackwardSeconds(
            SeekConfig.sanitize(config.backwardSeconds)
        )
    }

    /** Semua konfigurasi tampilan player dalam satu Flow. */
    fun getPlayerUiConfig(): Flow<PlayerUiConfig> = combine(
        getTitleConfig(),
        getControlLayoutConfig(),
        getThemeConfig(),
        getSeekConfig()
    ) { titleConfig, controlLayoutConfig, themeConfig, seekConfig ->
        PlayerUiConfig(
            titleConfig = titleConfig,
            controlLayoutConfig = controlLayoutConfig,
            themeConfig = themeConfig,
            seekConfig = seekConfig
        )
    }
}
