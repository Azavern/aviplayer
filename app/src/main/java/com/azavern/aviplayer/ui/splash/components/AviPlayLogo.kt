package com.azavern.aviplayer.ui.splash.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.util.lerp

/**
 * Logo AviPlay digambar per lapisan di Canvas, setia pada artwork
 * SVG asli Veo (viewBox 1254×1254, lingkaran pusat (627,617) r=570)
 * dan timeline animasi demo splash-nya (master clock 4600 ms):
 *
 * - Kamera: push-in halus scale 1.07 → 1 selama 4200 ms.
 * - Lingkaran (dasar + azure + magenta) terbentuk dengan overshoot
 *   halus dari scale .55; lapisan shade menyusul mengecil dari 1.25.
 * - Segitiga play meluncur masuk dari kiri; sheen-nya memudar masuk.
 * - Sapuan cahaya (sweep) menyapu lingkaran sekali, blend overlay.
 * - Huruf "AviPlay" masuk bergantian (stagger) dari bawah; titik
 *   huruf i jatuh dari atas; lubang play pada A meletup (pop).
 * - Penutup: halo teks bernapas, kilat bloom (di SplashScreen), dan
 *   ring mengembang lalu hilang.
 *
 * Penyimpangan sadar dari demo web: blur per-lapisan (filter CSS)
 * tidak direproduksi — Compose Canvas tidak menyediakan blur per
 * primitif gambar; koreografi opacity/posisi/skala dipertahankan.
 * Warna di sini adalah warna artwork (spesifikasi merek dari file
 * Veo), bukan token tema aplikasi.
 *
 * Fungsi ini mengharapkan transform DrawScope sudah memetakan
 * koordinat SVG (1254×1254) ke layar (dilakukan SplashScreen).
 */
internal fun DrawScope.drawAviPlayLogo(tMs: Float) {
    val cameraScale = lerp(1.07f, 1f, EaseOut.transform(progress(tMs, 0f, 4200f)))
    // Poros eksplisit di pusat logo: scale() Compose berpivot
    // bawaan di tengah area gambar, bukan di titik yang kita mau.
    withTransform({
        scale(cameraScale, cameraScale, Offset(627f, 617f))
    }) {
        drawCircleLayers(tMs)
        drawPlayLayer(tMs)
        drawSweepLayer(tMs)
        drawTextLayers(tMs)
        drawRingLayer(tMs)
    }
}

// ---------- Lapisan lingkaran ----------

private fun DrawScope.drawCircleLayers(t: Float) {
    // Tiga lapisan lingkaran berbagi koreografi "circleIn" yang sama.
    val p = progress(t, 250f, 1500f)
    if (p > 0f) {
        val alpha = key3(p, 0.68f, EaseOut, 0f, 1f, 1f)
        val ty = key3(p, 0.68f, EaseOut, 40f, -4f, 0f)
        val scale = key3(p, 0.68f, EaseOut, 0.55f, 1.025f, 1f)
        layerTransform(627f, 617f, 0f, ty, scale) {
            drawCircle(brush = circleBrush(), center = Offset(627f, 617f), radius = 570f, alpha = alpha)
            drawCircle(brush = azureBrush(), center = Offset(627f, 617f), radius = 570f, alpha = alpha)
            drawCircle(brush = magentaBrush(), center = Offset(627f, 617f), radius = 570f, alpha = alpha)
        }
    }
    // Shade: masuk belakangan, mengecil dari 1.25.
    val shadeP = progress(t, 700f, 1500f)
    if (shadeP > 0f) {
        layerTransform(627f, 617f, 0f, 0f, lerp(1.25f, 1f, EaseSoft.transform(shadeP))) {
            drawCircle(
                brush = shadeBrush(), center = Offset(627f, 617f), radius = 570f,
                alpha = EaseSoft.transform(shadeP)
            )
        }
    }
    // Rim: memudar masuk.
    val rimP = progress(t, 900f, 900f)
    if (rimP > 0f) {
        drawCircle(
            brush = rimBrush(), center = Offset(627f, 617f), radius = 567f,
            alpha = EaseSoft.transform(rimP), style = Stroke(width = 3f)
        )
    }
}

// ---------- Segitiga play ----------

