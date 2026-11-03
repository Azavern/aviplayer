package com.azavern.aviplayer.utils

import android.graphics.Bitmap
import android.graphics.Matrix

/**
 * Utilitas bitmap murni (Blueprint Milestone 6).
 */
object BitmapUtils {

    /**
     * Putar bitmap sesuai metadata rotasi video (0/90/180/270).
     * Video portrait sering disimpan landscape dengan bendera rotasi;
     * frame mentahnya harus diputar supaya tidak miring saat disimpan.
     * Mengembalikan bitmap yang sama bila tidak perlu diputar.
     */
    fun rotateBitmapIfNeeded(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val normalized = ((rotationDegrees % 360) + 360) % 360
        if (normalized == 0) return bitmap
        val matrix = Matrix().apply { postRotate(normalized.toFloat()) }
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }
}
