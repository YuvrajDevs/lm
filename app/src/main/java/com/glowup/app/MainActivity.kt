package com.glowup.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.glowup.app.notify.Notifications
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.screens.focus.FocusScreen
import com.glowup.app.ui.screens.learn.LearnScreen
import com.glowup.app.ui.screens.learn.SessionScreen
import com.glowup.app.ui.screens.morning.MorningSessionScreen
import com.glowup.app.ui.screens.onboarding.OnboardingScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.glowup.app.ui.screens.addroutine.AddRoutineScreen
import com.glowup.app.ui.screens.communication.CommunicationScreen
import com.glowup.app.ui.screens.confidence.ConfidenceScreen
import com.glowup.app.ui.screens.home.HomeScreen
import com.glowup.app.ui.screens.lesson.LessonDetailScreen
import com.glowup.app.ui.screens.looks.GroomingDetailScreen
import com.glowup.app.ui.screens.looks.LooksScreen
import com.glowup.app.ui.screens.mood.MoodCheckinScreen
import com.glowup.app.ui.screens.morning.MorningScreen
import com.glowup.app.ui.screens.notes.QuotesNotesScreen
import com.glowup.app.ui.screens.notifications.NotificationsScreen
import com.glowup.app.ui.screens.routines.RoutineDetailScreen
import com.glowup.app.ui.screens.routines.RoutinesScreen
import com.glowup.app.ui.screens.settings.SettingsScreen
import com.glowup.app.ui.screens.sleep.SleepScreen
import com.glowup.app.ui.screens.speaking.SpeakingPracticeScreen
import com.glowup.app.ui.screens.streak.StreakDetailScreen
import com.glowup.app.ui.screens.styleguide.StyleGuideDetailScreen
import com.glowup.app.ui.screens.styling.StylingScreen
import com.glowup.app.ui.screens.stylist.AiStylistScreen
import com.glowup.app.ui.screens.todo.TodoScreen
import com.glowup.app.ui.screens.wardrobe.WardrobeScreen
import com.glowup.app.ui.theme.GlowUpTheme

class MainActivity : ComponentActivity() {
    private val pendingRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) pendingRoute.value = intent.getStringExtra(Notifications.EXTRA_ROUTE)
        setContent {
            GlowUpTheme {
                val navController = rememberNavController()
                val route = pendingRoute.value
                LaunchedEffect(route) {
                    if (route != null && ProfileStore.onboarded) navController.navigate(route) { launchSingleTop = true }
                    pendingRoute.value = null
                }
                NavHost(
                    navController = navController,
                    startDestination = if (ProfileStore.onboarded) "home" else "onboarding",
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable("onboarding") { OnboardingScreen(navController) }
                    composable("home") { HomeScreen(navController) }
                    composable("learn") { LearnScreen(navController) }
                    composable("focus") { FocusScreen(navController) }
                    composable(
                        "session/{index}",
                        arguments = listOf(navArgument("index") { type = NavType.IntType }),
                    ) { backStackEntry ->
                        SessionScreen(navController, backStackEntry.arguments?.getInt("index") ?: 0)
                    }
                    composable(
                        "morning_session/{index}",
                        arguments = listOf(navArgument("index") { type = NavType.IntType }),
                    ) { backStackEntry ->
                        MorningSessionScreen(navController, backStackEntry.arguments?.getInt("index") ?: 0)
                    }
                    composable("routines") { RoutinesScreen(navController) }
                    composable("styling") { StylingScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }
                    composable("sleep") { SleepScreen(navController) }
                    composable("confidence") { ConfidenceScreen(navController) }
                    composable("communication") { CommunicationScreen(navController) }
                    composable("todo") { TodoScreen(navController) }
                    composable("quotes_notes") { QuotesNotesScreen(navController) }
                    composable("notifications") { NotificationsScreen(navController) }
                    composable("wardrobe") { WardrobeScreen(navController) }
                    composable("mood_checkin") { MoodCheckinScreen(navController) }
                    composable("streak_detail") { StreakDetailScreen(navController) }
                    composable("add_routine") { AddRoutineScreen(navController) }
                    composable("ai_stylist") { AiStylistScreen(navController) }
                    composable("morning") { MorningScreen(navController) }
                    composable("speaking_practice") { SpeakingPracticeScreen(navController) }
                    composable(
                        "speaking_practice/{topic}",
                        arguments = listOf(navArgument("topic") { type = NavType.StringType }),
                    ) { backStackEntry ->
                        val encoded = backStackEntry.arguments?.getString("topic")
                        val decoded = encoded?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                        SpeakingPracticeScreen(navController, decoded)
                    }
                    composable(
                        "lesson_detail/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType }),
                    ) { backStackEntry ->
                        LessonDetailScreen(navController, backStackEntry.arguments?.getInt("id") ?: 0)
                    }
                    composable("looks") { LooksScreen(navController) }
                    composable(
                        "grooming_detail/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType }),
                    ) { backStackEntry ->
                        GroomingDetailScreen(navController, backStackEntry.arguments?.getInt("id") ?: 0)
                    }
                    composable(
                        "style_guide_detail/{title}",
                        arguments = listOf(navArgument("title") { type = NavType.StringType }),
                    ) { backStackEntry ->
                        StyleGuideDetailScreen(navController, backStackEntry.arguments?.getString("title") ?: "Old Money")
                    }
                    composable(
                        "routine_detail/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType }),
                    ) { backStackEntry ->
                        RoutineDetailScreen(navController, backStackEntry.arguments?.getInt("id") ?: 0)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(Notifications.EXTRA_ROUTE)?.let { pendingRoute.value = it }
    }
}
