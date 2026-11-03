package com.azavern.aviplayer.utils

import java.security.MessageDigest
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

/**
 * Utilitas file murni (Blueprint Bagian 7): identitas video yang stabil
 * dan format ukuran yang bisa dibaca manusia. Tanpa dependensi Android
 * selain JDK, supaya mudah diuji.
 */
object FileUtils {

    /**
     * ID stabil satu video = MD5 hex dari path absolutnya.
     * Dipakai sebagai primary key `playback_state` dan id VideoItem.
     */
    fun generateVideoId(path: String): String {
        val digest = MessageDigest.getInstance("MD5")
            .digest(path.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }

    /**
     * Format ukuran: `512 B`, `12,4 KB`, `1,05 GB`.
     * Desimal memakai format Indonesia (koma).
     */
    fun getFileSizeReadable(sizeBytes: Long): String {
        if (sizeBytes <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(sizeBytes.toDouble()) / log10(1024.0)).toInt()
            .coerceIn(0, units.lastIndex)
        val value = sizeBytes / 1024.0.pow(digitGroups.toDouble())
        return if (digitGroups == 0) {
            "$sizeBytes B"
        } else {
            String.format(Locale.forLanguageTag("id-ID"), "%.1f %s", value, units[digitGroups])
        }
    }

    /**
     * Label resolusi ala pemutar video ("720P", "1080P") dari dimensi
     * piksel — memakai sisi pendek frame (tinggi pada video landscape,
     * lebar pada portrait). String kosong bila dimensi tidak diketahui.
     */
    fun getResolutionLabel(width: Int, height: Int): String {
        val shortSide = minOf(width, height)
        return when {
            shortSide <= 0 -> ""
            shortSide <= 240 -> "240P"
            shortSide <= 360 -> "360P"
            shortSide <= 480 -> "480P"
            shortSide <= 720 -> "720P"
            shortSide <= 1080 -> "1080P"
            shortSide <= 1440 -> "1440P"
            else -> "2160P"
        }
    }

    /**
     * Normalisasi path untuk perbandingan (dipakai FolderRepository):
     * buang spasi tepi, gabungkan slash ganda, buang slash di akhir
     * (kecuali root "/"). Case TIDAK diubah — path Android case-sensitive.
     */
    fun normalizePath(path: String): String {
        val collapsed = path.trim().replace(Regex("/{2,}"), "/")
        return if (collapsed.length > 1) collapsed.trimEnd('/') else collapsed
    }
}
