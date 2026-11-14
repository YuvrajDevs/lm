package com.glowup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.theme.Ink

enum class NavTab(val route: String) { HOME("home"), LEARN("learn"), ROUTINES("routines"), STYLING("styling"), SETTINGS("settings") }

@Composable
fun ReMakeUBottomNav(navController: NavHostController, current: NavTab) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.Xl, vertical = Space.L)
            .height(68.dp)
            .background(Ink, shape = Shapes.Pill),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Space.M),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIcon(selected = current == NavTab.HOME, label = stringResource(R.string.nav_home), icon = Icons.Filled.Home) {
                if (current != NavTab.HOME) navController.navigate(NavTab.HOME.route)
            }
            NavIcon(selected = current == NavTab.LEARN, label = stringResource(R.string.nav_learn), icon = Icons.Filled.School) {
                if (current != NavTab.LEARN) navController.navigate(NavTab.LEARN.route)
            }
            NavIcon(selected = current == NavTab.ROUTINES, label = stringResource(R.string.common_routine_routines), icon = Icons.Filled.AccessTime) {
                if (current != NavTab.ROUTINES) navController.navigate(NavTab.ROUTINES.route)
            }
            NavIcon(selected = current == NavTab.STYLING, label = stringResource(R.string.nav_style), icon = Icons.Filled.Style) {
                if (current != NavTab.STYLING) navController.navigate(NavTab.STYLING.route)
            }
            NavIcon(selected = current == NavTab.SETTINGS, label = stringResource(R.string.nav_profile), icon = Icons.Filled.Person) {
                if (current != NavTab.SETTINGS) navController.navigate(NavTab.SETTINGS.route)
            }
        }
    }
}

@Composable
private fun RowScope.NavIcon(selected: Boolean, label: String, icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .background(if (selected) Color.White else Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Ink else Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
