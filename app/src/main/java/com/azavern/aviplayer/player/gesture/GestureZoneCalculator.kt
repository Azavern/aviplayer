package com.azavern.aviplayer.player.gesture

/**
 * Zona sentuh player (Blueprint Bagian 7): sepertiga kiri layar untuk
 * kecerahan, sepertiga kanan untuk volume, tengah untuk seek.
 */
enum class GestureZone { BRIGHTNESS, SEEK, VOLUME, NONE }

/**
 * Utility stateless untuk kalkulasi zona dan nilai dari gesture
 * (Blueprint Bagian 7). Semua fungsi murni — mudah diuji.
 */
object GestureZoneCalculator {

    /** Geser selebar layar penuh = seek selebar durasi penuh. */
    private const val SEEK_SENSITIVITY = 1.0f

    fun getZone(x: Float, screenWidth: Float): GestureZone {
        if (screenWidth <= 0f) return GestureZone.NONE
        return when {
            x < screenWidth * 0.33f -> GestureZone.BRIGHTNESS
            x > screenWidth * 0.67f -> GestureZone.VOLUME
            else -> GestureZone.SEEK
        }
    }

    /** Drag ke atas = lebih terang. Hasil dibatasi -1f..1f. */
    fun calculateBrightnessDelta(dy: Float, screenHeight: Float): Float {
        if (screenHeight <= 0f) return 0f
        return (dy / screenHeight * -1f).coerceIn(-1f, 1f)
    }

    /** Drag ke atas = lebih keras. Hasil dibatasi -1f..1f. */
    fun calculateVolumeDelta(dy: Float, screenHeight: Float): Float {
        if (screenHeight <= 0f) return 0f
        return (dy / screenHeight * -1f).coerceIn(-1f, 1f)
    }

    /** Perpindahan horizontal → perpindahan posisi putar (ms). */
    fun calculateSeekDeltaMs(dx: Float, screenWidth: Float, durationMs: Long): Long {
        if (screenWidth <= 0f || durationMs <= 0L) return 0L
        return (dx / screenWidth * durationMs * SEEK_SENSITIVITY).toLong()
    }
}
