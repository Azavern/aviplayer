package com.azavern.aviplayer.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

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
    onNavigateToPlayer: (HomeNavigation.ToPlayer) -> Unit,
    onRequestPermission: () -> Unit,
    onNavigateToCustomization: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigation) {
        val navigation = uiState.navigation
        if (navigation is HomeNavigation.ToPlayer) {
            onNavigateToPlayer(navigation)
            viewModel.onNavigationHandled()
        }
    }

    // State daftar video di-hoist ke sini supaya FAB "kembali ke atas"
    // bisa mengendalikan daftar yang sama (permintaan Veo 2026-11-04:
    // posisi daftar suka nyangkut di bawah saat pencarian).
    val videoListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollTopButton by remember {
        derivedStateOf { videoListState.firstVisibleItemIndex > 2 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToCustomization) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = stringResource(R.string.customization_title)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (uiState.hasPermission && !uiState.miniBarDismissed &&
                    uiState.lastPlayedPath != null
                ) {
                    MiniPlayerBar(
                        title = uiState.lastPlayedTitle.orEmpty(),
                        thumbnailPath = uiState.lastPlayedPath,
                        onPlayClick = viewModel::onMiniPlayerClicked,
                        onDismiss = viewModel::onMiniBarDismissed
                    )
                }
                BottomNavBar(
                    activeTab = uiState.activeTab,
                    onTabSelected = viewModel::onTabSelected
                )
            }
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
                            playbackPositions = uiState.playbackPositions,
                            lastPlayedVideoId = uiState.lastPlayedVideoId,
                            onVideoClick = viewModel::onVideoClicked,
                            onCloseFolder = viewModel::onCloseFolder,
                            onAddToPlaylistClick = viewModel::onAddToPlaylistRequested,
                            listState = videoListState
                        )
                    }
                    HomeTab.FOLDER -> FolderGridScreen(
                        folders = uiState.folders,
                        isLoading = uiState.isLoading,
                        onFolderClick = viewModel::onFolderClicked
                    )
                    HomeTab.PLAYLIST -> {
                        val openPlaylist = uiState.playlists
                            .firstOrNull { it.id == uiState.openPlaylistId }
                        if (uiState.openPlaylistId != null && openPlaylist != null) {
                            PlaylistDetailScreen(
                                playlist = openPlaylist,
                                entries = uiState.playlistEntries,
                                onBack = viewModel::onClosePlaylist,
                                onPlayAll = {
                                    uiState.playlistEntries.firstOrNull()?.let {
                                        viewModel.onPlayPlaylist(it)
                                    }
                                },
                                onEntryClick = viewModel::onPlayPlaylist,
                                onRemoveEntry = viewModel::removePlaylistEntry,
                                onMoveEntry = viewModel::movePlaylistEntry,
                                onReorderFinished = viewModel::persistPlaylistEntryOrder
                            )
                        } else {
                            PlaylistScreen(
                                playlists = uiState.playlists,
                                onPlaylistClick = viewModel::onPlaylistClicked,
                                onCreatePlaylist = viewModel::createPlaylist,
                                onRenamePlaylist = viewModel::renamePlaylist,
                                onDeletePlaylist = viewModel::deletePlaylist
                            )
                        }
                    }
                }
            }

            // FAB kembali ke atas: hanya di tab Video saat daftar sudah
            // bergeser jauh dari atas. Box konten sudah dipangkas
            // innerPadding, jadi FAB otomatis bertengger tepat di atas
            // bar mini / navigasi bawah.
            if (uiState.hasPermission && uiState.activeTab == HomeTab.VIDEO &&
                showScrollTopButton
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch { videoListState.animateScrollToItem(0) }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.home_scroll_top)
                    )
                }
            }
        }

        // Dialog pemilih playlist untuk video yang menunggu ditambahkan.
        uiState.pendingPlaylistVideo?.let { pendingVideo ->
            PlaylistPickerDialog(
                videoTitle = pendingVideo.name,
                playlists = uiState.playlists,
                onPick = viewModel::addPendingVideoToPlaylist,
                onCreateAndPick = viewModel::createPlaylistAndAddPending,
                onDismiss = viewModel::onAddToPlaylistDismissed
            )
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
