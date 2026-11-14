package com.glowup.app.ui.flows

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.setRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingFlowTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun newUserCompletesOnboardingAndLandsOnGlowCheck() {
        val nav = compose.setRoute("onboarding", onboarded = false)

        compose.onNodeWithText("Welcome to Re:MakeU").assertExists()
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.onNode(hasSetTextAction()).performTextInput("Alex Doe")
        compose.onNodeWithText("Continue").assertIsEnabled().performClick()

        compose.onNodeWithText("How old are you?").assertExists()
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.onNodeWithText("18–24").performClick()
        compose.onNodeWithText("Continue").performClick()

        compose.onNodeWithText("What do you want to work on?").assertExists()
        compose.onNodeWithText("Fitness").performClick()
        compose.onNodeWithText("Sleep").performClick()
        compose.onNodeWithText("Continue").performClick()

        compose.onNodeWithText("Stay on track, Alex").assertExists()
        compose.onNodeWithText("Not now").performClick()
        compose.waitForIdle()

        assertTrue(ProfileStore.onboarded)
        assertEquals("Alex Doe", ProfileStore.name)
        assertEquals("18–24", ProfileStore.ageBand)
        assertEquals(listOf("Sleep", "Fitness"), ProfileStore.focusAreas.toList())
        // Home opens the personalised check once, straight after onboarding.
        assertEquals("glow_check", nav().currentDestination?.route)
    }

    @Test
    fun backReturnsToThePreviousStepKeepingAnswers() {
        compose.setRoute("onboarding", onboarded = false)
        compose.onNode(hasSetTextAction()).performTextInput("Sam")
        compose.onNodeWithText("Continue").performClick()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Sam").assertExists()
    }
}
