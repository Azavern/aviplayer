package com.azavern.aviplayer.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.R
import com.azavern.aviplayer.data.model.VideoItem
import com.azavern.aviplayer.utils.FileUtils
import com.azavern.aviplayer.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog-dialog aksi file dari menu titik-tiga video (permintaan
 * Veo 2026-11-04): ganti nama, informasi file, dan hapus file.
 */

/** Ganti nama file: hanya nama dasar yang diedit; ekstensi dipertahankan. */
@Composable
fun RenameVideoDialog(
    video: VideoItem,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(video.path) { mutableStateOf(video.name) }
    val extension = video.fileName.substringAfterLast('.', "")
    val canSave = name.trim().isNotEmpty() && name.trim() != video.name

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_rename_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.home_rename_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (extension.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.home_rename_extension_note, extension),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = canSave) {
                Text(stringResource(R.string.home_dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_dialog_cancel))
            }
        }
    )
}

/** Informasi lengkap satu file video, dari data MediaStore yang sudah dimuat. */
@Composable
fun FileInfoDialog(
    video: VideoItem,
    onDismiss: () -> Unit
) {
    val extension = video.fileName.substringAfterLast('.', "")
    val resolutionLabel = FileUtils.getResolutionLabel(video.width, video.height)
    val resolution = when {
        video.width > 0 && video.height > 0 ->
            "${video.width} × ${video.height}" +
                if (resolutionLabel.isNotEmpty()) " ($resolutionLabel)" else ""
        resolutionLabel.isNotEmpty() -> resolutionLabel
        else -> "-"
    }
    // DATE_ADDED MediaStore dalam detik sejak epoch.
    val addedDate = remember(video.dateAdded) {
        SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
            .format(Date(video.dateAdded * 1000L))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_info_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                InfoField(stringResource(R.string.home_info_name), video.fileName)
                InfoField(stringResource(R.string.home_info_location), video.folderPath)
                InfoField(
                    stringResource(R.string.home_info_size),
                    FileUtils.getFileSizeReadable(video.sizeBytes)
                )
                InfoField(
                    stringResource(R.string.home_info_duration),
                    TimeUtils.msToDurationReadable(video.durationMs)
                )
                InfoField(stringResource(R.string.home_info_resolution), resolution)
                if (extension.isNotEmpty()) {
                    InfoField(
                        stringResource(R.string.home_info_format),
                        extension.uppercase()
                    )
                }
                InfoField(stringResource(R.string.home_info_added), addedDate)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_info_close))
            }
        }
    )
}

@Composable
private fun InfoField(label: String, value: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
}

/** Ringkasan info untuk pilihan banyak file di mode seleksi. */
@Composable
fun SelectionInfoDialog(
    videos: List<VideoItem>,
    onDismiss: () -> Unit
) {
    val totalSize = videos.sumOf { it.sizeBytes }
    val totalDuration = videos.sumOf { it.durationMs }
    val folders = videos.map { it.folderPath }.distinct()
    val location = if (folders.size == 1) {
        videos.first().folderName
    } else {
        stringResource(R.string.home_selection_info_folders, folders.size)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_selection_info_title)) },
        text = {
            Column {
                InfoField(
                    stringResource(R.string.home_selection_info_count),
                    videos.size.toString()
                )
                InfoField(
                    stringResource(R.string.home_selection_info_total_size),
                    FileUtils.getFileSizeReadable(totalSize)
                )
                InfoField(
                    stringResource(R.string.home_selection_info_total_duration),
                    TimeUtils.msToDurationReadable(totalDuration)
                )
                InfoField(stringResource(R.string.home_info_location), location)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_info_close))
            }
        }
    )
}

/** Konfirmasi hapus batch dari mode seleksi: semua file terpilih hilang permanen. */
@Composable
fun DeleteSelectionDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_delete_selection_title, count)) },
        text = { Text(stringResource(R.string.home_delete_selection_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.home_action_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_dialog_cancel))
            }
        }
    )
}

/** Konfirmasi hapus: file hilang permanen dari perangkat. */
@Composable
fun DeleteVideoDialog(
    video: VideoItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_delete_title)) },
        text = { Text(stringResource(R.string.home_delete_message, video.fileName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.home_action_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_dialog_cancel))
            }
        }
    )
}
