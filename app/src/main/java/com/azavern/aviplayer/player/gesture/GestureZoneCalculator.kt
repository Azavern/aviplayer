package com.azavern.aviplayer.player.gesture

/**
 * Area sentuh player untuk gesture VERTIKAL (koreksi Veo 2026-11-04):
 * layar hanya dibagi DUA — paruh kiri kecerahan, paruh kanan volume.
 *
 * Gesture HORIZONTAL tidak memakai area sama sekali: menggeser ke
 * kiri/kanan di mana pun adalah seek. Yang membedakan aksi adalah
 * ARAH gerakan, bukan posisi mulai. (Blueprint awalnya membagi layar
 * jadi tiga zona dengan seek hanya di tengah — digantikan model ini.)
 */
enum class GestureZone { BRIGHTNESS, VOLUME, NONE }

/**
 * Utility stateless untuk kalkulasi area dan nilai dari gesture.
 * Semua fungsi murni — mudah diuji.
 */
object GestureZoneCalculator {

    /** Geser selebar layar penuh = seek selebar durasi penuh. */
    private const val SEEK_SENSITIVITY = 1.0f

    /** Paruh layar tempat gesture dimulai: kiri = brightness, kanan = volume. */
    fun getZone(x: Float, screenWidth: Float): GestureZone {
        if (screenWidth <= 0f) return GestureZone.NONE
        return if (x < screenWidth * 0.5f) GestureZone.BRIGHTNESS else GestureZone.VOLUME
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
