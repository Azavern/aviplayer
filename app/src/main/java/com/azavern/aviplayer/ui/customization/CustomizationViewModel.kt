package com.azavern.aviplayer.ui.customization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azavern.aviplayer.data.model.ControlLayoutConfig
import com.azavern.aviplayer.data.model.ControlSlot
import com.azavern.aviplayer.data.model.FolderVisibilityConfig
import com.azavern.aviplayer.data.model.ThemeConfig
import com.azavern.aviplayer.data.model.TitleConfig
import com.azavern.aviplayer.data.repository.FolderRepository
import com.azavern.aviplayer.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel Kustomisasi (Blueprint Bagian 7 & Milestone 7).
 * Memuat semua konfigurasi dari SettingsRepository; setiap perubahan
 * editor langsung disimpan, dan player (yang mengumpulkan Flow yang
 * sama) menerapkannya seketika.
 */
@HiltViewModel
class CustomizationViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    /** Alamat satu posisi slot di ControlLayoutConfig. */
    sealed class SlotPosition {
        data object TopLeft : SlotPosition()
        data class TopRight(val index: Int) : SlotPosition()
        data class Center(val index: Int) : SlotPosition()
        data class BottomLeft(val index: Int) : SlotPosition()
        data class BottomRight(val index: Int) : SlotPosition()
    }

    private val _uiState = MutableStateFlow(CustomizationUiState())
    val uiState: StateFlow<CustomizationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getPlayerUiConfig().collect { config ->
                _uiState.update {
                    it.copy(
                        titleConfig = config.titleConfig,
                        controlLayoutConfig = config.controlLayoutConfig,
                        themeConfig = config.themeConfig,
                        isLoading = false
                    )
                }
            }
        }
        // Whitelist folder (Milestone 9): config + semua folder.
        viewModelScope.launch {
            settingsRepository.getFolderVisibilityConfig().collect { config ->
                _uiState.update { it.copy(folderVisibilityConfig = config) }
            }
        }
        viewModelScope.launch {
            folderRepository.getAllFolders().collect { folders ->
                _uiState.update { it.copy(allFolders = folders) }
            }
        }
    }

    fun onTabSelected(tab: CustomizationTab) {
        _uiState.update { it.copy(activeTab = tab, selectedSlotA = null) }
    }

    fun onTitleConfigChanged(config: TitleConfig) {
        viewModelScope.launch { settingsRepository.updateTitleConfig(config) }
    }

    /** Perubahan tema dari editor Tema (Milestone 8). */
    fun onThemeConfigChanged(config: ThemeConfig) {
        viewModelScope.launch { settingsRepository.updateThemeConfig(config) }
    }

    /** Perubahan whitelist folder dari editor Folder (Milestone 9). */
    fun onFolderVisibilityChanged(config: FolderVisibilityConfig) {
        viewModelScope.launch { settingsRepository.updateFolderVisibilityConfig(config) }
    }

    fun onControlLayoutChanged(config: ControlLayoutConfig) {
        viewModelScope.launch { settingsRepository.updateControlLayoutConfig(config) }
    }

    fun onResetControlLayout() {
        _uiState.update { it.copy(selectedSlotA = null) }
        viewModelScope.launch { settingsRepository.resetControlLayoutToDefault() }
    }

    /**
     * Mekanisme tap-swap editor: ketukan pertama memilih slot,
     * ketukan pada slot lain menukar keduanya, ketukan pada slot
     * yang sama membatalkan pilihan.
     */
    fun onSlotTapped(position: SlotPosition) {
        val selected = _uiState.value.selectedSlotA
        when {
            selected == null ->
                _uiState.update { it.copy(selectedSlotA = position) }
            selected == position ->
                _uiState.update { it.copy(selectedSlotA = null) }
            else -> {
                swapControlSlots(selected, position)
                _uiState.update { it.copy(selectedSlotA = null) }
            }
        }
    }

    /** Tukar isi dua posisi slot lalu simpan layout baru. */
    fun swapControlSlots(posA: SlotPosition, posB: SlotPosition) {
        val config = _uiState.value.controlLayoutConfig
        val slotA = slotAt(config, posA)
        val slotB = slotAt(config, posB)
        if (slotA == slotB) return
        val updated = setSlot(setSlot(config, posA, slotB), posB, slotA)
        onControlLayoutChanged(updated)
    }

    companion object {
        /** Isi slot di posisi; indeks di luar daftar dianggap EMPTY. */
        fun slotAt(config: ControlLayoutConfig, position: SlotPosition): ControlSlot =
            when (position) {
                SlotPosition.TopLeft -> config.topLeftSlot
                is SlotPosition.TopRight ->
                    config.topRightSlots.getOrElse(position.index) { ControlSlot.EMPTY }
                is SlotPosition.Center ->
                    config.centerSlots.getOrElse(position.index) { ControlSlot.EMPTY }
                is SlotPosition.BottomLeft ->
                    config.bottomLeftSlots.getOrElse(position.index) { ControlSlot.EMPTY }
                is SlotPosition.BottomRight ->
                    config.bottomRightSlots.getOrElse(position.index) { ControlSlot.EMPTY }
            }

        /** Tulis slot ke posisi; indeks tepat di ujung daftar = sisipkan. */
        fun setSlot(
            config: ControlLayoutConfig,
            position: SlotPosition,
            slot: ControlSlot
        ): ControlLayoutConfig = when (position) {
            SlotPosition.TopLeft -> config.copy(topLeftSlot = slot)
            is SlotPosition.TopRight ->
                config.copy(topRightSlots = config.topRightSlots.setAt(position.index, slot))
            is SlotPosition.Center ->
                config.copy(centerSlots = config.centerSlots.setAt(position.index, slot))
            is SlotPosition.BottomLeft ->
                config.copy(bottomLeftSlots = config.bottomLeftSlots.setAt(position.index, slot))
            is SlotPosition.BottomRight ->
                config.copy(bottomRightSlots = config.bottomRightSlots.setAt(position.index, slot))
        }

        private fun List<ControlSlot>.setAt(index: Int, slot: ControlSlot): List<ControlSlot> =
            when {
                index < size -> toMutableList().also { it[index] = slot }
                index == size -> this + slot
                else -> this
            }
    }
}