private fun DrawScope.drawPlayLayer(t: Float) {
    val p = progress(t, 950f, 1500f)
    if (p <= 0f) return
    val alpha = key3(p, 0.7f, EaseOut, 0f, 1f, 1f)
    val tx = key3(p, 0.7f, EaseOut, -170f, 12f, 0f)
    val scale = key3(p, 0.7f, EaseOut, 0.55f, 1.035f, 1f)
    // Poros transform = pusat bbox play ≈ (745, 617).
    layerTransform(745f, 617f, tx, 0f, scale) {
        clipPath(circleClipPath()) {
            drawPath(LogoPaths.play, brush = playBrush(), alpha = alpha)
            val sheenP = progress(t, 1250f, 1300f)
            if (sheenP > 0f) {
                drawPath(
                    LogoPaths.play, brush = playSheenBrush(),
                    alpha = alpha * EaseSoft.transform(sheenP)
                )
            }
        }
    }
}

// ---------- Sapuan cahaya ----------

private fun DrawScope.drawSweepLayer(t: Float) {
    val p = progress(t, 1500f, 1300f)
    if (p <= 0f || p >= 1f) return
    val tx = multiKey(p, SWEEP_OFFSETS, SWEEP_TX, EaseInOut)
    val alpha = multiKey(p, SWEEP_OFFSETS, SWEEP_ALPHA, EaseInOut)
    if (alpha <= 0f) return
    clipPath(circleClipPath()) {
        val band = Path().apply {
            // Pita selebar 300, tinggi menutupi lingkaran, miring -18°.
            moveTo(tx + 265f, -200f)
            lineTo(tx + 565f, -200f)
            lineTo(tx + 13f, 1500f)
            lineTo(tx - 287f, 1500f)
            close()
        }
        drawPath(
            band,
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0f to Color.White.copy(alpha = 0f),
                    0.5f to Color.White.copy(alpha = 0.5f),
                    1f to Color.White.copy(alpha = 0f)
                ),
                start = Offset(tx - 287f, 0f),
                end = Offset(tx + 565f, 0f)
            ),
            alpha = alpha,
            // Demo memakai soft-light; BlendMode Compose tak punya
            // SoftLight — Overlay adalah padanan terdekatnya.
            blendMode = BlendMode.Overlay
        )
    }
}

private val SWEEP_OFFSETS = floatArrayOf(0f, 0.2f, 0.8f, 1f)
private val SWEEP_TX = floatArrayOf(-420f, 120f, 1100f, 1500f)
private val SWEEP_ALPHA = floatArrayOf(0f, 1f, 1f, 0f)

// ---------- Teks "AviPlay" ----------

private fun DrawScope.drawTextLayers(t: Float) {
    // Halo di belakang teks: bernapas masuk lalu menetap di .28.
    val haloP = progress(t, 2300f, 1500f)
    if (haloP > 0f) {
        val haloAlpha = multiKey(
            haloP, floatArrayOf(0f, 0.35f, 1f), floatArrayOf(0f, 0.8f, 0.28f), EaseSoft
        )
        drawOval(
            brush = haloBrush(),
            topLeft = Offset(640f - 600f, 620f - 190f),
            size = Size(1200f, 380f),
            alpha = haloAlpha
        )
    }

    // Dua lintasan: bayangan lembut dulu (pengganti drop-shadow CSS),
    // lalu hurufnya. Keduanya memakai alpha lapisan yang sama.
    drawLetterSet(t, shadowPass = true)
    drawLetterSet(t, shadowPass = false)
}

