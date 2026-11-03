package com.azavern.aviplayer.ui.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.azavern.aviplayer.R
import com.azavern.aviplayer.ui.home.HomeTab

/**
 * Navigasi bawah 3 tab (Blueprint Bagian 7): Video, Folder, Playlist.
 * Tab aktif memakai ikon filled + warna aksen tema; nonaktif outline.
 */
@Composable
fun BottomNavBar(
    activeTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = activeTab == HomeTab.VIDEO,
            onClick = { onTabSelected(HomeTab.VIDEO) },
            icon = {
                Icon(
                    if (activeTab == HomeTab.VIDEO) Icons.Filled.VideoLibrary
                    else Icons.Outlined.VideoLibrary,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.home_tab_video)) }
        )
        NavigationBarItem(
            selected = activeTab == HomeTab.FOLDER,
            onClick = { onTabSelected(HomeTab.FOLDER) },
            icon = {
                Icon(
                    if (activeTab == HomeTab.FOLDER) Icons.Filled.Folder
                    else Icons.Outlined.Folder,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.home_tab_folder)) }
        )
        NavigationBarItem(
            selected = activeTab == HomeTab.PLAYLIST,
            onClick = { onTabSelected(HomeTab.PLAYLIST) },
            icon = { Icon(Icons.Filled.PlaylistPlay, contentDescription = null) },
            label = { Text(stringResource(R.string.home_tab_playlist)) }
        )
    }
}
