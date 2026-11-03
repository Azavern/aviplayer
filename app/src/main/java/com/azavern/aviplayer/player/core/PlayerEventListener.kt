package com.azavern.aviplayer.player.core

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize

/**
 * Event player yang sudah disederhanakan untuk ViewModel
 * (Blueprint Bagian 7).
 */
sealed class PlayerEvent {
    /** Konstanta state Media3: Player.STATE_IDLE/BUFFERING/READY/ENDED. */
    data class PlaybackStateChanged(val playbackState: Int) : PlayerEvent()
    data class IsPlayingChanged(val isPlaying: Boolean) : PlayerEvent()
    data class Error(val message: String) : PlayerEvent()
    data class VideoSizeChanged(val videoSize: VideoSize) : PlayerEvent()
    data class PositionDiscontinuity(val reason: Int) : PlayerEvent()
}

/**
 * Mengimplementasi [Player.Listener] dari Media3 dan meneruskan event
 * sebagai [PlayerEvent] lewat [onEvent] (Blueprint Bagian 7).
 * Di-register ke player oleh VideoPlayerManager.
 */
class PlayerEventListener(
    private val onEvent: (PlayerEvent) -> Unit
) : Player.Listener {

    override fun onPlaybackStateChanged(playbackState: Int) {
        onEvent(PlayerEvent.PlaybackStateChanged(playbackState))
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        onEvent(PlayerEvent.IsPlayingChanged(isPlaying))
    }

    override fun onPlayerError(error: PlaybackException) {
        onEvent(PlayerEvent.Error(error.message ?: "Kesalahan pemutaran"))
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        onEvent(PlayerEvent.VideoSizeChanged(videoSize))
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        onEvent(PlayerEvent.PositionDiscontinuity(reason))
    }
}
