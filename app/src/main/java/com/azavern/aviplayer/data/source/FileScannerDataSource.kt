package com.azavern.aviplayer.data.source

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
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
