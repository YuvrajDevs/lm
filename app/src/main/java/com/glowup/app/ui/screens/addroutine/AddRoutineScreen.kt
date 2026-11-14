package com.glowup.app.ui.screens.addroutine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.ReMakeUOptionRow
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.parseClock
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

private const val NO_REMINDER = "No reminder"

private data class TemplateGroup(val label: String, val icon: Int, val tint: Color, val items: List<String>)

private val templateGroups = listOf(
    TemplateGroup(
        "Looks",
        R.drawable.ic_looks,
        CoralTint,
        listOf(
            "Skincare — AM & PM", "Posture check", "Drink 2L of water", "Cold shower",
            "Stretch your neck & shoulders", "Eyebrow check", "Beard trim & shape",
            "Apply SPF before leaving", "Change your pillowcase"
        )
    ),
    TemplateGroup(
        "Fitness",
        R.drawable.ic_determined,
        GreenTint,
        listOf(
            "10-minute morning walk",
            "20 push-ups",
            "Stretch before bed",
            "Posture-fix exercises",
            "Jaw & neck mobility",
            "Take the stairs today"
        )
    ),
    TemplateGroup(
        "Mind",
        R.drawable.ic_reflective,
        PurpleTint,
        listOf("Journaling or gratitude", "Phone-free first 30 min", "5-minute meditation", "Read 10 pages", "No social media before noon")
    ),
    TemplateGroup(
        "Social",
        R.drawable.ic_social,
        GreenTint,
        listOf("2-minute eye contact practice", "Message a friend first", "Compliment someone genuinely", "Start one new conversation")
    ),
    TemplateGroup(
        "Style",
        R.drawable.ic_style,
        BlueTint,
        listOf("Lay out tomorrow's outfit", "Polish your shoes", "Iron a shirt for the week", "Declutter one wardrobe shelf")
    ),
    TemplateGroup(
        "Sleep",
        R.drawable.ic_sleep,
        BlueTint,
        listOf("Screens off 30 min before bed", "Same bedtime every night", "No caffeine after 4 PM", "Wind-down stretch routine")
    ),
    TemplateGroup(
        "Life admin",
        R.drawable.ic_lifeadmin,
        NavyTint,
        listOf("Tidy your desk", "Plan tomorrow's top 3", "Inbox zero for 10 minutes", "Review your budget")
    )
)

private val reminderTimes = listOf(NO_REMINDER, "6:00 AM", "7:00 AM", "8:00 AM", "12:00 PM", "6:00 PM", "8:00 PM", "9:00 PM", "9:30 PM", "10:00 PM")

