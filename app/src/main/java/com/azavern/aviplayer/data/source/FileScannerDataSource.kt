package com.azavern.aviplayer.data.source

import android.Manifest
import android.annotation.SuppressLint
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.azavern.aviplayer.data.model.FolderItem
import com.azavern.aviplayer.data.model.VideoItem
import com.azavern.aviplayer.utils.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Sumber data tunggal untuk scan file dari MediaStore
 * (Blueprint Bagian 7). Semua query berjalan sinkron — pemanggil
 * (repository) bertanggung jawab memindahkannya ke Dispatchers.IO.
 *
 * Tanpa izin baca media, semua fungsi mengembalikan list kosong
 * (bukan crash), sesuai spek.
 *
 * Filter ekstensi (pagar dari Veo, 2026-11-03): hanya file .mp4, .mkv,
 * dan .mov yang dikembalikan. Ekstensi lain yang terdaftar di
 * MediaStore sebagai video (mis. .ts dari folder unduhan bot) disaring
 * di [queryVideos], sehingga tidak muncul di daftar video, daftar
 * folder, isi folder, maupun pencarian.
 */
class FileScannerDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Semua video di perangkat, terbaru lebih dulu (DATE_ADDED DESC). */
    fun scanAllVideos(): List<VideoItem> {
        if (!hasReadPermission()) return emptyList()
        return queryVideos(selection = null, selectionArgs = null)
    }

    /** Video yang folder induknya persis [folderPath]. */
    fun scanVideosByFolder(folderPath: String): List<VideoItem> {
        if (!hasReadPermission()) return emptyList()
        val normalized = FileUtils.normalizePath(folderPath)
        // Prefilter LIKE di SQL, lalu saring folder induk yang persis sama
        // (LIKE saja akan ikut mengambil subfolder).
        return queryVideos(
            selection = "${MediaStore.Video.Media.DATA} LIKE ?",
            selectionArgs = arrayOf("$normalized/%")
        ).filter { it.folderPath == normalized }
    }

    /** Satu video berdasarkan path persis; null bila tidak ada / tanpa izin. */
    fun getVideoByPath(path: String): VideoItem? {
        if (!hasReadPermission()) return null
        return queryVideos(
            selection = "${MediaStore.Video.Media.DATA} = ?",
            selectionArgs = arrayOf(path)
        ).firstOrNull()
    }

    /**
     * Ganti nama file video di perangkat (DISPLAY_NAME MediaStore;
     * file fisik ikut berpindah nama di folder yang sama).
     *
     * File milik aplikasi lain (mis. unduhan WhatsApp) tidak bisa
     * ditulis begitu saja di Android 10+: sistem menuntut persetujuan
     * pengguna, dikembalikan sebagai [MediaWriteResult.NeedsConsent]
     * supaya UI menampilkan dialog sistemnya, lalu operasi ini
     * dipanggil ulang setelah persetujuan diberikan.
     */
    fun renameVideo(path: String, newDisplayName: String): MediaWriteResult {
        val uri = uriForPath(path) ?: return MediaWriteResult.Failed
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, newDisplayName)
        }
        return try {
            val updated = context.contentResolver.update(uri, values, null, null)
            if (updated > 0) MediaWriteResult.Success else MediaWriteResult.Failed
        } catch (e: RecoverableSecurityException) {
            MediaWriteResult.NeedsConsent(e.userAction.actionIntent.intentSender)
        } catch (e: SecurityException) {
            writeRequestConsent(uri) ?: MediaWriteResult.Failed
        }
    }

    /** Hapus file video dari perangkat lewat MediaStore; aturan persetujuan sama seperti [renameVideo]. */
    fun deleteVideo(path: String): MediaWriteResult {
        val uri = uriForPath(path) ?: return MediaWriteResult.Failed
        return try {
            val deleted = context.contentResolver.delete(uri, null, null)
            if (deleted > 0) MediaWriteResult.Success else MediaWriteResult.Failed
        } catch (e: RecoverableSecurityException) {
            MediaWriteResult.NeedsConsent(e.userAction.actionIntent.intentSender)
        } catch (e: SecurityException) {
            writeRequestConsent(uri) ?: MediaWriteResult.Failed
        }
    }

    /** URI MediaStore satu file berdasarkan path persisnya; null bila tak terdaftar. */
    private fun uriForPath(path: String): Uri? {
        val projection = arrayOf(MediaStore.Video.Media._ID)
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Video.Media.DATA} = ?",
            arrayOf(path),
            null
        )?.use { c ->
            if (c.moveToFirst()) {
                val id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                return ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )
            }
        }
        return null
    }

    /**
     * IntentSender persetujuan tulis untuk BANYAK file sekaligus —
     * satu dialog sistem untuk semuanya (Android 11+). Null di
     * Android 10 atau bila tak satu pun path terdaftar di MediaStore.
     */
    @SuppressLint("NewApi")
    fun createWriteConsentSender(paths: List<String>): IntentSender? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val uris = paths.mapNotNull { uriForPath(it) }
        if (uris.isEmpty()) return null
        return MediaStore.createWriteRequest(context.contentResolver, uris).intentSender
    }

    /** Dialog persetujuan tulis bawaan Android 11+ (createWriteRequest). */
    @SuppressLint("NewApi")
    private fun writeRequestConsent(uri: Uri): MediaWriteResult.NeedsConsent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val pendingIntent = MediaStore.createWriteRequest(
            context.contentResolver, listOf(uri)
        )
        return MediaWriteResult.NeedsConsent(pendingIntent.intentSender)
    }

    /**
     * Semua folder yang berisi video: dikelompokkan dari satu query
     * video (BUCKET_DISPLAY_NAME untuk nama, path induk untuk kunci).
     */
    fun scanAllFolders(): List<FolderItem> {
        val videos = scanAllVideos()
        return videos
            .groupBy { it.folderPath }
            .map { (folderPath, folderVideos) ->
                FolderItem(
                    path = folderPath,
                    name = folderVideos.first().folderName,
                    videoCount = folderVideos.size,
                    thumbnailPath = folderVideos.first().path
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    private fun hasReadPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun queryVideos(selection: String?, selectionArgs: Array<String>?): List<VideoItem> {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )
        val results = mutableListOf<VideoItem>()
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )
        cursor?.use { c ->
            val nameIdx = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dataIdx = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val durationIdx = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeIdx = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val bucketIdx = c.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val dateIdx = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val widthIdx = c.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightIdx = c.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            while (c.moveToNext()) {
                val path = c.getString(dataIdx) ?: continue
                val fileName = c.getString(nameIdx) ?: File(path).name
                val extension = fileName.substringAfterLast('.', "").lowercase()
                if (extension !in ALLOWED_EXTENSIONS) continue
                val folderPath = File(path).parent ?: ""
                results += VideoItem(
                    id = FileUtils.generateVideoId(path),
                    name = fileName.substringBeforeLast('.', fileName),
                    fileName = fileName,
                    path = path,
                    durationMs = c.getLong(durationIdx),
                    sizeBytes = c.getLong(sizeIdx),
                    folderName = c.getString(bucketIdx) ?: File(folderPath).name.orEmpty(),
                    folderPath = folderPath,
                    dateAdded = c.getLong(dateIdx),
                    thumbnailPath = path,
                    width = c.getInt(widthIdx),
                    height = c.getInt(heightIdx)
                )
            }
        }
        return results
    }

    companion object {
        /** Satu-satunya ekstensi yang dirender aplikasi (pagar Veo 2026-11-03). */
        private val ALLOWED_EXTENSIONS = setOf("mp4", "mkv", "mov")
    }
}

/**
 * Hasil operasi tulis MediaStore (ganti nama / hapus file).
 * [NeedsConsent] membawa IntentSender dialog persetujuan sistem —
 * tampilkan lewat Activity Result, lalu ulangi operasinya.
 */
sealed class MediaWriteResult {
    data object Success : MediaWriteResult()
    data class NeedsConsent(val intentSender: IntentSender) : MediaWriteResult()
    data object Failed : MediaWriteResult()
}