private fun DrawScope.drawLetterSet(t: Float, shadowPass: Boolean) {
    fun ink(brush: Brush? = null): Pair<Color, Brush?> =
        if (shadowPass) SHADOW_COLOR to null else Color.White to brush

    // Huruf A (badan + lubang play yang meletup belakangan).
    letterLayer(t, start = 1500f, dur = 1000f, cx = 266f, cy = 576f) { layerAlpha ->
        if (shadowPass) {
            drawPath(LogoPaths.aBody, color = SHADOW_COLOR, alpha = 0.3f * layerAlpha)
        } else {
            val holeP = progress(t, 2050f, 900f)
            if (holeP > 0f) {
                val holeScale = key3(holeP, 0.6f, EaseOut, 0.2f, 1.22f, 1f)
                layerTransform(280f, 617f, 0f, 0f, holeScale) {
                    drawPath(
                        LogoPaths.aHole, brush = holeBrush(),
                        alpha = key3(holeP, 0.6f, EaseOut, 0f, 1f, 1f)
                    )
                }
            }
            drawPath(LogoPaths.aBody, color = Color.White, alpha = layerAlpha)
        }
    }
    // v
    letterLayer(t, start = 1750f, dur = 900f, cx = 444f, cy = 621f) { layerAlpha ->
        val (color, _) = ink()
        drawPath(
            LogoPaths.letterV, color = color,
            alpha = (if (shadowPass) 0.3f else 1f) * layerAlpha
        )
    }
    // i: batang…
    letterLayer(t, start = 1830f, dur = 900f, cx = 554f, cy = 621f) { layerAlpha ->
        val (color, _) = ink()
        drawRoundRect(
            color = color,
            topLeft = Offset(531f, 543f),
            size = Size(46f, 157f),
            cornerRadius = CornerRadius(9f, 9f),
            alpha = (if (shadowPass) 0.3f else 1f) * layerAlpha
        )
    }
    // …dan titiknya, jatuh dari atas (hanya lintasan isi).
    if (!shadowPass) {
        val dotP = progress(t, 1960f, 800f)
        if (dotP > 0f) {
            val dotTy = key3(dotP, 0.7f, EaseOut, -70f, 5f, 0f)
            val dotScale = key3(dotP, 0.7f, EaseOut, 0.6f, 1.06f, 1f)
            layerTransform(554f, 506f, 0f, dotTy, dotScale) {
                drawCircle(
                    color = Color.White, center = Offset(554f, 506f), radius = 27f,
                    alpha = key3(dotP, 0.7f, EaseOut, 0f, 1f, 1f)
                )
            }
        }
    }
    // P, l, a, y — gradasi lavender (bayangan tetap warna deep).
    letterLayer(t, start = 1910f, dur = 900f, cx = 694f, cy = 591f) { layerAlpha ->
        if (shadowPass) {
            drawPath(LogoPaths.letterP, color = SHADOW_COLOR, alpha = 0.3f * layerAlpha)
        } else {
            drawPath(LogoPaths.letterP, brush = textBrush(), alpha = layerAlpha)
        }
    }
    letterLayer(t, start = 2010f, dur = 900f, cx = 814f, cy = 591f) { layerAlpha ->
        if (shadowPass) {
            drawRoundRect(
                color = SHADOW_COLOR, topLeft = Offset(791f, 485f),
                size = Size(47f, 213f), cornerRadius = CornerRadius(9f, 9f),
                alpha = 0.3f * layerAlpha
            )
        } else {
            drawRoundRect(
                brush = textBrush(), topLeft = Offset(791f, 485f),
                size = Size(47f, 213f), cornerRadius = CornerRadius(9f, 9f),
                alpha = layerAlpha
            )
        }
    }
    letterLayer(t, start = 2090f, dur = 900f, cx = 926f, cy = 622f) { layerAlpha ->
        if (shadowPass) {
            drawPath(LogoPaths.letterA, color = SHADOW_COLOR, alpha = 0.3f * layerAlpha)
        } else {
            drawPath(LogoPaths.letterA, brush = textBrush(), alpha = layerAlpha)
        }
    }
    letterLayer(t, start = 2170f, dur = 900f, cx = 1074f, cy = 653f) { layerAlpha ->
        if (shadowPass) {
            drawPath(LogoPaths.letterY, color = SHADOW_COLOR, alpha = 0.3f * layerAlpha)
        } else {
            drawPath(LogoPaths.letterY, brush = textBrush(), alpha = layerAlpha)
        }
    }
}

/** Satu huruf dengan koreografi "letterIn": naik + memudar + overshoot. */
private fun DrawScope.letterLayer(
    t: Float,
    start: Float,
    dur: Float,
    cx: Float,
    cy: Float,
    draw: DrawScope.(layerAlpha: Float) -> Unit
) {
    val p = progress(t, start, dur)
    if (p <= 0f) return
    val alpha = key3(p, 0.68f, EaseOut, 0f, 1f, 1f)
    val ty = key3(p, 0.68f, EaseOut, 42f, -5f, 0f)
    val scale = key3(p, 0.68f, EaseOut, 0.9f, 1.012f, 1f)
    layerTransform(cx, cy, 0f, ty, scale) { draw(alpha) }
}

// ---------- Ring penutup ----------

private fun DrawScope.drawRingLayer(t: Float) {
    val p = progress(t, 2300f, 1100f)
    if (p <= 0f || p >= 1f) return
    val alpha = multiKey(p, floatArrayOf(0f, 0.25f, 1f), floatArrayOf(0f, 0.8f, 0f), EaseOut)
    val scale = lerp(0.96f, 1.16f, EaseOut.transform(p))
    layerTransform(627f, 617f, 0f, 0f, scale) {
        drawCircle(
            brush = ringBrush(), center = Offset(627f, 617f), radius = 572f,
            alpha = alpha, style = Stroke(width = 4f)
        )
    }
}

