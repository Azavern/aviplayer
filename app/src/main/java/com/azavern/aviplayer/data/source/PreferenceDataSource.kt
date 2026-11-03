package com.azavern.aviplayer.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.data.model.ThemeMode
import com.azavern.aviplayer.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Wrapper tunggal untuk DataStore<Preferences> (Blueprint Bagian 7).
 * Semua akses baca/tulis DataStore hanya melalui file ini, dan setiap
 * key didefinisikan sebagai private val dari Constants.
 *
 * Milestone 2: setup DataStore + definisi key.
 * Milestone 4: pasangan getter/setter tema (mode, aksen, aksen sistem).
 * Key sisanya ditambahkan pada milestone Kustomisasi.
 */
class PreferenceDataSource @Inject constructor(
    /** Hanya untuk lapisan repository di paket data; UI/ViewModel tidak menyentuh ini. */
    val dataStore: DataStore<Preferences>
) {

    // ---- Tema (Milestone 4) ----

    /** Nama enum ThemeMode: "SYSTEM" | "LIGHT" | "DARK" | "AMOLED". */
    fun getThemeMode(): Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    /** Warna aksen ARGB; bawaan lavender ThemeConfig.Default. */
    fun getAccentColor(): Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_ACCENT_COLOR] ?: ThemeConfig.Default.accentColor
    }

    suspend fun setAccentColor(color: Long) {
        dataStore.edit { it[KEY_ACCENT_COLOR] = color }
    }

    fun getUseSystemAccent(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_USE_SYSTEM_ACCENT] ?: false
    }

    suspend fun setUseSystemAccent(enabled: Boolean) {
        dataStore.edit { it[KEY_USE_SYSTEM_ACCENT] = enabled }
    }

    // ---- Judul player (Milestone 7) ----

    fun getTitleFontSize(): Flow<Float> = dataStore.data.map { prefs ->
        prefs[KEY_TITLE_FONT_SIZE] ?: 14f
    }

    suspend fun setTitleFontSize(sizeSp: Float) {
        dataStore.edit { it[KEY_TITLE_FONT_SIZE] = sizeSp }
    }

    fun getTitleMaxLines(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_TITLE_MAX_LINES] ?: 1
    }

    suspend fun setTitleMaxLines(maxLines: Int) {
        dataStore.edit { it[KEY_TITLE_MAX_LINES] = maxLines }
    }

    fun getTitleColor(): Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_TITLE_COLOR] ?: 0xFFFFFFFFL
    }

    suspend fun setTitleColor(color: Long) {
        dataStore.edit { it[KEY_TITLE_COLOR] = color }
    }

    fun getTitleBold(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_TITLE_BOLD] ?: false
    }

    suspend fun setTitleBold(bold: Boolean) {
        dataStore.edit { it[KEY_TITLE_BOLD] = bold }
    }

    fun getTitleMarquee(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_TITLE_MARQUEE] ?: true
    }

    suspend fun setTitleMarquee(enabled: Boolean) {
        dataStore.edit { it[KEY_TITLE_MARQUEE] = enabled }
    }

    // ---- Layout kontrol player (Milestone 7) ----

    /** JSON ControlLayoutConfig; null = belum pernah disimpan. */
    fun getControlLayoutJson(): Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_CONTROL_LAYOUT_JSON]
    }

    suspend fun setControlLayoutJson(json: String) {
        dataStore.edit { it[KEY_CONTROL_LAYOUT_JSON] = json }
    }

    companion object {
        // String keys
        val KEY_THEME_MODE = stringPreferencesKey(Constants.PREF_THEME_MODE)
        val KEY_CONTROL_LAYOUT_JSON = stringPreferencesKey(Constants.PREF_CONTROL_LAYOUT_JSON)
        val KEY_FOLDER_FILTER_MODE = stringPreferencesKey(Constants.PREF_FOLDER_FILTER_MODE)
        val KEY_WHITELISTED_PATHS = stringPreferencesKey(Constants.PREF_WHITELISTED_PATHS)
        val KEY_ORIENTATION_MODE = stringPreferencesKey(Constants.PREF_ORIENTATION_MODE)

        // Long keys (warna disimpan sebagai color int)
        val KEY_ACCENT_COLOR = longPreferencesKey(Constants.PREF_ACCENT_COLOR)
        val KEY_TITLE_COLOR = longPreferencesKey(Constants.PREF_TITLE_COLOR)

        // Boolean keys
        val KEY_USE_SYSTEM_ACCENT = booleanPreferencesKey(Constants.PREF_USE_SYSTEM_ACCENT)
        val KEY_TITLE_BOLD = booleanPreferencesKey(Constants.PREF_TITLE_BOLD)
        val KEY_TITLE_MARQUEE = booleanPreferencesKey(Constants.PREF_TITLE_MARQUEE)

        // Float & Int keys
        val KEY_TITLE_FONT_SIZE = floatPreferencesKey(Constants.PREF_TITLE_FONT_SIZE)
        val KEY_TITLE_MAX_LINES = intPreferencesKey(Constants.PREF_TITLE_MAX_LINES)
        val KEY_SEEK_STEP_MS = intPreferencesKey(Constants.PREF_SEEK_STEP_MS)
    }
}
