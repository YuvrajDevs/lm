package com.glowup.app.ui.components

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController

/**
 * Pops one screen, ignoring repeat taps while a transition is running and never popping the
 * last screen — rapid double taps on back/Done buttons would otherwise empty the back stack.
 */
fun NavHostController.goBack() {
    val current = currentBackStackEntry ?: return
    if (current.lifecycle.currentState != Lifecycle.State.RESUMED) return
    if (previousBackStackEntry == null) return
    popBackStack()
}
