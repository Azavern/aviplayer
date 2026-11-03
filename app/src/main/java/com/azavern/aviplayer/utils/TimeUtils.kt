package com.azavern.aviplayer.utils

import java.util.Locale

/** Utilitas format waktu (Blueprint Bagian 7). */
object TimeUtils {

    /**
     * Format milidetik menjadi timestamp: "01:23:45" bila durasi ≥ 1 jam,
     * "23:45" bila di bawah 1 jam.
     */
    fun msToTimestamp(ms: Long): String {
        val totalSeconds = (ms.coerceAtLeast(0L)) / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }

    /** Format durasi terbaca: "1j 23m" atau "23 mnt". */
    fun msToDurationReadable(ms: Long): String {
        val totalMinutes = (ms.coerceAtLeast(0L)) / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}j ${minutes}m" else "$minutes mnt"
    }
}
