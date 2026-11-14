package com.glowup.app.ui.flows

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glowup.app.ui.setRoute
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun showsProfileAndMembership() {
        compose.setRoute("settings")
        compose.onNodeWithText("Level 1 · 0 points").assertExists()
        compose.onNodeWithText("18–24").assertExists()
        compose.onNodeWithTag("settings_list").performScrollToNode(hasText("MEMBERSHIP"))
    }

    @Test
    fun yourDataIsReachable() {
        val nav = compose.setRoute("settings")
        compose.onNodeWithTag("settings_list").performScrollToNode(hasText("Your data"))
        compose.onNodeWithText("Your data").performClick()
        assertEquals("your_data", nav().currentDestination?.route)
    }
}
