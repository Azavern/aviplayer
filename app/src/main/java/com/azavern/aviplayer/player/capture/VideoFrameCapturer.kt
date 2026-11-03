package com.azavern.aviplayer.player.capture

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.azavern.aviplayer.utils.BitmapUtils
import com.azavern.aviplayer.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Pengambil frame video (Blueprint Bagian 7 & Milestone 6).
 *
 * Menggunakan MediaMetadataRetriever — BUKAN ScreenCapture API dan
 * bukan screenshot layar: hasilnya frame mentah dari decoder pada
 * resolusi asli video, tanpa UI overlay dan tanpa black bar.
 */
class VideoFrameCapturer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Ambil frame pada [positionMs]. Perhatian: getFrameAtTime
     * menerima waktu dalam MICROSECONDS — makanya dikali 1000.
     *
     * Opsinya OPTION_CLOSEST (frame-akurat), BUKAN OPTION_CLOSEST_SYNC:
     * varian SYNC mengembalikan keyframe terdekat yang jaraknya bisa
     * 1–2 detik dari posisi tekan — hasil tangkapan terasa "loncat"
     * (laporan Veo 2026-11-04). CLOSEST mendekode sampai frame yang
     * benar-benar terdekat dengan waktu yang diminta.
     */
    suspend fun captureFrame(videoPath: String, positionMs: Long): Result<Bitmap> =
        withContext(Dispatchers.IO) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(videoPath)
                    val rotation = retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
                    )?.toIntOrNull() ?: 0
                    val frame = retriever.getFrameAtTime(
                        positionMs * 1000L,
                        MediaMetadataRetriever.OPTION_CLOSEST
                    ) ?: throw IllegalStateException("Frame tidak tersedia di posisi ini")
                    BitmapUtils.rotateBitmapIfNeeded(frame, rotation)
                } finally {
                    retriever.release()
                }
            }
        }

    /**
     * Simpan frame ke galeri: folder [Constants.CAPTURE_SAVE_FOLDER]
     * (Pictures/AVP), nama `avp_<timestamp>.jpg`, JPEG kualitas 95,
     * lewat pola IS_PENDING MediaStore (API 29+).
     */
    suspend fun saveFrameToGallery(bitmap: Bitmap, videoTitle: String): Result<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                val resolver = context.contentResolver
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "avp_$timestamp.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Constants.CAPTURE_SAVE_FOLDER)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                ) ?: throw IllegalStateException("Gagal membuat entri galeri")
                try {
                    val stream = resolver.openOutputStream(uri)
                        ?: throw IllegalStateException("Gagal membuka keluaran galeri")
                    stream.use { output ->
                        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)) {
                            throw IllegalStateException("Gagal menulis file JPEG")
                        }
                    }
                    val publish = ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    }
                    resolver.update(uri, publish, null, null)
                    uri
                } catch (error: Exception) {
                    // Jangan tinggalkan entri yatim setengah jadi di galeri.
                    resolver.delete(uri, null, null)
                    throw error
                }
            }
        }
}
