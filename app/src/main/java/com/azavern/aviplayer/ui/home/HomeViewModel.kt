package com.azavern.aviplayer.ui.home

import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azavern.aviplayer.data.model.FolderItem
import com.azavern.aviplayer.data.model.PlaylistEntry
import com.azavern.aviplayer.data.model.SortConfig
import com.azavern.aviplayer.data.model.SortField
import com.azavern.aviplayer.data.model.VideoItem
import com.azavern.aviplayer.data.repository.FolderRepository
import com.azavern.aviplayer.data.repository.PlaybackRepository
import com.azavern.aviplayer.data.repository.PlaylistRepository
import com.azavern.aviplayer.data.repository.SettingsRepository
import com.azavern.aviplayer.data.repository.VideoRepository
import com.azavern.aviplayer.data.source.MediaWriteResult
import com.azavern.aviplayer.utils.FileUtils
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
        // Urutan daftar (sorting global) mengikuti SettingsRepository.
        viewModelScope.launch {
            settingsRepository.getSortConfig().collect { config ->
                _uiState.update { it.copy(sortConfig = config) }
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

    fun loadVideos(showLoading: Boolean = true) {
        videoLoadJob?.cancel()
        videoLoadJob = viewModelScope.launch {
            if (showLoading) _uiState.update { it.copy(isLoading = true, error = null) }
            videoRepository.getAllVideos()
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = e.message)
                    }
                }
                .collect { videos ->
                    _uiState.update {
                        it.copy(videos = videos, isLoading = false, isRefreshing = false)
                    }
                }
        }
    }

    fun loadFolders() {
        viewModelScope.launch {
            folderRepository.getVisibleFolders()
                .catch { e ->
                    _uiState.update { it.copy(isRefreshing = false, error = e.message) }
                }
                .collect { folders ->
                    _uiState.update { it.copy(folders = folders, isRefreshing = false) }
                }
        }
    }

    fun onTabSelected(tab: HomeTab) {
        exitSelectionMode()
        _uiState.update { it.copy(activeTab = tab) }
    }

    /** Pencarian menyaring daftar video (tab Video keluar dari mode folder). */
    fun onSearchQueryChanged(query: String) {
        exitSelectionMode()
        _uiState.update { it.copy(searchQuery = query, activeFolder = null) }
        videoLoadJob?.cancel()
        videoLoadJob = viewModelScope.launch {
            videoRepository.searchVideos(query)
                .catch { e ->
                    _uiState.update { it.copy(isRefreshing = false, error = e.message) }
                }
                .collect { videos ->
                    _uiState.update { it.copy(videos = videos, isRefreshing = false) }
                }
        }
    }

    /** Tap folder → tampilkan video di dalam folder itu pada tab Video. */
    fun onFolderClicked(folder: FolderItem) {
        exitSelectionMode()
        _uiState.update { it.copy(activeFolder = folder, activeTab = HomeTab.VIDEO, searchQuery = "") }
        loadFolderVideos(folder)
    }

    private fun loadFolderVideos(folder: FolderItem, showLoading: Boolean = true) {
        videoLoadJob?.cancel()
        videoLoadJob = viewModelScope.launch {
            if (showLoading) _uiState.update { it.copy(isLoading = true) }
            videoRepository.getVideosByFolder(folder.path)
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = e.message)
                    }
                }
                .collect { videos ->
                    _uiState.update {
                        it.copy(videos = videos, isLoading = false, isRefreshing = false)
                    }
                }
        }
    }

    // ---- Urutan & penyegaran (sorting global + refresh) ----

    /**
     * Pilih bidang urutan dari menu: bidang baru memakai arah
     * bawaannya (mis. Nama → A–Z, Waktu → terbaru dulu). Memilih
     * bidang yang sama tidak mengubah apa pun — arah diatur tombol
     * naik/turun di sebelah menu.
     */
    fun onSortFieldSelected(field: SortField) {
        if (field == _uiState.value.sortConfig.field) return
        viewModelScope.launch {
            settingsRepository.updateSortConfig(SortConfig.forField(field))
        }
    }

    /** Tombol naik/turun: balik arah urutan bidang yang aktif. */
    fun onToggleSortDirection() {
        val current = _uiState.value.sortConfig
        viewModelScope.launch {
            settingsRepository.updateSortConfig(
                current.copy(ascending = !current.ascending)
            )
        }
    }

    /**
     * Segarkan daftar yang sedang tampil (tarik ke bawah atau tombol
     * refresh): scan ulang MediaStore untuk konteks saat ini — daftar
     * Beranda, hasil pencarian, isi folder, daftar folder, atau
     * playlist — supaya file yang baru masuk muncul tanpa membuka
     * ulang aplikasi. Indikator berhenti saat emisi baru tiba.
     */
    fun refresh() {
        val state = _uiState.value
        if (!state.hasPermission || state.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true) }
        when (state.activeTab) {
            HomeTab.VIDEO -> when {
                state.activeFolder != null ->
                    loadFolderVideos(state.activeFolder, showLoading = false)
                state.searchQuery.isNotBlank() -> onSearchQueryChanged(state.searchQuery)
                else -> loadVideos(showLoading = false)
            }
            HomeTab.FOLDER -> loadFolders()
            HomeTab.PLAYLIST -> loadPlaylists()
        }
    }

    /** Keluar dari mode folder → kembali ke semua video. */
    fun onCloseFolder() {
        exitSelectionMode()
        _uiState.update { it.copy(activeFolder = null) }
        loadVideos()
    }

    fun onVideoClicked(video: VideoItem) {
        // Di mode seleksi, klik biasa = pilih / batal pilih.
        if (_uiState.value.selectionMode) {
            toggleSelection(video)
            return
        }
        navigateToPlayer(video)
    }

    /** Menu titik-tiga "Putar": selalu memutar, termasuk dari dalam mode seleksi. */
    fun onPlayMenuClicked(video: VideoItem) {
        exitSelectionMode()
        navigateToPlayer(video)
    }

    private fun navigateToPlayer(video: VideoItem) {
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
                .catch { e ->
                    _uiState.update { it.copy(isRefreshing = false, error = e.message) }
                }
                .collect { playlists ->
                    _uiState.update { it.copy(playlists = playlists, isRefreshing = false) }
                }
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
            exitSelectionMode()
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
            exitSelectionMode()
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

    // ---- Aksi file: ganti nama, info, hapus (menu titik-tiga video) ----

    /** Operasi yang menunggu persetujuan sistem; dijalankan ulang bila disetujui. */
    private var pendingConsentRetry: (() -> Unit)? = null

    fun onRenameVideoRequested(video: VideoItem) {
        _uiState.update { it.copy(pendingFileAction = PendingFileAction.Rename(video)) }
    }

    fun onFileInfoRequested(video: VideoItem) {
        _uiState.update { it.copy(pendingFileAction = PendingFileAction.Info(video)) }
    }

    fun onDeleteVideoRequested(video: VideoItem) {
        _uiState.update { it.copy(pendingFileAction = PendingFileAction.Delete(video)) }
    }

    fun onFileActionDismissed() {
        _uiState.update { it.copy(pendingFileAction = null) }
    }

    /**
     * Konfirmasi dialog ganti nama. Ekstensi tidak pernah berubah;
     * karakter yang dilarang nama file dibuang. Nama yang sama atau
     * kosong setelah dibersihkan = tidak ada tindakan.
     */
    fun confirmRenameVideo(newBaseName: String) {
        val action = _uiState.value.pendingFileAction as? PendingFileAction.Rename ?: return
        val video = action.video
        val clean = newBaseName.trim()
            .replace(Regex("[\\\\/:*?\"<>|]"), "")
            .trim()
        _uiState.update { it.copy(pendingFileAction = null) }
        if (clean.isEmpty() || clean == video.name) return
        val extension = video.fileName.substringAfterLast('.', "")
        val newDisplayName = if (extension.isEmpty()) clean else "$clean.$extension"
        val newPath = "${video.folderPath}/$newDisplayName"
        performRename(video, newDisplayName, newPath, newTitle = clean, isRetry = false)
    }

    private fun performRename(
        video: VideoItem,
        newDisplayName: String,
        newPath: String,
        newTitle: String,
        isRetry: Boolean
    ) {
        viewModelScope.launch {
            when (val result = videoRepository.renameVideo(video.path, newDisplayName)) {
                MediaWriteResult.Success -> {
                    // Data lokal mengikuti file: posisi tontonan pindah
                    // ke id/path baru, entri playlist menunjuk path baru.
                    val position = playbackRepository.getLastPosition(video.id)
                    if (position > 0L) {
                        playbackRepository.savePosition(
                            FileUtils.generateVideoId(newPath), newPath, position
                        )
                        playbackRepository.deletePosition(video.id)
                    }
                    playlistRepository.updateEntriesForRenamedVideo(
                        video.path, newPath, newTitle
                    )
                    exitSelectionMode()
                    refreshPlaybackInfo()
                    refresh()
                }
                is MediaWriteResult.NeedsConsent -> if (!isRetry) {
                    pendingConsentRetry = {
                        performRename(video, newDisplayName, newPath, newTitle, isRetry = true)
                    }
                    _uiState.update { it.copy(consentIntentSender = result.intentSender) }
                }
                MediaWriteResult.Failed -> Unit
            }
        }
    }

    /** Konfirmasi dialog hapus: file dihapus permanen dari perangkat. */
    fun confirmDeleteVideo() {
        val action = _uiState.value.pendingFileAction as? PendingFileAction.Delete ?: return
        val video = action.video
        _uiState.update { it.copy(pendingFileAction = null) }
        performDelete(video, isRetry = false)
    }

    private fun performDelete(video: VideoItem, isRetry: Boolean) {
        viewModelScope.launch {
            when (val result = videoRepository.deleteVideo(video.path)) {
                MediaWriteResult.Success -> {
                    // Bersihkan jejak lokal: riwayat posisi & entri playlist.
                    playbackRepository.deletePosition(video.id)
                    playlistRepository.removeAllEntriesForVideo(video.path)
                    exitSelectionMode()
                    refreshPlaybackInfo()
                    refresh()
                }
                is MediaWriteResult.NeedsConsent -> if (!isRetry) {
                    pendingConsentRetry = { performDelete(video, isRetry = true) }
                    _uiState.update { it.copy(consentIntentSender = result.intentSender) }
                }
                MediaWriteResult.Failed -> Unit
            }
        }
    }

    /** HomeScreen sudah meluncurkan dialog persetujuan sistem. */
    fun onConsentLaunched() {
        _uiState.update { it.copy(consentIntentSender = null) }
    }

    /** Hasil dialog persetujuan sistem: ulangi operasi yang menunggu bila disetujui. */
    fun onConsentResult(granted: Boolean) {
        val retry = pendingConsentRetry
        pendingConsentRetry = null
        if (granted) retry?.invoke()
    }

    // ---- Mode seleksi banyak video (permintaan Veo 2026-11-04) ----
    //
    // Masuk lewat tekan-tahan pada kotak video (di mana pun kecuali
    // tombol titik-tiga) atau item "Pilih" di menu titik-tiga. Selama
    // mode ini aktif, klik biasa pada video = pilih / batal pilih, dan
    // urutan memilih menentukan nomor pada lingkaran tiap video serta
    // urutan antrean/playlist hasil tombol Putar.

    /** Video terpilih, terurut sesuai urutan memilih. */
    private fun orderedSelectedVideos(): List<VideoItem> {
        val state = _uiState.value
        val byId = state.videos.associateBy { it.id }
        return state.selectedVideoIds.mapNotNull { byId[it] }
    }

    fun onVideoLongPressed(video: VideoItem) = enterSelection(video)

    /** Item "Pilih" di menu titik-tiga: pintu masuk mode seleksi selain tekan-tahan. */
    fun onSelectVideoRequested(video: VideoItem) = enterSelection(video)

    private fun enterSelection(video: VideoItem) {
        if (_uiState.value.selectionMode) {
            toggleSelection(video)
        } else {
            _uiState.update {
                it.copy(selectionMode = true, selectedVideoIds = listOf(video.id))
            }
        }
    }

    private fun toggleSelection(video: VideoItem) {
        val current = _uiState.value.selectedVideoIds
        val updated = if (video.id in current) current - video.id else current + video.id
        if (updated.isEmpty()) {
            exitSelectionMode()
        } else {
            _uiState.update { it.copy(selectedVideoIds = updated) }
        }
    }

    /** Keluar mode seleksi dan lupakan semua pilihan + dialog turunannya. */
    fun exitSelectionMode() {
        _uiState.update {
            it.copy(
                selectionMode = false,
                selectedVideoIds = emptyList(),
                playlistPickerForSelection = false,
                showSelectionInfo = false,
                showDeleteSelectionConfirm = false
            )
        }
    }

    /** Shortcut pojok kanan atas: pilih semua yang tampil; ketuk lagi saat semua terpilih = kosongkan. */
    fun onSelectAllClicked() {
        val state = _uiState.value
        val allIds = state.videos.map { it.id }
        if (allIds.isEmpty()) return
        if (state.selectedVideoIds.size == allIds.size &&
            state.selectedVideoIds.containsAll(allIds)
        ) {
            exitSelectionMode()
        } else {
            _uiState.update {
                it.copy(selectionMode = true, selectedVideoIds = allIds)
            }
        }
    }

    /**
     * Putar dari bar seleksi: otomatis membuat playlist baru bernama
     * "Playlist NN" (nomor bebas terkecil), mengisinya sesuai urutan
     * memilih, lalu memutar dari video pertama dengan antrean =
     * urutan itu (semantik Veo).
     */
    fun playSelection() {
        val selected = orderedSelectedVideos()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(nextAutoPlaylistName())
            selected.forEach { playlistRepository.addVideoToPlaylist(playlistId, it.path) }
            exitSelectionMode()
            _uiState.update {
                it.copy(
                    navigation = HomeNavigation.ToPlayer(
                        path = selected.first().path,
                        id = selected.first().id,
                        title = selected.first().name,
                        queuePaths = selected.map { video -> video.path },
                        queueTitles = selected.map { video -> video.name }
                    )
                )
            }
        }
    }

    /** Nama otomatis "Playlist 01", "Playlist 02", … — nomor terkecil yang belum dipakai. */
    private fun nextAutoPlaylistName(): String {
        val used = _uiState.value.playlists.mapNotNull { playlist ->
            Regex("""Playlist (\d+)""").matchEntire(playlist.name)
                ?.groupValues?.get(1)?.toIntOrNull()
        }.toSet()
        var n = 1
        while (n in used) n++
        return "Playlist " + n.toString().padStart(2, '0')
    }

    fun onAddSelectionToPlaylistRequested() {
        if (_uiState.value.selectedVideoIds.isEmpty()) return
        _uiState.update { it.copy(playlistPickerForSelection = true) }
    }

    fun onSelectionPlaylistPickerDismissed() {
        _uiState.update { it.copy(playlistPickerForSelection = false) }
    }

    /** Dialog pemilih: masukkan seluruh pilihan ke playlist tujuan, sesuai urutan memilih. */
    fun addSelectionToPlaylist(playlistId: Long) {
        val selected = orderedSelectedVideos()
        _uiState.update { it.copy(playlistPickerForSelection = false) }
        if (selected.isEmpty()) return
        viewModelScope.launch {
            selected.forEach { playlistRepository.addVideoToPlaylist(playlistId, it.path) }
            exitSelectionMode()
        }
    }

    /** Dialog pemilih: buat playlist baru lalu isi dengan seluruh pilihan. */
    fun createPlaylistAndAddSelection(name: String) {
        val trimmed = name.trim()
        val selected = orderedSelectedVideos()
        if (trimmed.isEmpty() || selected.isEmpty()) return
        _uiState.update { it.copy(playlistPickerForSelection = false) }
        viewModelScope.launch {
            val newId = playlistRepository.createPlaylist(trimmed)
            selected.forEach { playlistRepository.addVideoToPlaylist(newId, it.path) }
            exitSelectionMode()
        }
    }

    /** Ganti nama dari bar seleksi — hanya jalan bila tepat satu video terpilih. */
    fun onRenameSelectionRequested() {
        val selected = orderedSelectedVideos()
        if (selected.size != 1) return
        exitSelectionMode()
        onRenameVideoRequested(selected.first())
    }

    /** Info dari bar seleksi: satu video → info file; banyak → ringkasan pilihan. */
    fun onSelectionInfoRequested() {
        val selected = orderedSelectedVideos()
        when {
            selected.size == 1 -> {
                exitSelectionMode()
                onFileInfoRequested(selected.first())
            }
            selected.size > 1 ->
                _uiState.update { it.copy(showSelectionInfo = true) }
        }
    }

    fun onSelectionInfoDismissed() {
        _uiState.update { it.copy(showSelectionInfo = false) }
    }

    fun onDeleteSelectionRequested() {
        if (_uiState.value.selectedVideoIds.isNotEmpty()) {
            _uiState.update { it.copy(showDeleteSelectionConfirm = true) }
        }
    }

    fun onDeleteSelectionDismissed() {
        _uiState.update { it.copy(showDeleteSelectionConfirm = false) }
    }

    /** Konfirmasi dialog hapus batch: semua file terpilih dihapus permanen. */
    fun confirmDeleteSelection() {
        val selected = orderedSelectedVideos()
        _uiState.update { it.copy(showDeleteSelectionConfirm = false) }
        if (selected.isEmpty()) return
        performBatchDelete(selected, isRetry = false)
    }

    private fun performBatchDelete(videos: List<VideoItem>, isRetry: Boolean) {
        viewModelScope.launch {
            val needsConsentPaths = mutableListOf<String>()
            var firstConsentSender: IntentSender? = null
            for (video in videos) {
                when (val result = videoRepository.deleteVideo(video.path)) {
                    MediaWriteResult.Success -> {
                        playbackRepository.deletePosition(video.id)
                        playlistRepository.removeAllEntriesForVideo(video.path)
                    }
                    is MediaWriteResult.NeedsConsent -> {
                        needsConsentPaths += video.path
                        if (firstConsentSender == null) {
                            firstConsentSender = result.intentSender
                        }
                    }
                    MediaWriteResult.Failed -> Unit
                }
            }
            if (needsConsentPaths.isNotEmpty() && !isRetry) {
                // Satu dialog persetujuan sistem untuk semua file yang
                // tertahan sekaligus (Android 11+); Android 10 memakai
                // dialog milik file pertama yang tertahan.
                val sender = videoRepository.createWriteConsentSender(needsConsentPaths)
                    ?: firstConsentSender
                if (sender != null) {
                    pendingConsentRetry = { performBatchDelete(videos, isRetry = true) }
                    _uiState.update { it.copy(consentIntentSender = sender) }
                    return@launch
                }
            }
            exitSelectionMode()
            refreshPlaybackInfo()
            refresh()
        }
    }
}
