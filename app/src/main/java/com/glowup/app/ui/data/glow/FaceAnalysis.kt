package com.glowup.app.ui.data.glow

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class Pt(val x: Float, val y: Float)

/**
 * The face outline and features from the camera's face detector, in image pixels. [oval] runs
 * clockwise from the top centre (36 points from ML Kit's face contour).
 */
data class FaceGeometry(
    val oval: List<Pt>,
    val leftEye: List<Pt>,
    val rightEye: List<Pt>,
    val leftBrow: List<Pt>,
    val rightBrow: List<Pt>,
    val noseBridge: List<Pt>,
    val noseBottom: List<Pt>,
    val upperLip: List<Pt>
)

/** Measurements only — never the image. All of these are approximate and explained as such. */
data class FaceMetrics(
    val shape: String,
    val lengthToWidth: Double,
    val foreheadToCheek: Double,
    val jawToCheek: Double,
    /** 0–100. Real faces typically land around 88–97; nobody is perfectly symmetrical. */
    val symmetry: Int,
    /** Lower face (nose to chin) relative to mid face (eyes to nose). Fixed trait, used for beard advice. */
    val lowerFaceRatio: Double
)

object FaceAnalysis {
    /** Average of several good frames, so one bad frame can't skew the result. */
    fun average(frames: List<FaceMetrics>): FaceMetrics? {
        if (frames.isEmpty()) return null
        val lw = frames.map { it.lengthToWidth }.average()
        val fc = frames.map { it.foreheadToCheek }.average()
        val jc = frames.map { it.jawToCheek }.average()
        return FaceMetrics(
            shape = classify(lw, fc, jc),
            lengthToWidth = lw,
            foreheadToCheek = fc,
            jawToCheek = jc,
            symmetry = frames.map { it.symmetry }.average().roundToInt(),
            lowerFaceRatio = frames.map { it.lowerFaceRatio }.average()
        )
    }

    fun measure(g: FaceGeometry): FaceMetrics? {
        if (g.oval.size < 20 || g.leftEye.isEmpty() || g.rightEye.isEmpty()) return null
        // Undo head roll so a slightly tilted head doesn't read as asymmetry.
        val le = centre(g.leftEye)
        val re = centre(g.rightEye)
        val angle = atan2((re.y - le.y).toDouble(), (re.x - le.x).toDouble())
        val pivot = centre(g.oval)
        fun r(p: Pt): Pt {
            val c = cos(-angle)
            val s = sin(-angle)
            val dx = p.x - pivot.x
            val dy = p.y - pivot.y
            return Pt((pivot.x + dx * c - dy * s).toFloat(), (pivot.y + dx * s + dy * c).toFloat())
        }
        val oval = g.oval.map(::r)
        val top = oval.minOf { it.y }
        val chin = oval.maxOf { it.y }
        val length = chin - top
        if (length <= 0f) return null

        val forehead = widthAt(oval, top + length * 0.18f)
        val cheek = (30..60 step 5).maxOf { widthAt(oval, top + length * it / 100f) }
        val jaw = widthAt(oval, top + length * 0.82f)
        if (cheek <= 0f) return null
        val lw = length / cheek.toDouble()
        val fc = forehead / cheek.toDouble()
        val jc = jaw / cheek.toDouble()

        val mid = (g.noseBridge.takeIf { it.isNotEmpty() }?.map(::r)?.let { centre(it).x } ?: ((r(le).x + r(re).x) / 2))
        val pairs = mutableListOf<Double>()
        fun compare(a: Pt, b: Pt) {
            pairs += abs(abs(a.x - mid) - abs(b.x - mid)).toDouble()
            pairs += abs(a.y - b.y).toDouble()
        }
        compare(centre(g.leftEye.map(::r)), centre(g.rightEye.map(::r)))
        if (g.leftBrow.isNotEmpty() && g.rightBrow.isNotEmpty()) compare(centre(g.leftBrow.map(::r)), centre(g.rightBrow.map(::r)))
        if (g.upperLip.size >= 2) {
            val lip = g.upperLip.map(::r)
            compare(lip.minBy { it.x }, lip.maxBy { it.x })
        }
        // Jaw and cheek outline: compare each point with its mirror on the other side.
        val n = oval.size
        for (i in 4 until n / 2 - 2) compare(oval[i], oval[n - i])
        val asymmetry = pairs.average() / cheek
        val symmetry = (100 - asymmetry * 100 * 4).coerceIn(50.0, 100.0).roundToInt()

        val eyeLine = (r(le).y + r(re).y) / 2
        val noseY = g.noseBottom.takeIf { it.isNotEmpty() }?.map(::r)?.maxOf { it.y } ?: (eyeLine + (chin - eyeLine) * 0.45f)
        val lower = (chin - noseY) / (noseY - eyeLine).coerceAtLeast(1f).toDouble()

        return FaceMetrics(classify(lw, fc, jc), lw, fc, jc, symmetry, lower)
    }

    /**
     * Face shape from proportions. The detector's outline starts around the upper forehead rather
     * than the hairline, so length ratios run a little shorter than tape-measure rules of thumb.
     */
    fun classify(lengthToWidth: Double, foreheadToCheek: Double, jawToCheek: Double): String = when {
        lengthToWidth >= 1.42 -> "Long"
        // Short, wide faces: a strong jaw makes it square, a softer one round.
        lengthToWidth < 1.18 && jawToCheek >= 0.90 -> "Square"
        lengthToWidth < 1.18 -> "Round"
        foreheadToCheek < 0.86 && jawToCheek < 0.86 -> "Diamond"
        foreheadToCheek >= 0.95 && jawToCheek < 0.80 -> "Heart"
        else -> "Oval"
    }

    private fun centre(points: List<Pt>) = Pt(points.map { it.x }.average().toFloat(), points.map { it.y }.average().toFloat())

    /** Width of the closed outline [poly] along the horizontal line y = [y]. */
    fun widthAt(poly: List<Pt>, y: Float): Float {
        val xs = mutableListOf<Float>()
        for (i in poly.indices) {
            val a = poly[i]
            val b = poly[(i + 1) % poly.size]
            if ((a.y <= y && b.y > y) || (b.y <= y && a.y > y)) {
                xs += a.x + (y - a.y) / (b.y - a.y) * (b.x - a.x)
            }
        }
        return if (xs.size >= 2) xs.max() - xs.min() else 0f
    }
}

enum class PostureLevel(val label: String) { GOOD("Upright"), MILD("Slightly forward"), FORWARD("Forward head") }

object PostureAnalysis {
    /**
     * Side-on angle of the ear above the shoulder, in degrees: 90° means the ear is straight above
     * the shoulder; the further the head drifts forward, the smaller the angle.
     */
    fun neckAngle(ear: Pt, shoulder: Pt): Int? {
        val rise = shoulder.y - ear.y
        if (rise <= 0f) return null
        val run = abs(ear.x - shoulder.x)
        return Math.toDegrees(atan2(rise.toDouble(), run.toDouble())).roundToInt()
    }

    fun level(angle: Int): PostureLevel = when {
        angle >= 75 -> PostureLevel.GOOD
        angle >= 62 -> PostureLevel.MILD
        else -> PostureLevel.FORWARD
    }

    fun distance(a: Pt, b: Pt) = sqrt(((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)).toDouble())
}
