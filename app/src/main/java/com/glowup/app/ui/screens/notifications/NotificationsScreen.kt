package com.glowup.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.GlowUpOptionRow
import com.glowup.app.ui.theme.*

private data class ReminderItem(val title: String, val meta: String, val defaultOn: Boolean)

private val yourReminders = listOf(
    ReminderItem("Lay out tomorrow's outfit", "8:00 PM · Daily · Linked to a routine card", true),
    ReminderItem("Drink water", "Every 2 hours · Notification only", true),
    ReminderItem("Call mom", "Sun · One-time", false),
)

private val builtInReminders = listOf(
    ReminderItem("Sunscreen reapply", "Off by default", false),
    ReminderItem("Eating nudge", "A gentle nudge, not a fixed schedule", false),
)

private data class SuggestedReminder(val title: String, val defaultTime: String)

private val suggestedReminders = listOf(
    SuggestedReminder("Posture check", "Every 2 hours"),
    SuggestedReminder("Stand up & stretch", "Every hour"),
    SuggestedReminder("Skincare — PM routine", "9:30 PM · Daily"),
    SuggestedReminder("Floss before bed", "10:00 PM · Daily"),
    SuggestedReminder("Drink water", "Every 2 hours"),
    SuggestedReminder("Lay out tomorrow's outfit", "8:00 PM · Daily"),
    SuggestedReminder("Wind down — screens off", "10:30 PM · Daily"),
    SuggestedReminder("Weekly wardrobe refresh", "Sun · Weekly"),
)

private val timeChipOptions = listOf(
    "Every hour", "Every 2 hours", "8:00 AM · Daily", "7:00 PM · Daily",
    "9:00 PM · Daily", "10:00 PM · Daily", "Sun · Weekly", "Notification only",
)

@Composable
fun NotificationsScreen(navController: NavHostController) {
    val customReminders = remember { yourReminders.toMutableStateList() }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newTime by remember { mutableStateOf(timeChipOptions.first()) }
    var showTimePicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Reminders") { navController.popBackStack() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 20.dp, 24.dp, 24.dp),
            ) {
                item {
                    Text("YOUR REMINDERS", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                    Spacer(Modifier.height(10.dp))
                }
                items(customReminders) { ReminderRow(it); Spacer(Modifier.height(10.dp)) }
                item { Spacer(Modifier.height(10.dp)) }
                item {
                    Text("BUILT-IN REMINDERS", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                    Spacer(Modifier.height(10.dp))
                }
                items(builtInReminders) { ReminderRow(it); Spacer(Modifier.height(10.dp)) }
                item { Spacer(Modifier.height(10.dp)) }
                item {
                    Text("SUGGESTED FOR YOU", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                    Spacer(Modifier.height(10.dp))
                }
                items(suggestedReminders.filter { s -> customReminders.none { it.title == s.title } }) { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ChipNeutral)
                            .clickable { customReminders.add(0, ReminderItem(suggestion.title, suggestion.defaultTime, true)) }
                            .padding(16.dp, 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(suggestion.title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(suggestion.defaultTime, color = TextSecondary, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier.size(28.dp).clip(CircleShape).background(Ink),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(14.dp))
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
                onDismissRequest = { showAddDialog = false; newTitle = ""; newTime = timeChipOptions.first() },
                title = "Add a reminder",
                secondaryText = "Cancel",
                primaryText = "Add",
                primaryEnabled = newTitle.isNotBlank(),
                onPrimary = {
                    customReminders.add(0, ReminderItem(newTitle.trim(), newTime, true))
                    showAddDialog = false
                    newTitle = ""
                    newTime = timeChipOptions.first()
                },
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("Reminder title") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(CardNeutral)
                                .clickable { showTimePicker = true }
                                .padding(16.dp, 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(newTime, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Change", color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
            )
        }

        if (showTimePicker) {
            GlowUpDialog(
                onDismissRequest = { showTimePicker = false },
                title = "When?",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        timeChipOptions.forEach { time ->
                            GlowUpOptionRow(time, selected = time == newTime) { newTime = time }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun ReminderRow(item: ReminderItem) {
    var checked by remember { mutableStateOf(item.defaultOn) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardNeutral)
            .padding(16.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
            Text(item.meta, color = TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
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
