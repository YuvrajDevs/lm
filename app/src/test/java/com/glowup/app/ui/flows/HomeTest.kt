package com.glowup.app.ui.flows

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glowup.app.ui.data.RoutineSession
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.TodoBucket
import com.glowup.app.ui.data.TodoStore
import com.glowup.app.ui.setRoute
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun greetsByFirstNameAndShowsCheckIns() {
        compose.setRoute("home")
        compose.onNodeWithText("Hey, Sam").assertExists()
        compose.onNode(hasText("Mood") and hasText("Check in")).assertExists()
        compose.onNodeWithText("Workout").assertExists()
        compose.onNodeWithContentDescription("Home").assertIsSelected()
    }

    @Test
    fun bottomNavOpensEachTab() {
        val nav = compose.setRoute("home")
        compose.onNodeWithContentDescription("Learn").performClick()
        assertEquals("learn", nav().currentDestination?.route)
        compose.onNodeWithContentDescription("Profile").performClick()
        assertEquals("settings", nav().currentDestination?.route)
    }

    @Test
    fun checkInChipOpensItsScreen() {
        val nav = compose.setRoute("home")
        // The check-in chip, not the Mood shortcut in the top row.
        compose.onNode(hasText("Mood") and hasText("Check in")).performClick()
        assertEquals("mood_checkin", nav().currentDestination?.route)
    }

    @Test
    fun tappingAPlanItemTicksItOff() {
        compose.setRoute("home")
        val item = TodoStore.items.first { it.bucket == TodoBucket.TODAY }
        compose.onNodeWithTag("home_feed").performScrollToNode(hasText(item.title))
        compose.onNodeWithText(item.title).performClick()
        assertTrue(TodoStore.items.first { it.id == item.id }.done)
    }

    @Test
    fun routineWithItsOwnPageOpensItThere() {
        val nav = compose.setRoute("home")
        val skincare = RoutineStore.items.first { it.title.startsWith("Skincare") }
        compose.onNodeWithTag("home_feed").performScrollToNode(hasText(skincare.title))
        compose.onNodeWithText(skincare.title).performClick()
        assertEquals(skincare.id, RoutineSession.activeId)
        assertTrue(nav().currentDestination?.route != "home")
        // Opening the page doesn't tick it; the user finishes it there.
        assertTrue(!skincare.isDoneOn(LocalDate.now()))
    }
}
