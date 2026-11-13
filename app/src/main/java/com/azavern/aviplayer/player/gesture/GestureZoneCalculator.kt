package com.azavern.aviplayer.player.gesture

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign

/**
 * Area sentuh player untuk gesture VERTIKAL (koreksi Veo 2026-11-04):
 * layar hanya dibagi DUA — paruh kiri kecerahan, paruh kanan volume.
 *
 * Gesture HORIZONTAL tidak memakai area sama sekali: menggeser ke
 * kiri/kanan di mana pun adalah seek. Yang membedakan aksi adalah
 * ARAH gerakan, bukan posisi mulai. (Blueprint awalnya membagi layar
 * jadi tiga zona dengan seek hanya di tengah — digantikan model ini.)
 *
 * Pemetaan seek-nya non-linear (RP2, perintah Veo 2026-11-13):
 * gesekan kecil = lompatan kecil, gesekan besar = lompatan besar.
 */
enum class GestureZone { BRIGHTNESS, VOLUME, NONE }

/**
 * Utility stateless untuk kalkulasi area dan nilai dari gesture.
 * Semua fungsi murni — mudah diuji.
 */
object GestureZoneCalculator {

    /** Geser selebar layar penuh = seek selebar durasi penuh. */
    private const val SEEK_SENSITIVITY = 1.0f

    /**
     * Pangkat kurva seek non-linear (RP2, perintah Veo 2026-11-13).
     * Fraksi jarak geser dipangkatkan dulu sebelum dikali durasi:
     * 2.0 artinya geser 10% lebar layar ≈ 1% durasi, 50% lebar ≈ 25%
     * durasi, 100% lebar = durasi penuh. Dengan pemetaan linear lama,
     * skip 5 detik pada episode 24 menit butuh gesekan ±0,6 mm —
     * mustahil dikontrol jari (rekor terpendek Veo: 14 detik).
     */
    private const val SEEK_CURVE_EXPONENT = 2.0f

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

    /**
     * Perpindahan horizontal → perpindahan posisi putar (ms).
     * Kurva non-linear (RP2): gesekan sangat kecil terasa halus
     * (skip di bawah 5 detik jadi mungkin), gesekan lebar tetap bisa
     * melompat jauh. Tanda arah dipertahankan; pratinjau di
     * ViewModel dijepit ke [0, durasi], jadi nilai di luar rentang
     * tidak pernah lolos.
     */
    fun calculateSeekDeltaMs(dx: Float, screenWidth: Float, durationMs: Long): Long {
        if (screenWidth <= 0f || durationMs <= 0L) return 0L
        val fraction = (dx / screenWidth).coerceIn(-1f, 1f)
        val curved = sign(fraction) * abs(fraction).pow(SEEK_CURVE_EXPONENT)
        return (curved * durationMs * SEEK_SENSITIVITY).toLong()
    }
}
