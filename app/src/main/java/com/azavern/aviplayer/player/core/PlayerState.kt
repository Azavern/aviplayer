package com.azavern.aviplayer.player.core

/**
 * Status player yang dikonsumsi ViewModel/UI (Blueprint Bagian 7) —
 * abstraksi di atas state mentah ExoPlayer agar UI tidak bergantung
 * pada konstanta Media3.
 */
sealed class PlayerState {
    object Idle : PlayerState()
    object Loading : PlayerState()
    object Playing : PlayerState()
    object Paused : PlayerState()
    object Ended : PlayerState()
    data class Error(val message: String) : PlayerState()
}
