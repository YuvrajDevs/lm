package com.glowup.app.ui.screens.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.GlowUpBottomNav
import com.glowup.app.ui.components.NavTab
import com.glowup.app.ui.theme.Surface

@Composable
fun PlaceholderScreen(navController: NavHostController, tab: NavTab, label: String) {
    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom) {
            Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "$label — coming next")
            }
            GlowUpBottomNav(navController, tab)
        }
    }
}
