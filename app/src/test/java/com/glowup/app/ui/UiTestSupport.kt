package com.glowup.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.freshStores
import com.glowup.app.ui.navigation.AppNavGraph
import com.glowup.app.ui.theme.ReMakeUTheme

/** Fresh in-memory stores, then [content] inside the app theme. */
fun ComposeContentTestRule.setThemedContent(content: @Composable () -> Unit) {
    freshStores()
    setContent { ReMakeUTheme { content() } }
}

/** A signed-in user who has finished onboarding. */
fun onboardedUser(name: String = "Sam") {
    ProfileStore.completeOnboarding(name, focus = listOf("Looks", "Fitness"), faceShape = null, ageBand = "18–24")
}

/**
 * Renders the real navigation graph and opens [route]. Routes with arguments are reached by
 * navigating from Home, exactly as the app does. Returns the controller so tests can assert on it.
 */
fun ComposeContentTestRule.setRoute(route: String, onboarded: Boolean = true): () -> NavHostController {
    freshStores()
    if (onboarded) onboardedUser()
    lateinit var controller: NavHostController
    setContent {
        ReMakeUTheme {
            controller = rememberNavController()
            val start = if (onboarded) "home" else "onboarding"
            AppNavGraph(controller, startDestination = start)
            LaunchedEffect(route) { if (route != start) controller.navigate(route) }
        }
    }
    waitForIdle()
    return { controller }
}
