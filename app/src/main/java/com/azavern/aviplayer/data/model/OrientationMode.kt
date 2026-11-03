package com.azavern.aviplayer.data.model

/**
 * Mode orientasi Player (Blueprint Bagian 8 & 10F). Diterapkan
 * PlayerActivity sebagai `requestedOrientation`; BUKAN kunci Manifest.
 */
enum class OrientationMode {
    AUTO,       // ikuti perangkat: requestedOrientation = SCREEN_ORIENTATION_FULL_USER
                // (sensor aktif hanya jika kunci rotasi HP mati; jika kunci rotasi HP nyala, layar diam)
    LANDSCAPE,  // paksa landscape — mengalahkan kunci rotasi HP
    PORTRAIT    // paksa portrait — mengalahkan kunci rotasi HP
}
