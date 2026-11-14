package com.glowup.app.ui.screens.mood

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.MoodStore
import com.glowup.app.ui.data.moodOptions
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
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
            Row(modifier = Modifier.fillMaxWidth().padding(Space.Gutter, Space.M, Space.Gutter, 0.dp), horizontalArrangement = Arrangement.End) {
                Text(
                    stringResource(R.string.mood_checkin_skip),
                    color = TextSecondary,
                    fontSize = TextSize.Body,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(Shapes.Pill).clickable { navController.goBack() }.padding(Space.S, Space.Xs)
                )
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.Xs, Space.Gutter, Space.M)) {
                Text(dateLabel, color = TextSecondary, fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                Spacer(Modifier.height(Space.S))
                Text(stringResource(R.string.mood_checkin_how_are_you), fontSize = TextSize.Display, lineHeight = LineHeight.Display)
                Text(
                    stringResource(R.string.mood_checkin_feeling_today),
                    fontSize = TextSize.Display,
                    fontWeight = FontWeight.Bold,
                    lineHeight = LineHeight.Display
                )
                Spacer(Modifier.height(Space.L))

                moodOptions.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.S), modifier = Modifier.padding(bottom = Space.S)) {
                        row.forEach { mood ->
                            val isSelected = selected == mood.label
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(Shapes.Card)
                                    .background(if (isSelected) ChipNeutral else DialogSurface)
                                    .border(2.dp, if (isSelected) Ink else Color.Transparent, Shapes.Card)
                                    .clickable { selected = mood.label }
                                    .padding(Space.Xs, Space.M),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Space.S)
                            ) {
                                Image(painterResource(mood.icon), contentDescription = null, modifier = Modifier.size(44.dp))
                                Text(mood.label, fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Space.M))
                Text(stringResource(R.string.mood_checkin_what_s_been_going_on), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.mood_checkin_optional_tags_help_re_makeu),
                    color = TextSecondary,
                    fontSize = TextSize.Caption,
                    modifier = Modifier.padding(top = 2.dp, bottom = Space.M)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.S), verticalArrangement = Arrangement.spacedBy(Space.S)) {
                    MoodStore.allTags.forEach { tag ->
                        val on = tag in chosenTags
                        Text(
                            tag,
                            color = if (on) Color.White else Ink,
                            fontSize = TextSize.BodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(Shapes.Pill)
                                .background(if (on) Ink else ChipNeutral)
                                .clickable { chosenTags = if (on) chosenTags - tag else chosenTags + tag }
                                .padding(Space.M, Space.S)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(Shapes.Pill)
                            .border(1.5.dp, TextMuted, Shapes.Pill)
                            .clickable { showAddTag = true }
                            .padding(Space.M, Space.S),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Ink, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(Space.Xs))
                        Text(stringResource(R.string.mood_checkin_your_own), fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(Space.L))
                Box(modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.M, 2.dp)) {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it.take(500) },
                        placeholder = {
                            Text(stringResource(R.string.mood_checkin_add_a_quick_note_optional), color = TextSecondary, fontSize = TextSize.Body)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            }

            Box(modifier = Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Gutter)) {
                AppButton(
                    if (selected ==
                        null
                    ) {
                        stringResource(R.string.mood_checkin_pick_a_mood)
                    } else {
                        stringResource(R.string.common_save_check_in)
                    },
                    variant = ButtonVariant.Primary,
                    enabled = selected != null
                ) {
                    selected?.let { mood -> MoodStore.logToday(mood, note, MoodStore.allTags.filter { it in chosenTags }) }
                    navController.goBack()
                }
            }
        }

        if (showAddTag) {
            AddTagDialog(
                onAdded = { tag -> chosenTags = chosenTags + tag },
                onRemoved = { tag -> chosenTags = chosenTags - tag },
                onDismiss = { showAddTag = false }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddTagDialog(onAdded: (String) -> Unit, onRemoved: (String) -> Unit, onDismiss: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    ReMakeUDialog(
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
                error =
                    if (MoodStore.customTags.size >=
                        MoodStore.MAX_CUSTOM_TAGS
                    ) {
                        "You can have up to ${MoodStore.MAX_CUSTOM_TAGS} of your own tags."
                    } else {
                        "That tag already exists."
                    }
            }
        },
        content = {
            OutlinedTextField(
                value = draft,
                onValueChange = {
                    draft = it.take(24)
                    error = null
                },
                placeholder = { Text(stringResource(R.string.mood_checkin_e_g_gaming_gym_meditation)) },
                singleLine = true,
                isError = error != null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                modifier = Modifier.fillMaxWidth()
            )
            error?.let { Text(it, color = Maroon, fontSize = TextSize.Caption, modifier = Modifier.padding(top = Space.S)) }
            if (MoodStore.customTags.isNotEmpty()) {
                Spacer(Modifier.height(Space.M))
                Text(
                    stringResource(R.string.mood_checkin_your_tags),
                    color = TextSecondary,
                    fontSize = TextSize.Micro,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                )
                Spacer(Modifier.height(Space.S))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.S), verticalArrangement = Arrangement.spacedBy(Space.S)) {
                    MoodStore.customTags.toList().forEach { tag ->
                        Row(
                            modifier = Modifier
                                .clip(Shapes.Pill)
                                .background(ChipNeutral)
                                .clickable {
                                    MoodStore.removeCustomTag(tag)
                                    onRemoved(tag)
                                }
                                .padding(Space.M, Space.S),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tag, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(Space.Xs))
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.mood_checkin_remove_1_s, tag),
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}
