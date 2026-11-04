package com.azavern.aviplayer.data.model

import kotlinx.serialization.Serializable

/**
 * Jenis tombol yang bisa menempati slot kontrol player
 * (Blueprint Bagian 8). Diserialkan sebagai nama enum di JSON layout.
 *
 * Yang sudah berfungsi: PLAY_PAUSE, PREVIOUS, NEXT, ORIENTATION,
 * CAPTURE, SPEED, dan MUTE (tombol bisu, tambahan Veo 2026-11-04).
 * LOCK dan SCREEN_RATIO menunggu milestone fiturnya; keduanya
 * tidak ditawarkan editor dan tidak dirender player (slot-nya
 * diperlakukan seperti EMPTY).
 */
@Serializable
enum class ControlSlot {
    PLAY_PAUSE,
    PREVIOUS,
    NEXT,
    LOCK,
    ORIENTATION,
    CAPTURE,
    SPEED,
    SCREEN_RATIO,
    MUTE,
    EMPTY
}
