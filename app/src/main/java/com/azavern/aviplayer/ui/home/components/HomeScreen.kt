package com.azavern.aviplayer.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azavern.aviplayer.R
import com.azavern.aviplayer.ui.home.HomeNavigation
import com.azavern.aviplayer.ui.home.HomeTab
import com.azavern.aviplayer.ui.home.HomeViewModel

/**
 * Root composable Beranda (Blueprint Bagian 7): TopAppBar + konten
 * tab aktif + BottomNavBar. Event navigasi dari state diteruskan ke
 * Activity lewat [onNavigateToPlayer], lalu dibersihkan.
 *
 * Tanpa izin baca media, konten diganti layar permintaan izin —
 * daftar video memang tidak bisa dimuat tanpa izin itu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPlayer: (path: String, id: String, title: String) -> Unit,
    onRequestPermission: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigation) {
        val navigation = uiState.navigation
        if (navigation is HomeNavigation.ToPlayer) {
            onNavigateToPlayer(navigation.path, navigation.id, navigation.title)
            viewModel.onNavigationHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
        bottomBar = {
            BottomNavBar(
                activeTab = uiState.activeTab,
                onTabSelected = viewModel::onTabSelected
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                !uiState.hasPermission -> PermissionContent(onRequestPermission)
                else -> when (uiState.activeTab) {
                    HomeTab.VIDEO -> Column(modifier = Modifier.fillMaxSize()) {
                        SearchBarComponent(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChanged
                        )
                        VideoListScreen(
                            videos = uiState.videos,
                            isLoading = uiState.isLoading,
                            activeFolder = uiState.activeFolder,
                            onVideoClick = viewModel::onVideoClicked,
                            onCloseFolder = viewModel::onCloseFolder
                        )
                    }
                    HomeTab.FOLDER -> FolderGridScreen(
                        folders = uiState.folders,
                        isLoading = uiState.isLoading,
                        onFolderClick = viewModel::onFolderClicked
                    )
                    HomeTab.PLAYLIST -> PlaylistEmptyContent()
                }
            }
        }
    }
}

@Composable
private fun PermissionContent(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.VideoLibrary,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = stringResource(R.string.home_permission_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.home_permission_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(stringResource(R.string.home_permission_button))
        }
    }
}

/**
 * Tab Playlist di Milestone 3 baru menampilkan empty state yang jujur:
 * daftar playlist memang masih kosong; CRUD-nya dibangun pada
 * milestone Playlist.
 */
@Composable
private fun PlaylistEmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.PlaylistPlay,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(R.string.home_empty_playlists),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = stringResource(R.string.home_empty_playlists_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
