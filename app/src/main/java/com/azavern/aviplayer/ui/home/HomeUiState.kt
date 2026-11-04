package com.azavern.aviplayer.ui.home

import com.azavern.aviplayer.data.model.FolderItem
import com.azavern.aviplayer.data.model.PlaylistEntry
import com.azavern.aviplayer.data.model.PlaylistInfo
import com.azavern.aviplayer.data.model.SortConfig
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.data.model.VideoItem

/**
 * State Beranda (Blueprint Bagian 7). Satu state untuk tiga tab;
 * [navigation] adalah event sekali-jalan menuju PlayerActivity
 * (dibersihkan HomeScreen lewat HomeViewModel.onNavigationHandled).
 */
data class HomeUiState(
    val videos: List<VideoItem> = emptyList(),
    val folders: List<FolderItem> = emptyList(),
    val playlists: List<PlaylistInfo> = emptyList(),
    val activeTab: HomeTab = HomeTab.VIDEO,
    val activeFolder: FolderItem? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val hasPermission: Boolean = false,
    val error: String? = null,
    val themeConfig: ThemeConfig = ThemeConfig.Default,
    val navigation: HomeNavigation? = null,
    /** Peta videoId → posisi terakhir, untuk bar progres di thumbnail. */
    val playbackPositions: Map<String, Long> = emptyMap(),
    /** Video yang paling terakhir diputar: judulnya diwarnai di daftar. */
    val lastPlayedVideoId: String? = null,
    val lastPlayedPath: String? = null,
    val lastPlayedTitle: String? = null,
    /** Bar mini video terakhir (di atas navigasi bawah) ditutup pengguna. */
    val miniBarDismissed: Boolean = false,
    /** Playlist yang sedang dibuka detailnya di tab Playlist (Milestone 10). */
    val openPlaylistId: Long? = null,
    /** Entri playlist yang sedang dibuka, sesuai urutan tampil. */
    val playlistEntries: List<PlaylistEntry> = emptyList(),
    /** Video yang menunggu dipilihkan playlist (dialog pemilih). */
    val pendingPlaylistVideo: VideoItem? = null,
    /** Urutan daftar video yang sedang berlaku (sorting global). */
    val sortConfig: SortConfig = SortConfig.Default,
    /** Sedang memuat ulang daftar (tarik-untuk-segarkan / tombol refresh). */
    val isRefreshing: Boolean = false
)

enum class HomeTab { VIDEO, FOLDER, PLAYLIST }

sealed class HomeNavigation {
    /**
     * Menuju player. [queuePaths]/[queueTitles] adalah antrean konteks
     * putar (Milestone 10, semantik Veo): daftar yang sedang tampil
     * saat video dibuka — isi folder, daftar Beranda, atau entri
     * playlist — supaya prev/next player mengikuti kelompoknya.
     */
    data class ToPlayer(
        val path: String,
        val id: String,
        val title: String,
        val queuePaths: List<String> = emptyList(),
        val queueTitles: List<String> = emptyList()
    ) : HomeNavigation()
}
