package com.glowup.app.ui.flows

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glowup.app.ui.setRoute
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GlowCheckFlowTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun everyOptionalStepCanBeSkipped() {
        compose.setRoute("glow_check")
        compose.onNodeWithText("Your Glow Check").assertExists()
        compose.onNodeWithText("Our promise").assertExists()
        compose.onNodeWithText("Let's go").performClick()

        compose.onNodeWithText("What do you want to improve?").assertExists()
        compose.onNodeWithText("Continue").performClick()

        compose.onNodeWithText("Face scan").assertExists()
        compose.onNodeWithText("Skip").performClick()

        compose.onNodeWithText("Posture scan").assertExists()
        compose.onNodeWithText("FOR THE BEST READ").assertExists()
        compose.onNodeWithText("Skip").performClick()

        compose.onNodeWithText("Body").assertExists()
        compose.onNodeWithText("WHY IT HELPS").assertExists()
        compose.onNodeWithText("Skip").performClick()

        compose.onNodeWithText("Quick questions").assertExists()
    }

    @Test
    fun backStepsBackwards() {
        compose.setRoute("glow_check")
        compose.onNodeWithText("Let's go").performClick()
        compose.onNodeWithText("Continue").performClick()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("What do you want to improve?").assertExists()
    }
}
