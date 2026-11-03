package com.azavern.aviplayer.ui.customization

import com.azavern.aviplayer.data.model.ControlLayoutConfig
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.data.model.TitleConfig

/** Tab yang tersedia di layar Kustomisasi (Milestone 7 + 8). */
enum class CustomizationTab { TITLE, CONTROLS, THEME }

/**
 * State layar Kustomisasi (Blueprint Bagian 7, dipersempit:
 * whitelist folder menyusul di milestone folder — state-nya
 * ditambah saat itu).
 */
data class CustomizationUiState(
    val titleConfig: TitleConfig = TitleConfig.Default,
    val controlLayoutConfig: ControlLayoutConfig = ControlLayoutConfig.Default,
    val themeConfig: ThemeConfig = ThemeConfig.Default,
    val activeTab: CustomizationTab = CustomizationTab.TITLE,
    /** Slot pertama yang dipilih untuk ditukar; null = belum memilih. */
    val selectedSlotA: CustomizationViewModel.SlotPosition? = null,
    val isLoading: Boolean = true
)