// ---------- Ambient & bloom (dipakai SplashScreen, ruang layar) ----------

/** Keadaan cahaya ambient pada waktu [t] — mengikuti track demo. */
internal data class AmbientState(
    val blueAlpha: Float,
    val blueScale: Float,
    val violetAlpha: Float,
    val violetScale: Float,
    val bloomAlpha: Float
)

internal fun ambientStateAt(t: Float): AmbientState {
    val total = progress(t, 0f, 4600f)
    val offsets = floatArrayOf(0f, 0.348f, 0.63f, 1f)
    val blueAlpha = multiKey(total, offsets, floatArrayOf(0f, 0.9f, 1f, 0.6f), EaseSoft) * 0.6f
    val blueScale = multiKey(total, offsets, floatArrayOf(0.45f, 0.95f, 1.12f, 1f), EaseSoft)
    val violetOffsets = floatArrayOf(0f, 0.1f, 0.43f, 0.63f, 1f)
    val violetAlpha = multiKey(
        total, violetOffsets, floatArrayOf(0f, 0f, 0.8f, 1f, 0.55f), EaseSoft
    ) * 0.55f
    val violetScale = multiKey(
        total, violetOffsets, floatArrayOf(0.4f, 0.45f, 0.95f, 1.1f, 1f), EaseSoft
    )
    val bloomP = progress(t, 2400f, 1000f)
    val bloomAlpha = if (bloomP <= 0f || bloomP >= 1f) {
        0f
    } else {
        multiKey(bloomP, floatArrayOf(0f, 0.3f, 1f), floatArrayOf(0f, 0.5f, 0f), EaseSoft)
    }
    return AmbientState(blueAlpha, blueScale, violetAlpha, violetScale, bloomAlpha)
}

// ---------- Util transform & keyframe ----------

/**
 * Transform satu lapisan ala CSS: geser (tx, ty), lalu skala
 * berporos di (cx, cy) — poros disebut eksplisit karena bawaan
 * scale() Compose adalah tengah area gambar.
 */
private fun DrawScope.layerTransform(
    cx: Float,
    cy: Float,
    tx: Float,
    ty: Float,
    scale: Float,
    draw: DrawScope.() -> Unit
) {
    withTransform({
        translate(tx, ty)
        scale(scale, scale, Offset(cx, cy))
    }) { draw() }
}

private fun progress(t: Float, start: Float, dur: Float): Float =
    ((t - start) / dur).coerceIn(0f, 1f)

/** Interpolasi 3 keyframe: from → mid (di [midOffset]) → to. */
private fun key3(
    p: Float,
    midOffset: Float,
    easing: Easing,
    from: Float,
    mid: Float,
    to: Float
): Float = if (p < midOffset) {
    lerp(from, mid, easing.transform(p / midOffset))
} else {
    lerp(mid, to, easing.transform((p - midOffset) / (1f - midOffset)))
}

/** Interpolasi multi-keyframe mengikuti deretan offset & nilai. */
private fun multiKey(p: Float, offsets: FloatArray, values: FloatArray, easing: Easing): Float {
    if (p <= offsets.first()) return values.first()
    for (i in 1 until offsets.size) {
        if (p <= offsets[i]) {
            val local = (p - offsets[i - 1]) / (offsets[i] - offsets[i - 1])
            return lerp(values[i - 1], values[i], easing.transform(local))
        }
    }
    return values.last()
}

private val EaseOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val EaseInOut = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
private val EaseSoft = CubicBezierEasing(0.33f, 0f, 0.2f, 1f)

private val SHADOW_COLOR = Color(0xFF060046)

// ---------- Brush artwork (koordinat SVG) ----------

private fun circleBrush() = Brush.linearGradient(
    colorStops = arrayOf(
        0f to Color(0xFF00B8FF), 0.34f to Color(0xFF1B45FF),
        0.72f to Color(0xFF4A00F0), 1f to Color(0xFF6A00EE)
    ),
    start = Offset(171f, 104f), end = Offset(1083f, 1130f)
)

private fun azureBrush() = Brush.radialGradient(
    colorStops = arrayOf(0f to Color(0xF20A3CFF), 1f to Color(0x000A3CFF)),
    center = Offset(103f, 777f), radius = 570f
)

