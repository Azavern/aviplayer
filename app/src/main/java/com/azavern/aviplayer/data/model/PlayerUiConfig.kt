package com.azavern.aviplayer.data.model

/**
 * Gabungan semua konfigurasi tampilan player (Blueprint Bagian 8):
 * judul + layout kontrol + tema. Sumbernya SettingsRepository
 * .getPlayerUiConfig() — tidak ada nilai tampilan yang di-hardcode
 * di Composable.
 */
data class PlayerUiConfig(
    val titleConfig: TitleConfig = TitleConfig.Default,
    val controlLayoutConfig: ControlLayoutConfig = ControlLayoutConfig.Default,
    val themeConfig: ThemeConfig = ThemeConfig.Default
) {
    companion object {
        val Default = PlayerUiConfig()
    }
}
