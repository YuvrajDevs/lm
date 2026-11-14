package com.glowup.app.ui.screens

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.glowup.app.ui.data.GroomingStore
import com.glowup.app.ui.data.LessonStore
import com.glowup.app.ui.data.Scenarios
import com.glowup.app.ui.data.fitness.ExerciseLibrary
import com.glowup.app.ui.data.fitness.Programmes
import com.glowup.app.ui.data.fitness.QuickFocus
import com.glowup.app.ui.data.fitness.QuickSessions
import com.glowup.app.ui.data.glow.LooksRoutines
import com.glowup.app.ui.data.style.StyleLibrary
import com.glowup.app.ui.setRoute
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Opens every route in the real navigation graph and checks that it renders, that every tappable
 * element has a label for screen readers and a comfortable touch target, and records a screenshot.
 *
 * Record reference screenshots with `./gradlew recordRoborazziDebug`; check them with
 * `./gradlew verifyRoborazziDebug`.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AllScreensTest(private val route: String) {
    @get:Rule val compose = createComposeRule()

    @Test
    fun rendersWithAccessibleControls() {
        compose.setRoute(route)
        compose.onRoot().captureRoboImage("src/test/screenshots/${route.replace('/', '_').replace('.', '-')}.png")

        val unlabeled = mutableListOf<String>()
        val small = mutableListOf<String>()
        compose.onAllNodes(hasClickAction() and isEnabled()).fetchSemanticsNodes().forEach { node ->
            val config = node.config
            val label = config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }
                ?: config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
            val describe = label ?: node.boundsInRoot.toString()
            if (label.isNullOrBlank()) unlabeled += describe
            // Measure the element itself, not what's left of it after scrolling clips it.
            val visible = node.boundsInRoot
            val fullyOnScreen = abs(visible.width - node.size.width) <= 1f && abs(visible.height - node.size.height) <= 1f
            val touch = node.touchBoundsInRoot
            with(compose.density) {
                if (fullyOnScreen && (touch.width.toDp() < MinTouch || touch.height.toDp() < MinTouch)) {
                    small += "$describe (${touch.width.toDp()}×${touch.height.toDp()})"
                }
            }
        }
        assertTrue("[$route] tappable elements without a label: $unlabeled", unlabeled.isEmpty())
        assertTrue("[$route] touch targets under $MinTouch: $small", small.isEmpty())
    }

    companion object {
        /** Material's minimum touch target. Compose extends small elements' touch area up to this. */
        private val MinTouch = 48.dp

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun routes(): List<String> = listOf(
            "home", "learn", "focus", "routines", "styling", "settings", "sleep", "confidence", "communication",
            "todo", "quotes_notes", "notifications", "wardrobe", "mood_checkin", "streak_detail", "mood_insights",
            "sleep_log", "fitness", "fitness_setup", "exercises", "body_checkin", "glow_check", "glow_results",
            "face_shape", "glow_routines", "progress_photos", "blocker", "capsule", "packing", "scenarios",
            "speech_history", "your_data", "add_routine", "morning", "speaking_practice", "looks",
            "session/0", "morning_session/0", "routine_detail/1",
            "programme/${Programmes.all.first().id}",
            "exercise/${ExerciseLibrary.all.first().id}",
            "workout/${QuickSessions.key(QuickFocus.entries.first(), 10)}",
            "glow_routine/${LooksRoutines.all.first().id}",
            "scenario/${Scenarios.all.first().id}",
            "lesson_detail/${LessonStore.lessons.first().id}",
            "grooming_detail/${GroomingStore.lessons.first().id}",
            "style_guide_detail/${StyleLibrary.guides.first().key}",
            "outfit/${StyleLibrary.outfits.first().id}"
        )
    }
}
