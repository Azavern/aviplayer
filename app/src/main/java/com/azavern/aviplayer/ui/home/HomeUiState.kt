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
    val navigation: HomeNavigation? = null
)

enum class HomeTab { VIDEO, FOLDER, PLAYLIST }

sealed class HomeNavigation {
    data class ToPlayer(
        val path: String,
        val id: String,
        val title: String
    ) : HomeNavigation()
}
