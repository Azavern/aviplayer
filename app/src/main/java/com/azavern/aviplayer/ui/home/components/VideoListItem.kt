package com.azavern.aviplayer.ui.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.azavern.aviplayer.R
import com.azavern.aviplayer.data.model.VideoItem
import com.azavern.aviplayer.utils.FileUtils
import com.azavern.aviplayer.utils.TimeUtils

/**
 * Satu baris video di daftar Beranda — layout mengikuti referensi
 * Veo (2026-11-03):
 *
 *   [thumbnail 16:9]  Judul (2 baris; berwarna aksen bila video
 *    durasi di kanan    terakhir diputar)
 *    bawah thumbnail   720P  188,19 MB
 *   [bar progres tipis di tepi bawah thumbnail bila sudah ditonton sebagian]
 *
 * Thumbnail diambil Coil dari frame video itu sendiri (VideoFrameDecoder).
 *
 * Tekan-tahan di mana pun pada baris ini (kecuali tombol titik-tiga)
 * masuk ke mode seleksi banyak video; di mode itu baris menampilkan
 * lingkaran di kiri — terisi warna aksen + nomor urut memilih bila
 * video ini terpilih (semantik Veo 2026-11-04).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoListItem(
    video: VideoItem,
    progressFraction: Float?,
    isLastPlayed: Boolean,
    selectionMode: Boolean,
    selectionIndex: Int?,
    onVideoClick: (VideoItem) -> Unit,
    onPlayClick: (VideoItem) -> Unit,
    onLongPress: (VideoItem) -> Unit,
    onAddToPlaylistClick: (VideoItem) -> Unit,
    onSelectClick: (VideoItem) -> Unit,
    onRenameClick: (VideoItem) -> Unit,
    onInfoClick: (VideoItem) -> Unit,
    onDeleteClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isSelected = selectionIndex != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = { onVideoClick(video) },
                onLongClick = { onLongPress(video) }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectionMode) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        }
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selectionIndex != null) {
                    Text(
                        text = selectionIndex.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
        Box(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = video.thumbnailPath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = TimeUtils.msToTimestamp(video.durationMs),
                color = Color.White,
                style = TextStyle(
                    fontSize = 11.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(0f, 0f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 4.dp)
            )
            if (progressFraction != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.35f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = video.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isLastPlayed) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val resolution = FileUtils.getResolutionLabel(video.width, video.height)
            val metadata = listOf(resolution, FileUtils.getFileSizeReadable(video.sizeBytes))
                .filter { it.isNotEmpty() }
                .joinToString(separator = "  ")
            Text(
                text = metadata,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = null)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_action_play)) },
                    onClick = {
                        menuExpanded = false
                        onPlayClick(video)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_action_select)) },
                    onClick = {
                        menuExpanded = false
                        onSelectClick(video)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.playlist_add_to)) },
                    onClick = {
                        menuExpanded = false
                        onAddToPlaylistClick(video)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_action_rename)) },
                    onClick = {
                        menuExpanded = false
                        onRenameClick(video)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_action_info)) },
                    onClick = {
                        menuExpanded = false
                        onInfoClick(video)
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.home_action_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDeleteClick(video)
                    }
                )
            }
        }
    }
}
