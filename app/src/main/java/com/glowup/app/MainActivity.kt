package com.glowup.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.glowup.app.billing.Billing
import com.glowup.app.notify.Notifications
import com.glowup.app.ui.components.ProPaywall
import com.glowup.app.ui.components.RoutineDoneBar
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.data.RoutineSession
import com.glowup.app.ui.navigation.AppNavGraph
import com.glowup.app.ui.theme.ReMakeUTheme

class MainActivity : ComponentActivity() {
    private val pendingRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is always light, so system bars need dark icons even when the phone is in dark mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        if (savedInstanceState == null) pendingRoute.value = intent.getStringExtra(Notifications.EXTRA_ROUTE)
        setContent {
            ReMakeUTheme {
                val navController = rememberNavController()
                val route = pendingRoute.value
                LaunchedEffect(route) {
                    if (route != null && ProfileStore.onboarded) navController.navigate(route) { launchSingleTop = true }
                    pendingRoute.value = null
                }
                AppNavGraph(navController, startDestination = if (ProfileStore.onboarded) "home" else "onboarding")
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route
                LaunchedEffect(currentRoute) {
                    // Back on Home means the user has left the page the routine was opened on.
                    if (currentRoute == "home") RoutineSession.clear()
                }
                RoutineDoneBar(currentRoute)
                ProPaywall()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Billing.refreshPurchases()
        // The date may have rolled over while the app was in the background.
        ProgressStore.applyFreezes()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(Notifications.EXTRA_ROUTE)?.let { pendingRoute.value = it }
    }
}