@Composable
fun AddRoutineScreen(navController: NavHostController) {
    var showTemplates by remember { mutableStateOf(true) }
    var addedItems by remember { mutableStateOf(setOf<String>()) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(NO_REMINDER) }
    val askPermission = rememberNotificationPermission()
    var customText by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf("Looks") }
    var showSaved by remember { mutableStateOf(false) }

    fun groupFor(item: String) = templateGroups.firstOrNull { item in it.items }?.label ?: "Looks"

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.add_routine_add_a_routine)) { navController.goBack() }

            Row(
                modifier = Modifier.fillMaxWidth().padding(Space.Gutter, Space.L, Space.Gutter, 0.dp),
                horizontalArrangement = Arrangement.spacedBy(Space.S)
            ) {
                listOf("Templates" to true, "Custom" to false).forEach { (label, isTemplates) ->
                    val selected = showTemplates == isTemplates
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(Shapes.Pill)
                            .background(if (selected) Ink else ChipNeutral)
                            .clickable { showTemplates = isTemplates }
                            .padding(vertical = Space.M),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (selected) Color.White else Ink,
                            fontSize = TextSize.Body,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.Gutter)) {
                if (showTemplates) {
                    templateGroups.forEach { group ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                            Image(painterResource(group.icon), contentDescription = null, modifier = Modifier.size(26.dp))
                            Text(
                                group.label.uppercase(),
                                color = TextSecondary,
                                fontSize = TextSize.BodySmall,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp
                            )
                        }
                        Spacer(Modifier.height(Space.S))
                        Column(verticalArrangement = Arrangement.spacedBy(Space.S)) {
                            group.items.forEach { item ->
                                val isAdded = item in addedItems
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(Shapes.Medium)
                                        .background(group.tint)
                                        .clickable {
                                            addedItems = if (isAdded) addedItems - item else addedItems + item
                                        }
                                        .padding(Space.L, Space.M),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
                                    Box(
                                        modifier = Modifier.size(26.dp).clip(CircleShape).background(if (isAdded) Green else Ink),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (isAdded) Icons.Filled.Check else Icons.Filled.Add,
                                            contentDescription = if (isAdded) "Added" else "Add",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(Space.Xl))
                    }
                } else {
                    val selectedDays = remember { mutableStateListOf(true, true, true, true, true, false, false) }

                    Text(
                        stringResource(R.string.add_routine_what_do_you_want_to),
                        color = TextSecondary,
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(Space.S))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Medium)
                            .background(CardNeutral)
                            .padding(Space.L, 2.dp)
                    ) {
                        OutlinedTextField(
                            value = customText,
                            onValueChange = { customText = it },
                            placeholder = {
                                Text(stringResource(R.string.add_routine_e_g_stretch_before_bed), color = TextSecondary, fontSize = TextSize.BodyLarge)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(Modifier.height(Space.Xl))
                    Text(stringResource(R.string.add_routine_repeat_on), color = TextSecondary, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Space.S))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { i, day ->
                            val on = selectedDays[i]
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (on) Ink else ChipNeutral)
                                    .clickable { selectedDays[i] = !selectedDays[i] },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(day, color = if (on) Color.White else Ink, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(Modifier.height(Space.Xl))
                    Text(
                        stringResource(R.string.add_routine_time_amp_alarm),
                        color = TextSecondary,
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(Space.S))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Medium)
                            .background(CardNeutral)
                            .clickable { showTimeDialog = true }
                            .padding(Space.L, Space.M),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedTime, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(Space.S))
                    Text(stringResource(R.string.add_routine_a_daily_notification_at_this), color = TextSecondary, fontSize = TextSize.Caption)

                    Spacer(Modifier.height(Space.Xl))
                    Text(stringResource(R.string.add_routine_area), color = TextSecondary, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Space.S))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        items(listOf("Looks", "Fitness", "Sleep", "Mind", "Social", "Style", "Life admin")) { area ->
                            val selected = area == selectedArea
                            Box(
                                modifier = Modifier
                                    .clip(Shapes.Pill)
                                    .background(if (selected) Ink else ChipNeutral)
                                    .clickable { selectedArea = area }
                                    .padding(Space.L, Space.S)
                            ) {
                                Text(
                                    area,
                                    color = if (selected) Color.White else Ink,
                                    fontSize = TextSize.BodySmall,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.padding(Space.Gutter, Space.M, Space.Gutter, Space.Section)) {
                val canSave = if (showTemplates) addedItems.isNotEmpty() else customText.isNotBlank()
                AppButton(stringResource(R.string.add_routine_save_routine), variant = ButtonVariant.Primary, enabled = canSave) {
                    if (showTemplates) {
                        addedItems.forEach { item -> RoutineStore.add(groupFor(item), item) }
                    } else {
                        val reminder = selectedTime.takeIf { it != NO_REMINDER }?.let(::parseClock)
                        RoutineStore.add(selectedArea, customText.trim(), reminderTime = reminder)
                        if (reminder != null) askPermission()
                    }
                    showSaved = true
                }
            }
        }

        if (showSaved) {
            ReMakeUDialog(
                onDismissRequest = { navController.goBack() },
                title = "Routine saved",
                message = "It's been added to your routines list.",
                primaryText = "Done",
                secondaryText = null,
                onPrimary = { navController.goBack() }
            )
        }

        if (showTimeDialog) {
            ReMakeUDialog(
                onDismissRequest = { showTimeDialog = false },
                title = "Time & alarm",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        reminderTimes.forEach { time ->
                            ReMakeUOptionRow(time, selected = time == selectedTime) { selectedTime = time }
                        }
                    }
                }
            )
        }
    }
}
