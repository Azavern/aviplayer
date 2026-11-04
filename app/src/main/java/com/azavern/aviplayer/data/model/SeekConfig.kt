package com.azavern.aviplayer.data.model

/**
 * Durasi lewati untuk double-tap seek (permintaan Veo 2026-11-04):
 * maju dan mundur diatur TERPISAH, masing-masing dari pilihan
 * kelipatan 5/10/15/30/45 detik. Bawaan keduanya 10 detik (perilaku
 * aplikasi sebelum pengaturan ini ada).
 */
data class SeekConfig(
    val forwardSeconds: Int = 10,
    val backwardSeconds: Int = 10
) {
    val forwardMs: Long get() = forwardSeconds * 1000L
    val backwardMs: Long get() = backwardSeconds * 1000L

    companion object {
        val Default = SeekConfig()

        /** Satu-satunya pilihan durasi yang ditawarkan ke pengguna. */
        val OPTIONS = listOf(5, 10, 15, 30, 45)

        /** Nilai di luar pilihan (mis. sisa preferensi lama) jatuh ke 10. */
        fun sanitize(seconds: Int): Int = if (seconds in OPTIONS) seconds else 10
    }
}
