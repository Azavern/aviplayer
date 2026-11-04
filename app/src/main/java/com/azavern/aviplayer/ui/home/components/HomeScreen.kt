package com.azavern.aviplayer.ui.home.components

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azavern.aviplayer.R
import com.azavern.aviplayer.ui.home.HomeNavigation
import com.azavern.aviplayer.ui.home.HomeTab
import com.azavern.aviplayer.ui.home.HomeViewModel
import com.azavern.aviplayer.ui.home.PendingFileAction
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

    // Dialog persetujuan sistem untuk ganti nama / hapus file milik
    // aplikasi lain (MediaStore): diluncurkan saat ViewModel meminta,
    // hasilnya menentukan operasi yang menunggu dijalankan ulang.
    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onConsentResult(result.resultCode == Activity.RESULT_OK)
    }
    LaunchedEffect(uiState.consentIntentSender) {
        uiState.consentIntentSender?.let { sender ->
            consentLauncher.launch(IntentSenderRequest.Builder(sender).build())
            viewModel.onConsentLaunched()
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

    // Tombol kembali sistem keluar dari mode seleksi lebih dulu.
    BackHandler(enabled = uiState.selectionMode) {
        viewModel.exitSelectionMode()
    }

    Scaffold(
        topBar = {
            if (uiState.selectionMode) {
                // Bar mode seleksi (referensi Veo): X | "Terpilih n/total" | pilih semua.
                TopAppBar(
                    title = {
                        Text(
                            stringResource(
                                R.string.home_selection_title,
                                uiState.selectedVideoIds.size,
                                uiState.videos.size
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = viewModel::exitSelectionMode) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(
                                    R.string.home_selection_close
                                )
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::onSelectAllClicked) {
                            Icon(
                                imageVector = Icons.Filled.SelectAll,
                                contentDescription = stringResource(
                                    R.string.home_selection_select_all
                                )
                            )
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        if (uiState.hasPermission) {
                            IconButton(onClick = viewModel::refresh) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = stringResource(R.string.home_refresh)
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToCustomization) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = stringResource(R.string.customization_title)
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (uiState.selectionMode) {
                SelectionActionBar(
                    selectedCount = uiState.selectedVideoIds.size,
                    onPlay = viewModel::playSelection,
                    onAddToPlaylist = viewModel::onAddSelectionToPlaylistRequested,
                    onRename = viewModel::onRenameSelectionRequested,
                    onInfo = viewModel::onSelectionInfoRequested,
                    onDelete = viewModel::onDeleteSelectionRequested
                )
            } else {
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
                        SortBar(
                            sortConfig = uiState.sortConfig,
                            onFieldSelected = viewModel::onSortFieldSelected,
                            onToggleDirection = viewModel::onToggleSortDirection
                        )
                        PullToRefreshBox(
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = viewModel::refresh,
                            modifier = Modifier.weight(1f)
                        ) {
                            VideoListScreen(
                                videos = uiState.videos,
                                isLoading = uiState.isLoading,
                                activeFolder = uiState.activeFolder,
                                playbackPositions = uiState.playbackPositions,
                                lastPlayedVideoId = uiState.lastPlayedVideoId,
                                onVideoClick = viewModel::onVideoClicked,
                                onPlayClick = viewModel::onPlayMenuClicked,
                                onLongPress = viewModel::onVideoLongPressed,
                                onCloseFolder = viewModel::onCloseFolder,
                                onAddToPlaylistClick = viewModel::onAddToPlaylistRequested,
                                onSelectClick = viewModel::onSelectVideoRequested,
                                onRenameClick = viewModel::onRenameVideoRequested,
                                onInfoClick = viewModel::onFileInfoRequested,
                                onDeleteClick = viewModel::onDeleteVideoRequested,
                                selectionMode = uiState.selectionMode,
                                selectedVideoIds = uiState.selectedVideoIds,
                                listState = videoListState
                            )
                        }
                    }
                    HomeTab.FOLDER -> PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        FolderGridScreen(
                            folders = uiState.folders,
                            isLoading = uiState.isLoading,
                            onFolderClick = viewModel::onFolderClicked
                        )
                    }
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
                !uiState.selectionMode && showScrollTopButton
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

        // Dialog pemilih playlist untuk seluruh pilihan (mode seleksi).
        if (uiState.playlistPickerForSelection) {
            PlaylistPickerDialog(
                videoTitle = stringResource(
                    R.string.playlist_video_count, uiState.selectedVideoIds.size
                ),
                playlists = uiState.playlists,
                onPick = viewModel::addSelectionToPlaylist,
                onCreateAndPick = viewModel::createPlaylistAndAddSelection,
                onDismiss = viewModel::onSelectionPlaylistPickerDismissed
            )
        }

        // Ringkasan info untuk pilihan banyak file.
        if (uiState.showSelectionInfo) {
            SelectionInfoDialog(
                videos = uiState.videos.filter { it.id in uiState.selectedVideoIds },
                onDismiss = viewModel::onSelectionInfoDismissed
            )
        }

        // Konfirmasi hapus untuk seluruh pilihan.
        if (uiState.showDeleteSelectionConfirm) {
            DeleteSelectionDialog(
                count = uiState.selectedVideoIds.size,
                onConfirm = viewModel::confirmDeleteSelection,
                onDismiss = viewModel::onDeleteSelectionDismissed
            )
        }

        // Dialog aksi file dari menu titik-tiga video.
        when (val action = uiState.pendingFileAction) {
            is PendingFileAction.Rename -> RenameVideoDialog(
                video = action.video,
                onConfirm = viewModel::confirmRenameVideo,
                onDismiss = viewModel::onFileActionDismissed
            )
            is PendingFileAction.Info -> FileInfoDialog(
                video = action.video,
                onDismiss = viewModel::onFileActionDismissed
            )
            is PendingFileAction.Delete -> DeleteVideoDialog(
                video = action.video,
                onConfirm = viewModel::confirmDeleteVideo,
                onDismiss = viewModel::onFileActionDismissed
            )
            null -> Unit
        }
    }
}

/**
 * Bar aksi bawah mode seleksi (referensi Veo): lima aksi yang sama
 * dengan menu titik-tiga — Putar, Playlist, Ganti Nama, Info, Hapus.
 * Ganti Nama hanya aktif bila tepat satu video terpilih; label Hapus
 * membawa jumlah pilihan.
 */
@Composable
private fun SelectionActionBar(
    selectedCount: Int,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRename: () -> Unit,
    onInfo: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SelectionAction(
                icon = Icons.Filled.PlayArrow,
                label = stringResource(R.string.home_selection_play),
                enabled = selectedCount > 0,
                onClick = onPlay
            )
            SelectionAction(
                icon = Icons.Filled.PlaylistAdd,
                label = stringResource(R.string.playlist_add_to),
                enabled = selectedCount > 0,
                onClick = onAddToPlaylist
            )
            SelectionAction(
                icon = Icons.Filled.Edit,
                label = stringResource(R.string.home_action_rename),
                enabled = selectedCount == 1,
                onClick = onRename
            )
            SelectionAction(
                icon = Icons.Filled.Info,
                label = stringResource(R.string.home_action_info),
                enabled = selectedCount > 0,
                onClick = onInfo
            )
            SelectionAction(
                icon = Icons.Filled.Delete,
                label = stringResource(R.string.home_selection_delete, selectedCount),
                enabled = selectedCount > 0,
                onClick = onDelete,
                danger = true
            )
        }
    }
}

@Composable
private fun SelectionAction(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    val activeColor = if (danger) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val color = if (enabled) {
        activeColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1
        )
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