private fun magentaBrush() = Brush.radialGradient(
    colorStops = arrayOf(
        0f to Color(0xFFE016FF), 0.55f to Color(0x4DE016FF), 1f to Color(0x00E016FF)
    ),
    center = Offset(1060f, 1096f), radius = 627f
)

private fun shadeBrush() = Brush.radialGradient(
    colorStops = arrayOf(
        0f to Color(0xF208005C), 0.55f to Color(0x8008005C), 1f to Color(0x0008005C)
    ),
    center = Offset(627f, 685f), radius = 593f
)

private fun rimBrush() = Brush.linearGradient(
    colorStops = arrayOf(
        0f to Color(0x8CFFFFFF), 0.5f to Color(0x0AFFFFFF), 1f to Color(0x59FF9BFF)
    ),
    start = Offset(60f, 50f), end = Offset(1194f, 1184f)
)

private fun playBrush() = Brush.linearGradient(
    colorStops = arrayOf(
        0f to Color(0xF22CC2FF), 0.22f to Color(0xB83C58FF),
        0.6f to Color(0x611A0098), 1f to Color(0x1F5000C8)
    ),
    start = Offset(343f, 306f), end = Offset(1107f, 617f)
)

private fun playSheenBrush() = Brush.linearGradient(
    colorStops = arrayOf(
        0f to Color(0xD97A66FF), 0.5f to Color(0x405A3CFF), 1f to Color(0x005A3CFF)
    ),
    start = Offset(343f, 1007f), end = Offset(801f, 384f)
)

private fun textBrush() = Brush.linearGradient(
    colorStops = arrayOf(0f to Color(0xFFEEE8FF), 1f to Color(0xFFC682FF)),
    start = Offset(0f, 480f), end = Offset(0f, 770f)
)

private fun holeBrush() = Brush.linearGradient(
    colorStops = arrayOf(0f to Color(0xFF2A10E6), 1f to Color(0xFF7A3CFF)),
    start = Offset(237f, 567f), end = Offset(324f, 667f)
)

private fun haloBrush() = Brush.radialGradient(
    colorStops = arrayOf(
        0f to Color(0x8CC9B8FF), 0.6f to Color(0x2E7A4CFF), 1f to Color(0x007A4CFF)
    ),
    center = Offset(640f, 620f), radius = 600f
)

private fun ringBrush() = Brush.linearGradient(
    colorStops = arrayOf(0f to Color(0xFF00B8FF), 1f to Color(0xFFE016FF)),
    start = Offset(0f, 0f), end = Offset(1254f, 1254f)
)

private fun circleClipPath(): Path = Path().apply {
    addOval(Rect(left = 57f, top = 47f, right = 1197f, bottom = 1187f))
}

// ---------- Path artwork (dari SVG asli, satuan viewBox) ----------

