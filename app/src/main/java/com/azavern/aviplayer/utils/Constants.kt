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
    const val KEY_QUEUE_DURATIONS = "extra_queue_durations"
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
    const val PREF_SEEK_FORWARD_SECONDS = "pref_seek_forward_seconds"
    const val PREF_SEEK_BACKWARD_SECONDS = "pref_seek_backward_seconds"
    const val PREF_SORT_FIELD = "pref_sort_field"
    const val PREF_SORT_ASCENDING = "pref_sort_ascending"
    const val PREF_ORIENTATION_MODE = "pref_orientation_mode"

    // ---- Nilai bawaan & perilaku (Blueprint Bagian 7 dan 9) ----
    const val DEFAULT_SEEK_STEP_MS = 10_000
    const val AUTO_HIDE_CONTROLS_DELAY_MS = 3_000L

    // ---- Toleransi titik resume (spek Veo 2026-11-07) ----
    // Konstanta GLOBAL untuk semua durasi video (bukan persentase):
    // posisi berhenti di bawah RESUME_MIN_POSITION_MS dianggap video
    // belum pernah diputar; posisi di dalam RESUME_END_MARGIN_MS
    // terakhir dianggap video sudah selesai. RESUME_TOLERANCE_MS
    // adalah napas 0,1 detik di kedua batas, menelan ketidakakuratan
    // posisi player yang sesekali meleset sekian milidetik — tanpanya,
    // posisi yang tersimpan mepet ujung membuat video tidak bisa
    // diputar lagi (langsung berakhir lalu lompat ke video berikutnya).
    const val RESUME_MIN_POSITION_MS = 2_000L
    const val RESUME_END_MARGIN_MS = 3_000L
    const val RESUME_TOLERANCE_MS = 100L

    // ---- Zona mati gesture player (perintah Veo 2026-11-07) ----
    // Drag/ketuk yang MULAI di dalam zona ini tidak memicu gesture
    // apa pun (brightness/volume/seek/ketuk): tepi atas supaya tarikan
    // bar notifikasi HP aman, tepi bawah supaya area seekbar + tombol
    // netral. Satuan dp dari tepi layar.
    // Atas ≈ tinggi bar judul (52) + margin 20.
    const val GESTURE_TOP_DEAD_ZONE_DP = 72
    // Bawah ≈ gugus seekbar + tombol (122) + margin 2.
    const val GESTURE_BOTTOM_DEAD_ZONE_DP = 124

    // ---- Ambang mulai drag seek horizontal (RP1, perintah Veo 2026-11-13) ----
    // Gesekan horizontal yang SANGAT kecil harus tetap dibaca sebagai
    // drag seek, bukan ketuk tunggal: dengan touchSlop sistem (±8dp)
    // gesekan di bawah itu jatuh ke jalur tap dan malah membangunkan
    // kontrol. Ambang ini hanya untuk arah horizontal yang dominan;
    // drag vertikal tetap memakai touchSlop sistem.
    const val GESTURE_SEEK_START_DP = 4

    /** Daftar level kecepatan putar (dipakai tombol speed + hold-speed RP5). */
    val SPEED_LEVELS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    // ---- Hold-speed: tekan-tahan untuk kecepatan sementara (RP5, perintah Veo 2026-11-13) ----
    // Jarak geser horizontal untuk satu langkah level speed selagi
    // menahan (geser kiri = turun level, geser kanan = naik level,
    // maks 2.0). Jarak geser ke bawah untuk mengunci speed di level
    // saat itu — shortcut tombol speed: lepas jari pun speed tetap.
    const val HOLD_SPEED_STEP_DP = 48
    const val HOLD_SPEED_LOCK_DP = 48

    /**
     * Jarak turun seekbar mendekati baris tombol di bawahnya (dp,
     * perintah Veo 2026-11-07: turunkan ±50% dari jarak semula).
     * Diubah dari PlayerScreen lewat Modifier.offset.
     */
    const val PLAYER_SEEK_BAR_DROP_DP = 14

    /** Folder tujuan simpan frame capture lewat MediaStore (API 29+). */
    const val CAPTURE_SAVE_FOLDER = "Pictures/AVP"

    /** Nama file DataStore Preferences. */
    const val DATASTORE_NAME = "avp_preferences"

    /** Nama database Room. */
    const val DATABASE_NAME = "avp.db"
}
