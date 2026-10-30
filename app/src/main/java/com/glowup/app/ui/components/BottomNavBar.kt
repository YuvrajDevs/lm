package com.glowup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.ui.theme.Ink

enum class NavTab(val route: String) { HOME("home"), ROUTINES("routines"), STYLING("styling"), SETTINGS("settings") }

@Composable
fun GlowUpBottomNav(navController: NavHostController, current: NavTab) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .height(68.dp)
            .background(Ink, shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavIcon(selected = current == NavTab.HOME, icon = Icons.Filled.Home) {
                if (current != NavTab.HOME) navController.navigate(NavTab.HOME.route)
            }
            NavIcon(selected = current == NavTab.ROUTINES, icon = Icons.Filled.AccessTime) {
                if (current != NavTab.ROUTINES) navController.navigate(NavTab.ROUTINES.route)
            }
            NavIcon(selected = current == NavTab.STYLING, icon = Icons.Filled.Style) {
                if (current != NavTab.STYLING) navController.navigate(NavTab.STYLING.route)
            }
            NavIcon(selected = current == NavTab.SETTINGS, icon = Icons.Filled.Person) {
                if (current != NavTab.SETTINGS) navController.navigate(NavTab.SETTINGS.route)
            }
        }
    }
}

@Composable
private fun RowScope.NavIcon(selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick)
            .background(if (selected) Color.White else Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Ink else Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}
