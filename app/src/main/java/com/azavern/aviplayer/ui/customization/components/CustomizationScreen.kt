package com.azavern.aviplayer.ui.customization.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azavern.aviplayer.R
import com.azavern.aviplayer.ui.customization.CustomizationTab
import com.azavern.aviplayer.ui.customization.CustomizationViewModel

/**
 * Root layar Kustomisasi (Milestone 7 + 8 + 9): TabRow empat tab —
 * Judul, Tombol, Tema, dan Folder (whitelist).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationScreen(
    viewModel: CustomizationViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.customization_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.player_action_back)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = uiState.activeTab.ordinal) {
                Tab(
                    selected = uiState.activeTab == CustomizationTab.TITLE,
                    onClick = { viewModel.onTabSelected(CustomizationTab.TITLE) },
                    text = { Text(stringResource(R.string.customization_tab_title)) }
                )
                Tab(
                    selected = uiState.activeTab == CustomizationTab.CONTROLS,
                    onClick = { viewModel.onTabSelected(CustomizationTab.CONTROLS) },
                    text = { Text(stringResource(R.string.customization_tab_controls)) }
                )
                Tab(
                    selected = uiState.activeTab == CustomizationTab.THEME,
                    onClick = { viewModel.onTabSelected(CustomizationTab.THEME) },
                    text = { Text(stringResource(R.string.customization_tab_theme)) }
                )
                Tab(
                    selected = uiState.activeTab == CustomizationTab.WHITELIST,
                    onClick = { viewModel.onTabSelected(CustomizationTab.WHITELIST) },
                    text = { Text(stringResource(R.string.customization_tab_whitelist)) }
                )
            }
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                when (uiState.activeTab) {
                    CustomizationTab.TITLE -> TitleStyleEditor(
                        titleConfig = uiState.titleConfig,
                        onTitleConfigChanged = viewModel::onTitleConfigChanged
                    )
                    CustomizationTab.CONTROLS -> ControlSwapEditor(
                        layout = uiState.controlLayoutConfig,
                        selectedSlotA = uiState.selectedSlotA,
                        onSlotTapped = viewModel::onSlotTapped,
                        onResetLayout = viewModel::onResetControlLayout,
                        seekConfig = uiState.seekConfig,
                        onSeekConfigChanged = viewModel::onSeekConfigChanged
                    )
                    CustomizationTab.THEME -> ThemeEditor(
                        themeConfig = uiState.themeConfig,
                        onThemeConfigChanged = viewModel::onThemeConfigChanged
                    )
                    CustomizationTab.WHITELIST -> WhitelistEditor(
                        config = uiState.folderVisibilityConfig,
                        allFolders = uiState.allFolders,
                        onConfigChanged = viewModel::onFolderVisibilityChanged
                    )
                }
            }
        }
    }
}
