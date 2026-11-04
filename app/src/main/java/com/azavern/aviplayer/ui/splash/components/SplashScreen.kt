package com.azavern.aviplayer.ui.splash.components

import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Panjang timeline animasi splash (master clock demo): 4600 ms. */
private const val SPLASH_DURATION_MS = 4600f

/**
 * Layar splash AviPlay: latar hitam, cahaya ambient biru & violet
 * di belakang logo, logo beranimasi per lapisan (lihat
 * [drawAviPlayLogo]), dan kilat bloom sebagai penutup.
 *
 * Setara demo web Veo, minus tombol "Putar animasi" (itu hanya
 * untuk demo): animasi berjalan sendiri, ketuk di mana pun untuk
 * melewati, dan bila sistem mematikan animasi (skala durasi
 * animator 0 / mode gerak dikurangi) splash langsung selesai —
 * padanan perilaku prefers-reduced-motion di demo.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var timeMs by remember { mutableFloatStateOf(0f) }
    var finished by remember { mutableStateOf(false) }

    fun finishOnce() {
        if (!finished) {
            finished = true
            onFinished()
        }
    }

    LaunchedEffect(Unit) {
        val animatorScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        if (animatorScale == 0f) {
            finishOnce()
            return@LaunchedEffect
        }
        var startNanos = -1L
        while (true) {
            val now = withFrameNanos { it }
            if (startNanos < 0L) startNanos = now
            val elapsedMs = (now - startNanos) / 1_000_000f
            timeMs = elapsedMs.coerceAtMost(SPLASH_DURATION_MS)
            if (elapsedMs >= SPLASH_DURATION_MS) break
        }
        finishOnce()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { finishOnce() }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = timeMs
            // `center` di sini adalah DrawScope.center (pusat canvas).
            // Ukuran logo ala demo: 82% sisi terpendek layar, maks 680dp.
            val logoPx = minOf(size.width, size.height) * 0.82f
                .coerceAtMost(680.dp.toPx())
            val ambient = ambientStateAt(t)

            // Cahaya ambient biru (di belakang logo, blend screen).
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0x8C1E5AFF),
                        0.38f to Color(0x381E3CFF),
                        0.68f to Color(0x001E3CFF)
                    ),
                    center = center,
                    radius = logoPx * 1.55f / 2f * ambient.blueScale
                ),
                center = center,
                radius = logoPx * 1.55f / 2f * ambient.blueScale,
                alpha = ambient.blueAlpha,
                blendMode = BlendMode.Screen
            )
            // Cahaya ambient violet, bergeser (12%, 10%) dari pusat.
            val violetRadius = logoPx * 1.35f / 2f * ambient.violetScale
            val violetCenter = Offset(
                x = center.x + logoPx * 1.35f * 0.12f,
                y = center.y + logoPx * 1.35f * 0.10f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0x80AA28FF),
                        0.4f to Color(0x338214FF),
                        0.68f to Color(0x008214FF)
                    ),
                    center = violetCenter,
                    radius = violetRadius
                ),
                center = violetCenter,
                radius = violetRadius,
                alpha = ambient.violetAlpha,
                blendMode = BlendMode.Screen
            )

            // Logo beranimasi: petakan viewBox 1254 ke kotak logo.
            val scale = logoPx / 1254f
            withTransform({
                translate(center.x - logoPx / 2f, center.y - logoPx / 2f)
                // Pivot WAJIB eksplisit di titik nol: bawaan scale()
                // Compose adalah tengah area gambar, dan itulah yang
                // sempat melempar logo keluar dari tengah layar.
                scale(scale, scale, Offset.Zero)
            }) {
                drawAviPlayLogo(t)
            }

            // Kilat bloom penutup (di atas logo, blend screen).
            if (ambient.bloomAlpha > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color(0xBFBEA0FF),
                            0.4f to Color(0x4D6E46FF),
                            0.7f to Color(0x006E46FF)
                        ),
                        center = center,
                        radius = logoPx * 1.2f / 2f
                    ),
                    center = center,
                    radius = logoPx * 1.2f / 2f,
                    alpha = ambient.bloomAlpha,
                    blendMode = BlendMode.Screen
                )
            }
        }
    }
}
