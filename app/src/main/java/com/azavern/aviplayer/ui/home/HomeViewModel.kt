package com.azavern.aviplayer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azavern.aviplayer.data.model.FolderItem
import com.azavern.aviplayer.data.model.PlaylistEntry
import com.azavern.aviplayer.data.model.VideoItem
import com.azavern.aviplayer.data.repository.FolderRepository
import com.azavern.aviplayer.data.repository.PlaybackRepository
import com.azavern.aviplayer.data.repository.PlaylistRepository
import com.azavern.aviplayer.data.repository.SettingsRepository
import com.azavern.aviplayer.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel Beranda (Blueprint Bagian 7 & Milestone 3).
 *
 * Data hanya dimuat setelah izin baca media diberikan — HomeActivity
 * melaporkan hasil izin lewat [onPermissionResult] (termasuk izin yang
 * sudah diberikan sebelumnya lewat Pengaturan HP).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val folderRepository: FolderRepository,
    private val settingsRepository: SettingsRepository,
    private val playbackRepository: PlaybackRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var videoLoadJob: Job? = null
    private var playlistEntriesJob: Job? = null

    init {
        // Tema mengikuti SettingsRepository (Milestone 4): HomeActivity
        // meneruskan themeConfig dari state ini ke AppTheme.
        viewModelScope.launch {
            settingsRepository.getThemeConfig().collect { config ->
                _uiState.update { it.copy(themeConfig = config) }
            }
        }
    }

    /** Hasil izin dari HomeActivity; saat diberikan, muat semua data. */
    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted, isLoading = granted) }
        if (granted) {
            loadVideos()
            loadFolders()
            loadPlaylists()
            refreshPlaybackInfo()
        }
    }

    /**
     * Dipanggil HomeActivity.onStart: segarkan info tontonan (bar
     * progres, penanda video terakhir, bar mini) setelah pengguna
     * kembali dari PlayerActivity.
     */
    fun onResume() {
        if (_uiState.value.hasPermission) refreshPlaybackInfo()
    }

    private fun refreshPlaybackInfo() {
        viewModelScope.launch {
            val positions = playbackRepository.getAllPositions()
            val lastPlayed = playbackRepository.getLastPlayed()
            _uiState.update { state ->
                state.copy(
                    playbackPositions = positions,
                    lastPlayedVideoId = lastPlayed?.videoId,
                    lastPlayedPath = lastPlayed?.videoPath,
                    lastPlayedTitle = lastPlayed?.videoPath
                        ?.substringAfterLast('/')
                        ?.substringBeforeLast('.'),
                    // Video terakhir berganti → bar mini boleh tampil lagi.
                    miniBarDismissed = if (lastPlayed?.videoId != state.lastPlayedVideoId) {
                        false
                    } else {
                        state.miniBarDismissed
                    }
                )
            }
        }
    }

    /** Tap bar mini → putar video terakhir (resume bekerja lewat Milestone 2). */
    fun onMiniPlayerClicked() {
        val state = _uiState.value
        val path = state.lastPlayedPath ?: return
        val id = state.lastPlayedVideoId ?: return
        _uiState.update {
            it.copy(
                navigation = HomeNavigation.ToPlayer(
                    path = path,
                    id = id,
                    title = state.lastPlayedTitle ?: ""
                )
            )
        }
    }

    fun onMiniBarDismissed() {
        _uiState.update { it.copy(miniBarDismissed = true) }
    }

    fun loadVideos() {
        videoLoadJob?.cancel()
        videoLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            videoRepository.getAllVideos()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { videos ->
                    _uiState.update { it.copy(videos = videos, isLoading = false) }
                }
        }
    }

    fun loadFolders() {
        viewModelScope.launch {
            folderRepository.getVisibleFolders()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { folders -> _uiState.update { it.copy(folders = folders) } }
        }
    }

    fun onTabSelected(tab: HomeTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    /** Pencarian menyaring daftar video (tab Video keluar dari mode folder). */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, activeFolder = null) }
        videoLoadJob?.cancel()
        videoLoadJob = viewModelScope.launch {
            videoRepository.searchVideos(query)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { videos -> _uiState.update { it.copy(videos = videos) } }
        }
    }

    /** Tap folder → tampilkan video di dalam folder itu pada tab Video. */
    fun onFolderClicked(folder: FolderItem) {
        _uiState.update { it.copy(activeFolder = folder, activeTab = HomeTab.VIDEO, searchQuery = "") }
        videoLoadJob?.cancel()
        videoLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            videoRepository.getVideosByFolder(folder.path)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { videos ->
                    _uiState.update { it.copy(videos = videos, isLoading = false) }
                }
        }
    }

    /** Keluar dari mode folder → kembali ke semua video. */
    fun onCloseFolder() {
        _uiState.update { it.copy(activeFolder = null) }
        loadVideos()
    }

    fun onVideoClicked(video: VideoItem) {
        // Antrean = daftar yang sedang tampil (state.videos): dari
        // Beranda berarti lintas folder; dari mode folder berarti isi
        // folder itu; dari pencarian berarti hasil pencarian saat itu.
        val displayed = _uiState.value.videos
        _uiState.update {
            it.copy(
                navigation = HomeNavigation.ToPlayer(
                    path = video.path,
                    id = video.id,
                    title = video.name,
                    queuePaths = displayed.map { item -> item.path },
                    queueTitles = displayed.map { item -> item.name }
                )
            )
        }
    }

    // ---- Playlist (Milestone 10) ----

    fun loadPlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { playlists -> _uiState.update { it.copy(playlists = playlists) } }
        }
    }

    fun createPlaylist(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { playlistRepository.createPlaylist(trimmed) }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { playlistRepository.renamePlaylist(playlistId, trimmed) }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch { playlistRepository.deletePlaylist(playlistId) }
        if (_uiState.value.openPlaylistId == playlistId) onClosePlaylist()
    }

    /** Buka detail playlist: muat entrinya dan ikuti perubahannya. */
    fun onPlaylistClicked(playlistId: Long) {
        _uiState.update { it.copy(openPlaylistId = playlistId, playlistEntries = emptyList()) }
        playlistEntriesJob?.cancel()
        playlistEntriesJob = viewModelScope.launch {
            playlistRepository.getPlaylistEntries(playlistId)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { entries -> _uiState.update { it.copy(playlistEntries = entries) } }
        }
    }

    fun onClosePlaylist() {
        playlistEntriesJob?.cancel()
        playlistEntriesJob = null
        _uiState.update { it.copy(openPlaylistId = null, playlistEntries = emptyList()) }
    }

    /** Menu video "Tambah ke Playlist" → tampilkan dialog pemilih. */
    fun onAddToPlaylistRequested(video: VideoItem) {
        _uiState.update { it.copy(pendingPlaylistVideo = video) }
    }

    fun onAddToPlaylistDismissed() {
        _uiState.update { it.copy(pendingPlaylistVideo = null) }
    }

    /** Dialog pemilih: tambahkan video yang menunggu ke playlist tujuan. */
    fun addPendingVideoToPlaylist(playlistId: Long) {
        val video = _uiState.value.pendingPlaylistVideo ?: return
        _uiState.update { it.copy(pendingPlaylistVideo = null) }
        viewModelScope.launch {
            playlistRepository.addVideoToPlaylist(playlistId, video.path)
        }
    }

    /** Dialog pemilih: buat playlist baru lalu langsung isi dengan video yang menunggu. */
    fun createPlaylistAndAddPending(name: String) {
        val trimmed = name.trim()
        val video = _uiState.value.pendingPlaylistVideo ?: return
        if (trimmed.isEmpty()) return
        _uiState.update { it.copy(pendingPlaylistVideo = null) }
        viewModelScope.launch {
            val newId = playlistRepository.createPlaylist(trimmed)
            playlistRepository.addVideoToPlaylist(newId, video.path)
        }
    }

    fun removePlaylistEntry(entry: PlaylistEntry) {
        viewModelScope.launch { playlistRepository.removeVideoFromPlaylist(entry.id) }
    }

    /** Geser entri saat drag reorder (state berubah seketika; simpan saat drag selesai). */
    fun movePlaylistEntry(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value.playlistEntries
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        val reordered = current.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        _uiState.update { it.copy(playlistEntries = reordered) }
    }

    /** Drag selesai → tulis urutan baru ke database. */
    fun persistPlaylistEntryOrder() {
        val entries = _uiState.value.playlistEntries
        viewModelScope.launch { playlistRepository.reorderEntries(entries) }
    }

    /** Putar playlist dari entri tertentu (Play All = dari indeks 0). */
    fun onPlayPlaylist(startEntry: PlaylistEntry) {
        val entries = _uiState.value.playlistEntries
        if (entries.isEmpty()) return
        _uiState.update {
            it.copy(
                navigation = HomeNavigation.ToPlayer(
                    path = startEntry.videoPath,
                    id = com.azavern.aviplayer.utils.FileUtils.generateVideoId(startEntry.videoPath),
                    title = startEntry.videoTitle,
                    queuePaths = entries.map { entry -> entry.videoPath },
                    queueTitles = entries.map { entry -> entry.videoTitle }
                )
            )
        }
    }

    /** Dipanggil HomeScreen setelah event navigasi diteruskan ke Activity. */
    fun onNavigationHandled() {
        _uiState.update { it.copy(navigation = null) }
    }
}
