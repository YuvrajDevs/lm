package com.glowup.app.ui.data

import com.glowup.app.ui.data.glow.FaceAnalysis
import com.glowup.app.ui.data.glow.FaceGeometry
import com.glowup.app.ui.data.glow.FaceShapeGuide
import com.glowup.app.ui.data.glow.FocusArea
import com.glowup.app.ui.data.glow.GlowCheckStore
import com.glowup.app.ui.data.glow.GlowQuestions
import com.glowup.app.ui.data.glow.GlowScoring
import com.glowup.app.ui.data.glow.LooksRoutines
import com.glowup.app.ui.data.glow.PostureAnalysis
import com.glowup.app.ui.data.glow.PostureLevel
import com.glowup.app.ui.data.glow.Pt
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GlowCheckTest {
    private val today = LocalDate.of(2026, 7, 1)

    @Before
    fun setUp() {
        freshStores()
    }

    /** A synthetic face outline: half-width varies with height to mimic forehead, cheeks and jaw. */
    private fun face(length: Float, forehead: Float, cheek: Float, jaw: Float, skew: Float = 0f, roll: Double = 0.0): FaceGeometry {
        val n = 36
        val cx = 500f
        val top = 200f
        fun halfWidth(f: Float): Float = when {
            f < 0.18f -> forehead / 2 * (0.6f + f / 0.18f * 0.4f)
            f < 0.45f -> forehead / 2 + (cheek / 2 - forehead / 2) * ((f - 0.18f) / 0.27f)
            f < 0.82f -> cheek / 2 + (jaw / 2 - cheek / 2) * ((f - 0.45f) / 0.37f)
            else -> jaw / 2 * (1f - (f - 0.82f) / 0.18f * 0.85f)
        }
        val pts = (0 until n).map { i ->
            // 0 = top centre, clockwise: right side going down, then left side going up.
            val t = if (i <= n / 2) i / (n / 2f) else (n - i) / (n / 2f)
            val y = top + t * length
            val side = if (i <= n / 2) 1 else -1
            val hw = if (i == 0 || i == n / 2) 0f else halfWidth(t) * (if (side < 0) 1f + skew else 1f)
            Pt(cx + side * hw, y)
        }
        fun rot(p: Pt): Pt {
            val c = cos(roll)
            val s = sin(roll)
            val dx = p.x - cx
            val dy = p.y - 500f
            return Pt((cx + dx * c - dy * s).toFloat(), (500f + dx * s + dy * c).toFloat())
        }
        val eyeY = top + length * 0.42f
        val eye = { x: Float -> List(8) { k -> Pt(x + 15 * cos(k * 0.785).toFloat(), eyeY + 6 * sin(k * 0.785).toFloat()) } }
        return FaceGeometry(
            oval = pts.map(::rot),
            leftEye = eye(cx + cheek * 0.2f).map(::rot),
            rightEye = eye(cx - cheek * 0.2f).map(::rot),
            leftBrow = listOf(Pt(cx + 40, eyeY - 30), Pt(cx + 90, eyeY - 32)).map(::rot),
            rightBrow = listOf(Pt(cx - 40, eyeY - 30), Pt(cx - 90, eyeY - 32)).map(::rot),
            noseBridge = listOf(Pt(cx, eyeY), Pt(cx, eyeY + 60)).map(::rot),
            noseBottom = listOf(Pt(cx - 20, top + length * 0.66f), Pt(cx, top + length * 0.68f), Pt(cx + 20, top + length * 0.66f)).map(::rot),
            upperLip = listOf(Pt(cx - 50, top + length * 0.78f), Pt(cx, top + length * 0.76f), Pt(cx + 50, top + length * 0.78f)).map(::rot)
        )
    }

    @Test
    fun classifiesFaceShapes() {
        assertEquals("Long", FaceAnalysis.measure(face(500f, 300f, 320f, 270f))!!.shape)
        assertEquals("Square", FaceAnalysis.measure(face(350f, 310f, 330f, 320f))!!.shape)
        assertEquals("Round", FaceAnalysis.measure(face(360f, 260f, 330f, 250f))!!.shape)
        assertEquals("Oval", FaceAnalysis.measure(face(420f, 290f, 320f, 260f))!!.shape)
        assertEquals("Heart", FaceAnalysis.classify(1.3, 0.97, 0.75))
        assertEquals("Diamond", FaceAnalysis.classify(1.3, 0.8, 0.8))
    }

    @Test
    fun symmetryIsHighForBalancedFacesAndIgnoresHeadTilt() {
        val straight = FaceAnalysis.measure(face(420f, 290f, 320f, 260f))!!
        val tilted = FaceAnalysis.measure(face(420f, 290f, 320f, 260f, roll = 0.12))!!
        val uneven = FaceAnalysis.measure(face(420f, 290f, 320f, 260f, skew = 0.12f))!!
        assertTrue("straight ${straight.symmetry}", straight.symmetry >= 95)
        assertTrue("tilt shouldn't read as asymmetry: ${tilted.symmetry}", tilted.symmetry >= 93)
        assertTrue("uneven ${uneven.symmetry}", uneven.symmetry < straight.symmetry - 3)
        val avg = FaceAnalysis.average(listOf(straight, uneven))!!
        assertEquals((straight.symmetry + uneven.symmetry) / 2.0, avg.symmetry.toDouble(), 1.0)
        assertNull(FaceAnalysis.average(emptyList()))
    }

    @Test
    fun neckAngle() {
        assertEquals(90, PostureAnalysis.neckAngle(Pt(100f, 100f), Pt(100f, 250f)))
        val forward = PostureAnalysis.neckAngle(Pt(160f, 100f), Pt(100f, 200f))!!
        assertEquals(PostureLevel.FORWARD, PostureAnalysis.level(forward))
        assertNull(PostureAnalysis.neckAngle(Pt(0f, 300f), Pt(0f, 200f)))
    }

    private val answers = mapOf(
        "skincare" to "Nothing",
        "breakouts" to "Sometimes",
        "haircut" to "Every 3–5 weeks",
        "sleep" to "7–8 hours",
        "exercise" to "0",
        "screen" to "9+ hours",
        "style" to "Very"
    )

    @Test
    fun ranksChangeableAreasAndFindsStrengths() {
        ProfileStore.updateAgeBand("25–34")
        val r = GlowCheckStore.build(setOf("Face & jawline"), 20, answers, null, "Oval", 58, 178, 85.0, 98.0, today)
        val focus = r.focus.map { it.area }
        assertEquals(listOf(FocusArea.POSTURE, FocusArea.BODY, FocusArea.SKIN), focus)
        assertTrue(r.strengths.isNotEmpty())
        assertTrue(r.areas.none { it.reason.contains("ugly", ignoreCase = true) })
        val plan = GlowScoring.plan(r, false)
        assertEquals("posture", plan.programmeId)
        assertTrue(plan.routines.any { it.fromRoutine == "skin-basics" })
        assertTrue(plan.routines.size <= 4)
    }

    @Test
    fun minorsGetNoBodyFatFraming() {
        ProfileStore.updateAgeBand("16–17")
        val r = GlowCheckStore.build(setOf("Body"), 10, answers, null, null, null, 175, 95.0, 100.0, today)
        val body = r.areas.first { it.area == FocusArea.BODY }
        assertTrue(!body.reason.contains("fat", ignoreCase = true))
        assertEquals("first30", GlowScoring.plan(r, true).programmeId)
    }

    @Test
    fun savesComparesAndPersists() {
        ProfileStore.updateAgeBand("25–34")
        val first = GlowCheckStore.build(setOf("Posture"), 10, answers, null, null, 60, 178, 85.0, 98.0, today)
        GlowCheckStore.save(first)
        assertEquals(GlowCheckStore.RESCAN_DAYS, GlowCheckStore.daysUntilRescan(today))
        val second = GlowCheckStore.build(
            setOf(
                "Posture"
            ),
            10, answers + ("skincare" to "Wash, moisturiser & SPF"), null, null, 70, 178, 83.0, 95.0, today.plusDays(30)
        )
        GlowCheckStore.save(second)
        val lines = GlowScoring.changes(first, second)
        assertTrue(lines.any { it.startsWith("Posture improved by 10°") })
        assertTrue(lines.any { it.startsWith("Waist down 3 cm") })
        assertTrue(lines.any { it.startsWith("Skin") })
        Stores.loadAll()
        assertEquals(2, GlowCheckStore.results.size)
        assertEquals(70, GlowCheckStore.latest!!.neckAngle)
        assertNotNull(GlowCheckStore.previous)
    }

    @Test
    fun contentIsComplete() {
        listOf("Oval", "Round", "Square", "Heart", "Long", "Diamond").forEach { assertNotNull(it, FaceShapeGuide.forShape(it)) }
        assertTrue(LooksRoutines.all.size >= 12)
        assertTrue(LooksRoutines.all.all { it.steps.size >= 3 })
        assertTrue(GlowQuestions.all.size >= 8)
    }
}
