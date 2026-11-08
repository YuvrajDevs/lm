package com.glowup.app.ui.screens.mood

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.MoodStore
import com.glowup.app.ui.data.moodOptions
import com.glowup.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodCheckinScreen(navController: NavHostController) {
    val today = remember { LocalDate.now() }
    var selected by remember { mutableStateOf(MoodStore.moodOn(today)) }
    var note by remember { mutableStateOf(MoodStore.noteOn(today).orEmpty()) }
    var chosenTags by remember { mutableStateOf(MoodStore.tagsOn(today).toSet()) }
    var showAddTag by remember { mutableStateOf(false) }
    val dateLabel = remember { today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())).uppercase() }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding().imePadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(24.dp, 12.dp, 24.dp, 0.dp), horizontalArrangement = Arrangement.End) {
                Text(
                    "Skip",
                    color = TextSecondary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(RoundedCornerShape(999.dp)).clickable { navController.goBack() }.padding(8.dp, 4.dp),
                )
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 4.dp, 24.dp, 12.dp)) {
                Text(dateLabel, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                Spacer(Modifier.height(6.dp))
                Text("How are you", fontSize = 27.sp, lineHeight = 33.sp)
                Text("feeling today?", fontSize = 27.sp, fontWeight = FontWeight.Bold, lineHeight = 33.sp)
                Spacer(Modifier.height(16.dp))

                moodOptions.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                        row.forEach { mood ->
                            val isSelected = selected == mood.label
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) ChipNeutral else Color(0xFFFAF9F6))
                                    .border(2.dp, if (isSelected) Ink else Color.Transparent, RoundedCornerShape(20.dp))
                                    .clickable { selected = mood.label }
                                    .padding(4.dp, 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Image(painterResource(mood.icon), contentDescription = null, modifier = Modifier.size(44.dp))
                                Text(mood.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("What's been going on?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Optional · tags help Glow-Up spot what lifts your mood",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoodStore.allTags.forEach { tag ->
                        val on = tag in chosenTags
                        Text(
                            tag,
                            color = if (on) Color.White else Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (on) Ink else ChipNeutral)
                                .clickable { chosenTags = if (on) chosenTags - tag else chosenTags + tag }
                                .padding(14.dp, 9.dp),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .border(1.5.dp, TextMuted, RoundedCornerShape(999.dp))
                            .clickable { showAddTag = true }
                            .padding(12.dp, 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Ink, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Your own", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(18.dp))
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardNeutral).padding(12.dp, 2.dp)) {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it.take(500) },
                        placeholder = { Text("Add a quick note (optional)", color = TextSecondary, fontSize = 13.5.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                    )
                }
            }

            Box(modifier = Modifier.padding(24.dp, 8.dp, 24.dp, 24.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected != null) Ink else Color(0xFFD8D5CE))
                        .clickable(enabled = selected != null) {
                            selected?.let { mood -> MoodStore.logToday(mood, note, MoodStore.allTags.filter { it in chosenTags }) }
                            navController.goBack()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (selected == null) "Pick a mood" else "Save check-in", color = Color.White, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (showAddTag) {
            AddTagDialog(
                onAdded = { tag -> chosenTags = chosenTags + tag },
                onRemoved = { tag -> chosenTags = chosenTags - tag },
                onDismiss = { showAddTag = false },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddTagDialog(onAdded: (String) -> Unit, onRemoved: (String) -> Unit, onDismiss: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    GlowUpDialog(
        onDismissRequest = onDismiss,
        title = "Add your own tag",
        secondaryText = "Done",
        primaryText = "Add",
        primaryEnabled = draft.isNotBlank(),
        onPrimary = {
            val added = MoodStore.addCustomTag(draft)
            if (added != null) {
                onAdded(added)
                draft = ""
                error = null
            } else {
                error = if (MoodStore.customTags.size >= MoodStore.MAX_CUSTOM_TAGS) "You can have up to ${MoodStore.MAX_CUSTOM_TAGS} of your own tags." else "That tag already exists."
            }
        },
        content = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(24); error = null },
                placeholder = { Text("e.g. Gaming, Gym, Meditation") },
                singleLine = true,
                isError = error != null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                modifier = Modifier.fillMaxWidth(),
            )
            error?.let { Text(it, color = Maroon, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)) }
            if (MoodStore.customTags.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text("YOUR TAGS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoodStore.customTags.toList().forEach { tag ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(ChipNeutral)
                                .clickable { MoodStore.removeCustomTag(tag); onRemoved(tag) }
                                .padding(12.dp, 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(tag, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.Close, contentDescription = "Remove $tag", tint = TextSecondary, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
        },
    )
}
