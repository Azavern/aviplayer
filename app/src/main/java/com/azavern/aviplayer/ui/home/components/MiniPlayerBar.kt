package com.azavern.aviplayer.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.azavern.aviplayer.R

/**
 * Bar video terakhir diputar, menempel di atas navigasi bawah
 * (referensi Veo 2026-11-03, bagian opsional). Tap bar / tombol putar
 * membuka PlayerActivity pada video itu — resume posisi mengurus
 * sisanya (Milestone 2). Tombol ✕ menyembunyikannya sampai ada video
 * lain yang diputar.
 *
 * Sengaja TANPA tombol "berikutnya": belum ada antrean/playlist,
 * jadi tombol itu akan jadi tombol palsu.
 */
@Composable
fun MiniPlayerBar(
    title: String,
    thumbnailPath: String?,
    onPlayClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable { onPlayClick() }
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.home_mini_player_close)
                )
            }
            AsyncImage(
                model = thumbnailPath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(56.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            )
            IconButton(onClick = onPlayClick) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.home_action_play),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
