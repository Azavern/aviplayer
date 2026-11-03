package com.azavern.aviplayer.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.azavern.aviplayer.R
import com.azavern.aviplayer.data.model.PlaylistEntry
import com.azavern.aviplayer.data.model.PlaylistInfo
import com.azavern.aviplayer.utils.TimeUtils

/**
 * Detail satu playlist (Milestone 10): daftar entri sesuai urutan,
 * tombol Putar Semua, keluarkan entri, dan ubah urutan dengan
 * tekan-lama-lalu-seret (drag reorder). Ketuk entri memutar mulai
 * dari entri itu — antrean player mengikuti urutan playlist.
 */
@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistInfo,
    entries: List<PlaylistEntry>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onEntryClick: (PlaylistEntry) -> Unit,
    onRemoveEntry: (PlaylistEntry) -> Unit,
    onMoveEntry: (Int, Int) -> Unit,
    onReorderFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.player_action_back)
                )
            }
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onPlayAll,
                enabled = entries.isNotEmpty()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text(
                    text = stringResource(R.string.playlist_play_all),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.playlist_drag_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.playlist_empty_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
                    val isDragging = index == draggingIndex
                    val latestIndex by rememberUpdatedState(index)
                    val latestOnMove by rememberUpdatedState(onMoveEntry)
                    val latestOnFinish by rememberUpdatedState(onReorderFinished)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (isDragging) dragOffsetPx else 0f
                            }
                            .background(
                                if (isDragging) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    Color.Transparent
                                }
                            )
                            .pointerInput(entry.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingIndex = latestIndex
                                        dragOffsetPx = 0f
                                    },
                                    onDragEnd = {
                                        draggingIndex = null
                                        dragOffsetPx = 0f
                                        latestOnFinish()
                                    },
                                    onDragCancel = {
                                        draggingIndex = null
                                        dragOffsetPx = 0f
                                        latestOnFinish()
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetPx += dragAmount.y
                                        val current = draggingIndex ?: latestIndex
                                        val itemSize = listState.layoutInfo
                                            .visibleItemsInfo
                                            .firstOrNull { it.index == current }
                                            ?.size ?: 0
                                        if (itemSize > 0) {
                                            when {
                                                dragOffsetPx > itemSize &&
                                                    current < entries.lastIndex -> {
                                                    latestOnMove(current, current + 1)
                                                    draggingIndex = current + 1
                                                    dragOffsetPx -= itemSize
                                                }
                                                dragOffsetPx < -itemSize && current > 0 -> {
                                                    latestOnMove(current, current - 1)
                                                    draggingIndex = current - 1
                                                    dragOffsetPx += itemSize
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            .clickable { onEntryClick(entry) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = entry.videoPath,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(96.dp)
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = entry.videoTitle,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = TimeUtils.msToTimestamp(entry.durationMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        IconButton(onClick = { onRemoveEntry(entry) }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.playlist_remove_entry)
                            )
                        }
                    }
                }
            }
        }
    }
}
