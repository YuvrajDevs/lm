package com.glowup.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.glowup.app.ui.data.Scenarios
import com.glowup.app.ui.screens.addroutine.AddRoutineScreen
import com.glowup.app.ui.screens.communication.CommunicationScreen
import com.glowup.app.ui.screens.confidence.ConfidenceScreen
import com.glowup.app.ui.screens.data.YourDataScreen
import com.glowup.app.ui.screens.fitness.BodyCheckInScreen
import com.glowup.app.ui.screens.fitness.ExerciseDetailScreen
import com.glowup.app.ui.screens.fitness.ExerciseLibraryScreen
import com.glowup.app.ui.screens.fitness.FitnessScreen
import com.glowup.app.ui.screens.fitness.FitnessSetupScreen
import com.glowup.app.ui.screens.fitness.ProgrammeScreen
import com.glowup.app.ui.screens.fitness.WorkoutPlayerScreen
import com.glowup.app.ui.screens.fitness.WorkoutPreviewScreen
import com.glowup.app.ui.screens.focus.BlockerScreen
import com.glowup.app.ui.screens.focus.FocusScreen
import com.glowup.app.ui.screens.glow.FaceShapeScreen
import com.glowup.app.ui.screens.glow.GlowCheckScreen
import com.glowup.app.ui.screens.glow.GlowResultsScreen
import com.glowup.app.ui.screens.glow.LooksRoutineDetailScreen
import com.glowup.app.ui.screens.glow.LooksRoutinesScreen
import com.glowup.app.ui.screens.glow.ProgressPhotosScreen
import com.glowup.app.ui.screens.home.HomeScreen
import com.glowup.app.ui.screens.learn.LearnScreen
import com.glowup.app.ui.screens.learn.SessionScreen
import com.glowup.app.ui.screens.lesson.LessonDetailScreen
import com.glowup.app.ui.screens.looks.GroomingDetailScreen
import com.glowup.app.ui.screens.looks.LooksScreen
import com.glowup.app.ui.screens.mood.MoodCheckinScreen
import com.glowup.app.ui.screens.mood.MoodInsightsScreen
import com.glowup.app.ui.screens.morning.MorningScreen
import com.glowup.app.ui.screens.morning.MorningSessionScreen
import com.glowup.app.ui.screens.notes.QuotesNotesScreen
import com.glowup.app.ui.screens.notifications.NotificationsScreen
import com.glowup.app.ui.screens.onboarding.OnboardingScreen
import com.glowup.app.ui.screens.routines.RoutineDetailScreen
import com.glowup.app.ui.screens.routines.RoutinesScreen
import com.glowup.app.ui.screens.settings.SettingsScreen
import com.glowup.app.ui.screens.sleep.SleepLogScreen
import com.glowup.app.ui.screens.sleep.SleepScreen
import com.glowup.app.ui.screens.speaking.ScenariosScreen
import com.glowup.app.ui.screens.speaking.SpeakingPracticeScreen
import com.glowup.app.ui.screens.speaking.SpeechHistoryScreen
import com.glowup.app.ui.screens.streak.StreakDetailScreen
import com.glowup.app.ui.screens.styleguide.OutfitDetailScreen
import com.glowup.app.ui.screens.styleguide.StyleGuideDetailScreen
import com.glowup.app.ui.screens.styling.StylingScreen
import com.glowup.app.ui.screens.todo.TodoScreen
import com.glowup.app.ui.screens.wardrobe.CapsuleScreen
import com.glowup.app.ui.screens.wardrobe.PackingScreen
import com.glowup.app.ui.screens.wardrobe.WardrobeScreen
import java.net.URLDecoder

/** Every screen in the app, keyed by route. Kept out of MainActivity so tests can open any route directly. */
@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String, modifier: Modifier = Modifier.fillMaxSize()) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("learn") { LearnScreen(navController) }
        composable("focus") { FocusScreen(navController) }
        composable(
            "session/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            SessionScreen(navController, backStackEntry.arguments?.getInt("index") ?: 0)
        }
        composable(
            "morning_session/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
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
        composable("mood_insights") { MoodInsightsScreen(navController) }
        composable("sleep_log") { SleepLogScreen(navController) }
        composable("fitness") { FitnessScreen(navController) }
        composable("fitness_setup") { FitnessSetupScreen(navController) }
        composable("exercises") { ExerciseLibraryScreen(navController) }
        composable("body_checkin") { BodyCheckInScreen(navController) }
        composable("programme/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
            ProgrammeScreen(navController, it.arguments?.getString("id").orEmpty())
        }
        composable("exercise/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
            ExerciseDetailScreen(navController, it.arguments?.getString("id").orEmpty())
        }
        composable("workout/{key}", arguments = listOf(navArgument("key") { type = NavType.StringType })) {
            WorkoutPreviewScreen(navController, it.arguments?.getString("key").orEmpty())
        }
        composable("player/{key}", arguments = listOf(navArgument("key") { type = NavType.StringType })) {
            WorkoutPlayerScreen(navController, it.arguments?.getString("key").orEmpty())
        }
        composable("glow_check") { GlowCheckScreen(navController) }
        composable("glow_results") { GlowResultsScreen(navController) }
        composable("face_shape") { FaceShapeScreen(navController) }
        composable("glow_routines") { LooksRoutinesScreen(navController) }
        composable("progress_photos") { ProgressPhotosScreen(navController) }
        composable("glow_routine/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
            LooksRoutineDetailScreen(navController, it.arguments?.getString("id").orEmpty())
        }
        composable("blocker") { BlockerScreen(navController) }
        composable("capsule") { CapsuleScreen(navController) }
        composable("packing") { PackingScreen(navController) }
        composable("scenarios") { ScenariosScreen(navController) }
        composable("speech_history") { SpeechHistoryScreen(navController) }
        composable("scenario/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) {
            SpeakingPracticeScreen(navController, scenario = Scenarios.get(it.arguments?.getString("id").orEmpty()))
        }
        composable("your_data") { YourDataScreen(navController) }
        composable("add_routine") { AddRoutineScreen(navController) }
        // The AI stylist is planned for a later phase; until then this goes to the real outfit builder.
        composable("ai_stylist") { WardrobeScreen(navController) }
        composable("morning") { MorningScreen(navController) }
        composable("speaking_practice") { SpeakingPracticeScreen(navController) }
        composable(
            "speaking_practice/{topic}",
            arguments = listOf(navArgument("topic") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("topic")
            val decoded = encoded?.let { URLDecoder.decode(it, "UTF-8") }
            SpeakingPracticeScreen(navController, decoded)
        }
        composable(
            "lesson_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            LessonDetailScreen(navController, backStackEntry.arguments?.getInt("id") ?: 0)
        }
        composable("looks") { LooksScreen(navController) }
        composable(
            "grooming_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            GroomingDetailScreen(navController, backStackEntry.arguments?.getInt("id") ?: 0)
        }
        composable(
            "style_guide_detail/{key}",
            arguments = listOf(navArgument("key") { type = NavType.StringType })
        ) { backStackEntry ->
            StyleGuideDetailScreen(navController, backStackEntry.arguments?.getString("key") ?: "")
        }
        composable(
            "outfit/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            OutfitDetailScreen(navController, backStackEntry.arguments?.getString("id") ?: "")
        }
        composable(
            "routine_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            RoutineDetailScreen(navController, backStackEntry.arguments?.getInt("id") ?: 0)
        }
    }
}
