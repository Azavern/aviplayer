package com.azavern.aviplayer.ui.customization

import com.azavern.aviplayer.data.model.ControlLayoutConfig
import com.azavern.aviplayer.data.model.FolderItem
import com.azavern.aviplayer.data.model.FolderVisibilityConfig
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.data.model.TitleConfig

/** Tab yang tersedia di layar Kustomisasi (Milestone 7 + 8 + 9). */
enum class CustomizationTab { TITLE, CONTROLS, THEME, WHITELIST }

/** State layar Kustomisasi (Blueprint Bagian 7). */
data class CustomizationUiState(
    val titleConfig: TitleConfig = TitleConfig.Default,
    val controlLayoutConfig: ControlLayoutConfig = ControlLayoutConfig.Default,
    val themeConfig: ThemeConfig = ThemeConfig.Default,
    /** Konfigurasi whitelist folder (Milestone 9). */
    val folderVisibilityConfig: FolderVisibilityConfig = FolderVisibilityConfig.Default,
    /** Semua folder hasil scan, tanpa filter — untuk editor whitelist. */
    val allFolders: List<FolderItem> = emptyList(),
    val activeTab: CustomizationTab = CustomizationTab.TITLE,
    /** Slot pertama yang dipilih untuk ditukar; null = belum memilih. */
    val selectedSlotA: CustomizationViewModel.SlotPosition? = null,
    val isLoading: Boolean = true
)