private object LogoPaths {
    val play: Path by lazy {
        Path().apply {
            moveTo(343f, 320f)
            quadraticBezierTo(343f, 190f, 444f, 244f)
            lineTo(1076f, 580f)
            quadraticBezierTo(1147f, 617f, 1076f, 655f)
            lineTo(444f, 990f)
            quadraticBezierTo(343f, 1044f, 343f, 914f)
            close()
        }
    }
    val aHole: Path by lazy {
        Path().apply {
            moveTo(243f, 567f)
            lineTo(313f, 608f)
            quadraticBezierTo(324f, 614f, 313f, 621f)
            lineTo(250f, 661f)
            quadraticBezierTo(237f, 667f, 237f, 652f)
            lineTo(237f, 578f)
            quadraticBezierTo(237f, 571f, 243f, 567f)
            close()
        }
    }
    val aBody: Path by lazy {
        Path().apply {
            fillType = PathFillType.EvenOdd
            moveTo(128f, 706f)
            lineTo(228f, 497f)
            quadraticBezierTo(270f, 440f, 312f, 497f)
            lineTo(405f, 700f)
            quadraticBezierTo(410f, 712f, 398f, 712f)
            lineTo(356f, 712f)
            quadraticBezierTo(346f, 712f, 342f, 703f)
            lineTo(336f, 692f)
            quadraticBezierTo(331f, 682f, 318f, 682f)
            lineTo(224f, 682f)
            quadraticBezierTo(210f, 682f, 206f, 692f)
            lineTo(200f, 703f)
            quadraticBezierTo(196f, 712f, 186f, 712f)
            lineTo(136f, 712f)
            quadraticBezierTo(124f, 712f, 128f, 706f)
            close()
            moveTo(243f, 567f)
            lineTo(313f, 608f)
            quadraticBezierTo(324f, 614f, 313f, 621f)
            lineTo(250f, 661f)
            quadraticBezierTo(237f, 667f, 237f, 652f)
            lineTo(237f, 578f)
            quadraticBezierTo(237f, 571f, 243f, 567f)
            close()
        }
    }
    val letterV: Path by lazy {
        Path().apply {
            moveTo(363f, 553f)
            quadraticBezierTo(362f, 543f, 374f, 543f)
            lineTo(406f, 543f)
            quadraticBezierTo(417f, 543f, 421f, 553f)
            lineTo(446f, 620f)
            lineTo(475f, 553f)
            quadraticBezierTo(479f, 543f, 490f, 543f)
            lineTo(514f, 543f)
            quadraticBezierTo(526f, 543f, 522f, 555f)
            lineTo(474f, 688f)
            quadraticBezierTo(470f, 700f, 458f, 700f)
            lineTo(430f, 700f)
            quadraticBezierTo(418f, 700f, 414f, 688f)
            close()
        }
    }
    val letterP: Path by lazy {
        Path().apply {
            fillType = PathFillType.EvenOdd
            moveTo(616f, 485f)
            lineTo(735f, 485f)
            quadraticBezierTo(780f, 485f, 780f, 555f)
            quadraticBezierTo(780f, 625f, 735f, 625f)
            lineTo(657f, 625f)
            lineTo(657f, 690f)
            quadraticBezierTo(657f, 698f, 649f, 698f)
            lineTo(616f, 698f)
            quadraticBezierTo(608f, 698f, 608f, 690f)
            lineTo(608f, 493f)
            quadraticBezierTo(608f, 485f, 616f, 485f)
            close()
            moveTo(657f, 521f)
            lineTo(718f, 521f)
            quadraticBezierTo(728f, 521f, 728f, 555f)
            quadraticBezierTo(728f, 589f, 718f, 589f)
            lineTo(657f, 589f)
            close()
        }
    }
    val letterA: Path by lazy {
        Path().apply {
            fillType = PathFillType.EvenOdd
            moveTo(872f, 562f)
            quadraticBezierTo(905f, 543f, 950f, 543f)
            quadraticBezierTo(1000f, 543f, 1000f, 595f)
            lineTo(1000f, 690f)
            quadraticBezierTo(1000f, 698f, 992f, 698f)
            lineTo(962f, 698f)
            quadraticBezierTo(954f, 698f, 952f, 690f)
            lineTo(950f, 682f)
            quadraticBezierTo(925f, 702f, 895f, 702f)
            quadraticBezierTo(852f, 700f, 852f, 655f)
            quadraticBezierTo(852f, 610f, 905f, 603f)
            lineTo(950f, 598f)
            quadraticBezierTo(950f, 580f, 920f, 580f)
            quadraticBezierTo(895f, 580f, 880f, 592f)
            quadraticBezierTo(866f, 592f, 872f, 562f)
            close()
            moveTo(898f, 648f)
            quadraticBezierTo(898f, 632f, 925f, 630f)
            lineTo(950f, 630f)
            lineTo(950f, 655f)
            quadraticBezierTo(935f, 668f, 915f, 668f)
            quadraticBezierTo(898f, 668f, 898f, 648f)
            close()
        }
    }
    val letterY: Path by lazy {
        Path().apply {
            moveTo(1005f, 550f)
            quadraticBezierTo(1005f, 543f, 1013f, 543f)
            lineTo(1040f, 543f)
            quadraticBezierTo(1047f, 543f, 1050f, 550f)
            lineTo(1078f, 625f)
            lineTo(1105f, 553f)
            quadraticBezierTo(1108f, 543f, 1118f, 543f)
            lineTo(1143f, 543f)
            quadraticBezierTo(1153f, 543f, 1150f, 553f)
            lineTo(1090f, 712f)
            quadraticBezierTo(1070f, 760f, 1030f, 763f)
            quadraticBezierTo(1000f, 763f, 996f, 755f)
            lineTo(1000f, 735f)
            quadraticBezierTo(1000f, 727f, 1010f, 727f)
            quadraticBezierTo(1030f, 727f, 1042f, 700f)
            lineTo(1046f, 690f)
            lineTo(1007f, 560f)
            close()
        }
    }
}
