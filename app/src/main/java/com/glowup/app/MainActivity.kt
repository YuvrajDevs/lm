package com.glowup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlowUpTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable("home") { HomeScreen(navController) }
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
}
