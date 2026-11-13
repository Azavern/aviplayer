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
 *
 * Satu retriever dipakai ulang per video (RP4, perintah Veo
 * 2026-11-13): sebelumnya setiap capture membuat retriever +
 * setDataSource baru, sehingga capture pertama sesudah jeda membayar
 * biaya cold init (decoder native + buka file + baca metadata) dan
 * terasa delay — capture berikutnya kebetulan cache OS-nya masih
 * hangat. Sekarang retriever disiapkan sekali saat video dimuat
 * ([prepare]) dan dipakai ulang; posisi frame tetap dikunci saat
 * tombol ditekan, jadi akurasi tidak berubah.
 *
 * Sinkronisasi memakai `synchronized`, bukan Mutex: [release] harus
 * bisa dipanggil sinkron dari ViewModel.onCleared (tanpa coroutine),
 * dan getFrameAtTime memang sudah berjalan di Dispatchers.IO
 * sehingga menahan satu thread pool sebentar bukan masalah.
 * MediaMetadataRetriever tidak aman dipakai dari dua thread
 * sekaligus — semua akses lewat kunci ini.
 */
class VideoFrameCapturer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val retrieverLock = Any()
    private var retriever: MediaMetadataRetriever? = null
    private var preparedPath: String? = null
    private var preparedRotation: Int = 0

    /**
     * Siapkan retriever untuk [videoPath]: buka file + baca rotasi
     * sekali, di latar. Panggilan ulang dengan path yang sama =
     * no-op murah. Dipanggil ViewModel sesudah prepare player — dan
     * sesudah onStart bila player sempat di-release.
     */
    suspend fun prepare(videoPath: String) {
        withContext(Dispatchers.IO) {
            synchronized(retrieverLock) {
                if (preparedPath == videoPath && retriever != null) return@synchronized
                ensurePreparedLocked(videoPath)
            }
        }
    }

    /** Lepas retriever (saat player ditutup). Boleh dipanggil sinkron. */
    fun release() {
        synchronized(retrieverLock) {
            releaseLocked()
        }
    }

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
                val (frame, rotation) = synchronized(retrieverLock) {
                    // Belum dipanaskan (mis. capture ditekan sebelum
                    // prepare selesai): siapkan sekarang — sama seperti
                    // perilaku lama, hanya lebih lambat sekali ini.
                    if (preparedPath != videoPath || retriever == null) {
                        ensurePreparedLocked(videoPath)
                    }
                    val active = retriever
                        ?: throw IllegalStateException("Retriever tidak tersedia")
                    val decoded = active.getFrameAtTime(
                        positionMs * 1000L,
                        MediaMetadataRetriever.OPTION_CLOSEST
                    ) ?: throw IllegalStateException("Frame tidak tersedia di posisi ini")
                    // Rotasi dibaca di dalam kunci yang sama, supaya
                    // tidak tertukar bila video berganti di tengah jalan.
                    decoded to preparedRotation
                }
                BitmapUtils.rotateBitmapIfNeeded(frame, rotation)
            }
        }

    /**
     * Buat + setDataSource retriever baru untuk [videoPath].
     * WAJIB dipanggil dengan [retrieverLock] dipegang.
     */
    private fun ensurePreparedLocked(videoPath: String) {
        releaseLocked()
        val fresh = MediaMetadataRetriever()
        try {
            fresh.setDataSource(videoPath)
            preparedRotation = fresh.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
            )?.toIntOrNull() ?: 0
            retriever = fresh
            preparedPath = videoPath
        } catch (error: Exception) {
            fresh.release()
            throw error
        }
    }

    /** WAJIB dipanggil dengan [retrieverLock] dipegang. */
    private fun releaseLocked() {
        retriever?.release()
        retriever = null
        preparedPath = null
        preparedRotation = 0
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
