package com.azavern.aviplayer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azavern.aviplayer.data.model.FolderItem
import com.azavern.aviplayer.data.model.VideoItem
import com.azavern.aviplayer.data.repository.FolderRepository
import com.azavern.aviplayer.data.repository.PlaybackRepository
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
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var videoLoadJob: Job? = null

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
        _uiState.update {
            it.copy(
                navigation = HomeNavigation.ToPlayer(
                    path = video.path,
                    id = video.id,
                    title = video.name
                )
            )
        }
    }

    /** Dipanggil HomeScreen setelah event navigasi diteruskan ke Activity. */
    fun onNavigationHandled() {
        _uiState.update { it.copy(navigation = null) }
    }
}
