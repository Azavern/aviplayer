package com.azavern.aviplayer.ui.home

import com.azavern.aviplayer.data.model.FolderItem
import com.azavern.aviplayer.data.model.PlaylistInfo
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
    val miniBarDismissed: Boolean = false
)

enum class HomeTab { VIDEO, FOLDER, PLAYLIST }

sealed class HomeNavigation {
    data class ToPlayer(
        val path: String,
        val id: String,
        val title: String
    ) : HomeNavigation()
}
