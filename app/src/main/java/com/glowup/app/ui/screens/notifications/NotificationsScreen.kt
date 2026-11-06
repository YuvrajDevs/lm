package com.glowup.app.ui.screens.notifications

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.goBack
import com.glowup.app.notify.Notifications
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.GlowUpOptionRow
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.Reminder
import com.glowup.app.ui.data.ReminderStore
import com.glowup.app.ui.data.Schedule
import com.glowup.app.ui.theme.*

@Composable
fun NotificationsScreen(navController: NavHostController) {
    val context = LocalContext.current
    var permissionVersion by remember { mutableStateOf(0) }
    val canPost = remember(permissionVersion) { Notifications.canPost(context) }
    val askPermission = rememberNotificationPermission { permissionVersion++ }
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        permissionVersion++
        onPauseOrDispose { }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newSchedule by remember { mutableStateOf<Schedule>(Schedule.Daily(java.time.LocalTime.of(20, 0))) }
    var pickingFor by remember { mutableStateOf<Reminder?>(null) }
    var pickingForNew by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Reminder?>(null) }

    val custom = ReminderStore.custom
    val suggestions = ReminderStore.suggestions.filter { (title, _) -> custom.none { it.title == title } }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Reminders") { navController.goBack() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 20.dp, 24.dp, 24.dp),
            ) {
                if (!canPost) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaroonTint)
                                .clickable {
                                    askPermission()
                                    context.startActivity(
                                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                                    )
                                }
                                .padding(16.dp, 14.dp),
                        ) {
                            Text("Notifications are off", color = Maroon, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("Reminders can't reach you until you allow them. Tap to turn them on.", color = Maroon, fontSize = 12.sp)
                        }
                    }
                }
                item { SectionLabel("YOUR REMINDERS") }
                if (custom.isEmpty()) {
                    item {
                        Text("No reminders yet — add one below or with the + button.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                    }
                }
                items(custom, key = { it.id }) { reminder ->
                    ReminderRow(reminder, onClick = { editing = reminder }) { on ->
                        ReminderStore.setEnabled(reminder.id, on)
                        if (on) askPermission()
                    }
                    Spacer(Modifier.height(10.dp))
                }
                item { Spacer(Modifier.height(10.dp)) }
                item { SectionLabel("BUILT-IN REMINDERS") }
                items(ReminderStore.builtIn, key = { it.id }) { reminder ->
                    ReminderRow(reminder, onClick = { pickingFor = reminder }) { on ->
                        ReminderStore.setEnabled(reminder.id, on)
                        if (on) askPermission()
                    }
                    Spacer(Modifier.height(10.dp))
                }
                if (suggestions.isNotEmpty()) {
                    item { Spacer(Modifier.height(10.dp)) }
                    item { SectionLabel("SUGGESTED FOR YOU") }
                    items(suggestions, key = { it.first }) { (title, schedule) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(ChipNeutral)
                                .clickable {
                                    ReminderStore.add(title, schedule)
                                    askPermission()
                                }
                                .padding(16.dp, 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                                Text(schedule.label, color = TextSecondary, fontSize = 12.sp)
                            }
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(Ink),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(70.dp)) }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp, 0.dp, 24.dp, 26.dp)
                .size(62.dp)
                .clip(CircleShape)
                .background(Ink)
                .clickable { showAddDialog = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add reminder", tint = Color.White, modifier = Modifier.size(22.dp))
        }

        if (showAddDialog) {
            GlowUpDialog(
                onDismissRequest = { showAddDialog = false; newTitle = "" },
                title = "Add a reminder",
                secondaryText = "Cancel",
                primaryText = "Add",
                primaryEnabled = newTitle.isNotBlank(),
                onPrimary = {
                    ReminderStore.add(newTitle, newSchedule)
                    askPermission()
                    showAddDialog = false
                    newTitle = ""
                },
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it.take(60) },
                            placeholder = { Text("Reminder title") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                        )
                        ScheduleField(newSchedule) { pickingForNew = true }
                    }
                },
            )
        }

        editing?.let { reminder ->
            GlowUpDialog(
                onDismissRequest = { editing = null },
                title = reminder.title,
                message = reminder.schedule.label,
                secondaryText = "Delete",
                secondaryDestructive = true,
                onSecondary = { ReminderStore.remove(reminder.id); editing = null },
                primaryText = "Change time",
                onPrimary = { pickingFor = reminder; editing = null },
            )
        }

        if (pickingForNew || pickingFor != null) {
            val current = pickingFor?.let { picked -> ReminderStore.items.firstOrNull { it.id == picked.id }?.schedule } ?: newSchedule
            GlowUpDialog(
                onDismissRequest = { pickingFor = null; pickingForNew = false },
                title = "When?",
                primaryText = "Done",
                secondaryText = null,
                content = {
                    Column(modifier = Modifier.heightIn(max = 420.dp).verticalScrollable()) {
                        Schedule.presets.forEach { option ->
                            GlowUpOptionRow(option.label, selected = option == current) {
                                val target = pickingFor
                                if (target != null) ReminderStore.setSchedule(target.id, option) else newSchedule = option
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun Modifier.verticalScrollable(): Modifier =
    this.verticalScroll(androidx.compose.foundation.rememberScrollState())

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun ScheduleField(schedule: Schedule, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardNeutral)
            .clickable(onClick = onClick)
            .padding(16.dp, 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(schedule.label, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text("Change", color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ReminderRow(item: Reminder, onClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardNeutral)
            .clickable(onClick = onClick)
            .padding(16.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
            Text(item.schedule.label, color = TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = item.enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Ink,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE6E3DC),
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent,
            ),
        )
    }
}
