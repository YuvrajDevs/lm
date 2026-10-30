package com.glowup.app.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.GlowUpOptionRow
import com.glowup.app.ui.theme.*

private data class SettingsRow(val label: String, val value: String? = null, val danger: Boolean = false)

@Composable
fun SettingsScreen(navController: NavHostController) {
    var language by remember { mutableStateOf("English") }
    var appearance by remember { mutableStateOf("System") }
    var dialog by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp, 32.dp, 24.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Ink)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.size(48.dp))
            }

            Row(
                modifier = Modifier
                    .padding(24.dp, 14.dp, 24.dp, 0.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Ink)
                    .padding(16.dp, 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Image(painterResource(R.drawable.ic_profile), contentDescription = null, modifier = Modifier.size(46.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Arjun Mehta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Premium · Renews Dec 14", color = Color.White.copy(alpha = 0.5f), fontSize = 12.5.sp)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 28.dp),
            ) {
                settingsSection(
                    "Profile",
                    listOf(
                        SettingsRow("Gender & preferences"),
                        SettingsRow("Face shape & appearance"),
                        SettingsRow("Areas & topics"),
                    ),
                    onRowClick = { dialog = it },
                )
                settingsSection(
                    "App",
                    listOf(
                        SettingsRow("Notifications"),
                        SettingsRow("Language", language),
                        SettingsRow("Appearance", appearance),
                    ),
                    onRowClick = { label ->
                        when (label) {
                            "Notifications" -> navController.navigate("notifications")
                            else -> dialog = label
                        }
                    },
                )
                settingsSection(
                    "Privacy & data",
                    listOf(
                        SettingsRow("Export my data"),
                        SettingsRow("Clear profile data"),
                        SettingsRow("Delete account", danger = true),
                    ),
                    onRowClick = { label ->
                        if (label == "Delete account") showDeleteConfirm = true else dialog = label
                    },
                )
                settingsSection(
                    "Support",
                    listOf(
                        SettingsRow("Help & FAQ"),
                        SettingsRow("Send feedback"),
                        SettingsRow("Rate Glow-Up"),
                    ),
                    onRowClick = { dialog = it },
                    last = true,
                )
            }
        }

        if (dialog == "Language") {
            PickerDialog(
                title = "Language",
                options = listOf("English", "Hindi", "Spanish"),
                selected = language,
                onSelect = { language = it; dialog = null },
                onDismiss = { dialog = null },
            )
        } else if (dialog == "Appearance") {
            PickerDialog(
                title = "Appearance",
                options = listOf("System", "Light", "Dark"),
                selected = appearance,
                onSelect = { appearance = it; dialog = null },
                onDismiss = { dialog = null },
            )
        } else if (dialog != null) {
            GlowUpDialog(
                onDismissRequest = { dialog = null },
                title = dialog ?: "",
                message = "This is coming soon.",
                primaryText = "OK",
                secondaryText = null,
            )
        }

        if (showDeleteConfirm) {
            GlowUpDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = "Delete account?",
                message = "This permanently deletes your profile, routines, and history. This can't be undone.",
                secondaryText = "Cancel",
                primaryText = "Delete",
                primaryDestructive = true,
                onPrimary = {
                    showDeleteConfirm = false
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
            )
        }
    }
}

@Composable
private fun PickerDialog(title: String, options: List<String>, selected: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    GlowUpDialog(
        onDismissRequest = onDismiss,
        title = title,
        primaryText = "Close",
        secondaryText = null,
        onPrimary = onDismiss,
        content = {
            Column {
                options.forEach { option ->
                    GlowUpOptionRow(option, selected = option == selected) { onSelect(option) }
                }
            }
        },
    )
}

private fun androidx.compose.foundation.lazy.LazyListScope.settingsSection(
    title: String,
    rows: List<SettingsRow>,
    onRowClick: (String) -> Unit,
    last: Boolean = false,
) {
    item {
        Text(
            title.uppercase(),
            color = TextSecondary,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
            modifier = Modifier.padding(bottom = 10.dp),
        )
    }
    items(rows) { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (row.danger) MaroonTint else CardNeutral)
                .clickable { onRowClick(row.label) }
                .padding(15.dp, 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                row.label,
                fontSize = 14.5.sp,
                fontWeight = if (row.danger) FontWeight.SemiBold else FontWeight.Medium,
                color = if (row.danger) Maroon else Ink,
                modifier = Modifier.weight(1f),
            )
            row.value?.let {
                Text(it, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(end = 6.dp))
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = if (row.danger) Maroon else TextSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
    item { Spacer(Modifier.height(if (last) 0.dp else 10.dp)) }
}
