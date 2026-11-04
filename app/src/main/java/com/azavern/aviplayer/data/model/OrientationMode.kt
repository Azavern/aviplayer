package com.azavern.aviplayer.data.model

/**
 * Mode orientasi Player (Blueprint Bagian 8 & 10F). Diterapkan
 * PlayerActivity sebagai `requestedOrientation`; BUKAN kunci Manifest.
 *
 * Keluarga SENSOR_* sengaja dipilih (koreksi Veo 2026-11-04):
 * berbeda dari keluarga USER/FULL_USER, mode sensor membaca gerak
 * perangkat TANPA memandang kunci rotasi HP — video player memang
 * harus bisa memutar layar walau rotasi otomatis sistem mati.
 */
enum class OrientationMode {
    AUTO,       // ikuti sensor perangkat: SCREEN_ORIENTATION_SENSOR
                // (portrait/landscape dua arah; mengabaikan kunci rotasi HP)
    LANDSCAPE,  // landscape dua arah mengikuti sensor:
                // SCREEN_ORIENTATION_SENSOR_LANDSCAPE — kepala HP boleh
                // di kiri atau di kanan, layar membalik mengikuti pegangan;
                // mengabaikan kunci rotasi HP
    PORTRAIT    // paksa portrait — mengalahkan kunci rotasi HP
}
