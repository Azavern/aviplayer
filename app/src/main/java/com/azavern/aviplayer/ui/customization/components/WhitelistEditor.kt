package com.azavern.aviplayer.ui.customization.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azavern.aviplayer.R
import com.azavern.aviplayer.data.model.FolderFilterMode
import com.azavern.aviplayer.data.model.FolderItem
import com.azavern.aviplayer.data.model.FolderVisibilityConfig

/**
 * Editor whitelist folder (Milestone 9): switch mode di atas,
 * daftar semua folder dengan centang di bawahnya. Saat mode mati,
 * daftar tampak redup dan tidak bisa diubah (pilihan centang tetap
 * tersimpan untuk saat mode dinyalakan lagi). Penyaringan sendiri
 * terjadi di FolderRepository — editor hanya mengubah config.
 */
@Composable
fun WhitelistEditor(
    config: FolderVisibilityConfig,
    allFolders: List<FolderItem>,
    onConfigChanged: (FolderVisibilityConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val whitelistOnly = config.mode == FolderFilterMode.WHITELIST_ONLY

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.whitelist_mode_label),
                style = MaterialTheme.typography.titleSmall
            )
            Switch(
                checked = whitelistOnly,
                onCheckedChange = { enabled ->
                    onConfigChanged(
                        config.copy(
                            mode = if (enabled) {
                                FolderFilterMode.WHITELIST_ONLY
                            } else {
                                FolderFilterMode.ALL
                            }
                        )
                    )
                }
            )
        }
        Text(
            text = stringResource(R.string.whitelist_info),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (allFolders.isEmpty()) {
            Text(
                text = stringResource(R.string.whitelist_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp)
                    .alpha(if (whitelistOnly) 1f else 0.38f)
            ) {
                items(allFolders, key = { it.path }) { folder ->
                    val checked = folder.path in config.whitelistedPaths
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = whitelistOnly) {
                                onConfigChanged(
                                    config.copy(
                                        whitelistedPaths = if (checked) {
                                            config.whitelistedPaths - folder.path
                                        } else {
                                            config.whitelistedPaths + folder.path
                                        }
                                    )
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null,
                            enabled = whitelistOnly
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${folder.path} · " +
                                    stringResource(R.string.whitelist_video_count, folder.videoCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
