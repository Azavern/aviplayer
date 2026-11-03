package com.azavern.aviplayer.player.core

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

/**
 * Jantung aplikasi (Blueprint Bagian 7): wrapper eksklusif untuk semua
 * interaksi ExoPlayer. Layer lain TIDAK boleh menyentuh ExoPlayer
 * langsung — hanya lewat manager ini (satu pemilik, AGENTS.md §5.3).
 *
 * Instance manager dibuat per sesi PlayerActivity lewat PlayerModule.
 * [release] WAJIB dipanggil dari `PlayerActivity.onStop()`
 * (Blueprint Bagian 10A). ExoPlayer yang sudah di-release tidak bisa
 * dipakai lagi, jadi instance ExoPlayer dibuat ulang secara internal
 * pada [prepare] berikutnya — manager-nya yang berumur se-sesi,
 * bukan ExoPlayer-nya.
 */
class VideoPlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private var eventListener: PlayerEventListener? = null

    /** Player aktif untuk VideoSurface; dibuat saat pertama dibutuhkan atau setelah release. */
    val player: ExoPlayer
        get() = exoPlayer ?: buildPlayer()

    /** True setelah [release]; dipakai ViewModel untuk prepare ulang saat Activity kembali. */
    var isReleased: Boolean = false
        private set

    /** Daftarkan listener event (satu untuk seluruh sesi; menempel ke setiap instance baru). */
    fun setEventListener(listener: PlayerEventListener) {
        eventListener?.let { old -> exoPlayer?.removeListener(old) }
        eventListener = listener
        exoPlayer?.addListener(listener)
    }

    fun prepare(videoPath: String, startPositionMs: Long) {
        val active = player
        val mediaItem = MediaItem.fromUri(Uri.parse(videoPath))
        active.setMediaItem(mediaItem)
        active.seekTo(startPositionMs)
        active.prepare()
        active.playWhenReady = true
        isReleased = false
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs.coerceAtLeast(0L))
    }

    /** Maju [stepMs] dari posisi sekarang. */
    fun seekForward(stepMs: Long) {
        seekTo(getCurrentPosition() + stepMs)
    }

    /** Mundur [stepMs] dari posisi sekarang (tidak negatif). */
    fun seekBackward(stepMs: Long) {
        seekTo((getCurrentPosition() - stepMs).coerceAtLeast(0L))
    }

    fun setSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
    }

    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0L

    /** Durasi total; bisa `C.TIME_UNSET` sebelum player siap — pemanggil wajib menormalisasi. */
    fun getDuration(): Long = exoPlayer?.duration ?: 0L

    fun isPlaying(): Boolean = exoPlayer?.isPlaying ?: false

    /** Lepas player sepenuhnya. Aman dipanggil lebih dari sekali. */
    fun release() {
        val active = exoPlayer ?: run {
            isReleased = true
            return
        }
        eventListener?.let { active.removeListener(it) }
        active.stop()
        active.release()
        exoPlayer = null
        isReleased = true
    }

    private fun buildPlayer(): ExoPlayer =
        ExoPlayer.Builder(context).build().also { created ->
            exoPlayer = created
            eventListener?.let { created.addListener(it) }
        }
}
