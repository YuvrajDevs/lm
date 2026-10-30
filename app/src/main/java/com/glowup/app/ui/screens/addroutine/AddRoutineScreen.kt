package com.glowup.app.ui.screens.addroutine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.GlowUpOptionRow
import com.glowup.app.ui.theme.*

private data class TemplateGroup(val label: String, val icon: Int, val tint: Color, val items: List<String>)

private val templateGroups = listOf(
    TemplateGroup("Looks", R.drawable.ic_looks, CoralTint, listOf("Skincare — AM & PM", "Posture check")),
    TemplateGroup("Mind", R.drawable.ic_reflective, PurpleTint, listOf("Journaling or gratitude", "Phone-free first 30 min")),
    TemplateGroup("Life admin", R.drawable.ic_lifeadmin, BlueTint, listOf("Tidy your desk", "Plan tomorrow's top 3")),
    TemplateGroup("Style", R.drawable.ic_style, BlueTint, listOf("Lay out tomorrow's outfit")),
)

@Composable
fun AddRoutineScreen(navController: NavHostController) {
    var showTemplates by remember { mutableStateOf(true) }
    var addedItems by remember { mutableStateOf(setOf<String>()) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("9:00 PM") }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Add a routine") { navController.popBackStack() }

            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp, 16.dp, 24.dp, 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Templates" to true, "Custom" to false).forEach { (label, isTemplates) ->
                    val selected = showTemplates == isTemplates
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) Ink else ChipNeutral)
                            .clickable { showTemplates = isTemplates }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = if (selected) Color.White else Ink, fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                    }
                }
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 18.dp, 24.dp, 24.dp)) {
                if (showTemplates) {
                    templateGroups.forEach { group ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Image(painterResource(group.icon), contentDescription = null, modifier = Modifier.size(26.dp))
                            Text(group.label.uppercase(), color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            group.items.forEach { item ->
                                val isAdded = item in addedItems
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(group.tint)
                                        .clickable {
                                            addedItems = if (isAdded) addedItems - item else addedItems + item
                                        }
                                        .padding(16.dp, 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(item, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Box(
                                        modifier = Modifier.size(26.dp).clip(CircleShape).background(if (isAdded) Green else Ink),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            if (isAdded) Icons.Filled.Check else Icons.Filled.Add,
                                            contentDescription = if (isAdded) "Added" else "Add",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp),
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                } else {
                    var customText by remember { mutableStateOf("") }
                    val selectedDays = remember { mutableStateListOf(true, true, true, true, true, false, false) }
                    var selectedArea by remember { mutableStateOf("Looks") }

                    Text("What do you want to do?", color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardNeutral)
                            .padding(16.dp, 2.dp),
                    ) {
                        OutlinedTextField(
                            value = customText,
                            onValueChange = { customText = it },
                            placeholder = { Text("e.g. Stretch before bed", color = TextSecondary, fontSize = 14.5.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                            ),
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Repeat on", color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { i, day ->
                            val on = selectedDays[i]
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (on) Ink else ChipNeutral)
                                    .clickable { selectedDays[i] = !selectedDays[i] },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(day, color = if (on) Color.White else Ink, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Time & alarm", color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardNeutral)
                            .clickable { showTimeDialog = true }
                            .padding(16.dp, 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(selectedTime, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Needs exact-alarm permission — we'll ask once.", color = TextMuted, fontSize = 11.5.sp)

                    Spacer(Modifier.height(20.dp))
                    Text("Area", color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Looks", "Sleep", "Mind", "Social", "Style")) { area ->
                            val selected = area == selectedArea
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (selected) Ink else ChipNeutral)
                                    .clickable { selectedArea = area }
                                    .padding(16.dp, 9.dp),
                            ) {
                                Text(area, color = if (selected) Color.White else Ink, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.padding(24.dp, 14.dp, 24.dp, 28.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Ink)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Save routine", color = Color.White, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (showTimeDialog) {
            GlowUpDialog(
                onDismissRequest = { showTimeDialog = false },
                title = "Time & alarm",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column {
                        listOf("6:00 AM", "7:00 AM", "8:00 PM", "9:00 PM", "10:00 PM").forEach { time ->
                            GlowUpOptionRow(time, selected = time == selectedTime) { selectedTime = time }
                        }
                    }
                },
            )
        }
    }
}
