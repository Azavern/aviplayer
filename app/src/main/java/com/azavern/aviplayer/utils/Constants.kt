package com.azavern.aviplayer.utils

/**
 * Semua string konstanta aplikasi (Blueprint Bagian 6, 7, dan 9) agar tidak
 * ada kunci intent/preferensi yang ditulis tangan di tempat lain.
 */
object Constants {

    // ---- Intent extras untuk PlayerActivity (Blueprint Bagian 6) ----
    const val KEY_VIDEO_PATH = "extra_video_path"
    const val KEY_QUEUE_PATHS = "extra_queue_paths"
    const val KEY_QUEUE_TITLES = "extra_queue_titles"
    const val KEY_VIDEO_ID = "extra_video_id"
    const val KEY_VIDEO_TITLE = "extra_video_title"

    // ---- Kunci DataStore Preferences (Blueprint Bagian 9) ----
    const val PREF_THEME_MODE = "pref_theme_mode"
    const val PREF_ACCENT_COLOR = "pref_accent_color"
    const val PREF_USE_SYSTEM_ACCENT = "pref_use_system_accent"
    const val PREF_TITLE_FONT_SIZE = "pref_title_font_size"
    const val PREF_TITLE_MAX_LINES = "pref_title_max_lines"
    const val PREF_TITLE_COLOR = "pref_title_color"
    const val PREF_TITLE_BOLD = "pref_title_bold"
    const val PREF_TITLE_MARQUEE = "pref_title_marquee"
    const val PREF_CONTROL_LAYOUT_JSON = "pref_control_layout_json"
    const val PREF_FOLDER_FILTER_MODE = "pref_folder_filter_mode"
    const val PREF_WHITELISTED_PATHS = "pref_whitelisted_paths"
    const val PREF_SEEK_STEP_MS = "pref_seek_step_ms"
    const val PREF_ORIENTATION_MODE = "pref_orientation_mode"

    // ---- Nilai bawaan & perilaku (Blueprint Bagian 7 dan 9) ----
    const val DEFAULT_SEEK_STEP_MS = 10_000
    const val AUTO_HIDE_CONTROLS_DELAY_MS = 3_000L

    /** Folder tujuan simpan frame capture lewat MediaStore (API 29+). */
    const val CAPTURE_SAVE_FOLDER = "Pictures/AVP"

    /** Nama file DataStore Preferences. */
    const val DATASTORE_NAME = "avp_preferences"

    /** Nama database Room. */
    const val DATABASE_NAME = "avp.db"
}
