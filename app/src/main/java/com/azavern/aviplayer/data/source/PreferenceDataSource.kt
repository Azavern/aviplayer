package com.azavern.aviplayer.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.azavern.aviplayer.utils.Constants
import javax.inject.Inject

/**
 * Wrapper tunggal untuk DataStore<Preferences> (Blueprint Bagian 7).
 * Semua akses baca/tulis DataStore hanya melalui file ini, dan setiap
 * key didefinisikan sebagai private val dari Constants.
 *
 * Milestone 2: hanya setup DataStore + definisi key. Pasangan
 * getter/setter Flow per key ditambahkan pada milestone pemiliknya
 * (tema di Milestone 4, sisanya di milestone Kustomisasi).
 */
class PreferenceDataSource @Inject constructor(
    /** Hanya untuk lapisan repository di paket data; UI/ViewModel tidak menyentuh ini. */
    val dataStore: DataStore<Preferences>
) {

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
