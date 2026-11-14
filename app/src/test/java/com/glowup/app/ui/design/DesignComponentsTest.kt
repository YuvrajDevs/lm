package com.glowup.app.ui.design

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glowup.app.ui.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DesignComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun primaryButtonClicksOnlyWhenEnabled() {
        var clicks = 0
        var enabled by mutableStateOf(false)
        compose.setThemedContent { PrimaryButton("Continue", enabled = enabled) { clicks++ } }

        compose.onNodeWithText("Continue").assertIsNotEnabled().performClick()
        assertEquals(0, clicks)

        enabled = true
        compose.onNodeWithText("Continue").assertIsEnabled().performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun buttonsShareOneHeight() {
        compose.setThemedContent {
            androidx.compose.foundation.layout.Column {
                PrimaryButton("Primary") {}
                SecondaryButton("Secondary") {}
            }
        }
        compose.onNodeWithText("Primary").assertTouchHeightIsEqualTo(Sizes.Button)
        compose.onNodeWithText("Secondary").assertTouchHeightIsEqualTo(Sizes.Button)
    }

    @Test
    fun chipReportsSelectionAndHasFullTouchTarget() {
        compose.setThemedContent {
            var selected by remember { mutableStateOf(false) }
            Chip("Skin", selected) { selected = !selected }
        }
        compose.onNodeWithText("Skin").assertIsNotSelected().performClick()
        compose.onNodeWithText("Skin").assertIsSelected().assertTouchHeightIsEqualTo(48.dp)
    }

    @Test
    fun switchIsLabelledAndToggles() {
        compose.setThemedContent {
            var on by remember { mutableStateOf(false) }
            AppSwitch(on, label = "Bedtime reminder") { on = it }
        }
        compose.onNodeWithContentDescription("Bedtime reminder").assertIsOff().performClick()
        compose.onNodeWithContentDescription("Bedtime reminder").assertIsOn()
    }

    @Test
    fun sectionHeaderActionFires() {
        var tapped = false
        compose.setThemedContent { SectionHeader("Routines", subtitle = "0 of 5 done", action = "See all", onAction = { tapped = true }) }
        compose.onNodeWithText("0 of 5 done").assertExists()
        compose.onNodeWithText("See all").performClick()
        assertEquals(true, tapped)
    }
}
