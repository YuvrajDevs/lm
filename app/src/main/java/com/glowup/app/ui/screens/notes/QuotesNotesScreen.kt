package com.glowup.app.ui.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.NotesStore
import com.glowup.app.ui.data.UserNote
import com.glowup.app.ui.data.quoteLibrary
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

private val quoteTints = listOf(CoralTint, CardNeutral, BlueTint, GreenTint, PurpleTint)

@Composable
fun QuotesNotesScreen(navController: NavHostController) {
    var showQuotes by remember { mutableStateOf(true) }
    var selectedTag by remember { mutableStateOf("All") }
    val quoteTags = listOf("All") + quoteLibrary.map { it.tag }.distinct()
    val filteredQuotes = if (selectedTag == "All") quoteLibrary else quoteLibrary.filter { it.tag == selectedTag }
    var showAddNote by remember { mutableStateOf(false) }
    var newNoteText by remember { mutableStateOf("") }
    var selectedNote by remember { mutableStateOf<UserNote?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.quotes_notes_quotes_amp_notes)) { navController.goBack() }

            Row(
                modifier = Modifier.fillMaxWidth().padding(Space.Gutter, Space.M, Space.Gutter, 0.dp),
                horizontalArrangement = Arrangement.spacedBy(Space.S)
            ) {
                listOf("Quotes" to true, "My Notes" to false).forEach { (label, isQuotes) ->
                    val selected = showQuotes == isQuotes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(Shapes.Pill)
                            .background(if (selected) Ink else ChipNeutral)
                            .clickable { showQuotes = isQuotes }
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

            if (showQuotes) {
                LazyRow(
                    contentPadding = PaddingValues(Space.Gutter, Space.M, Space.Gutter, 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(Space.S)
                ) {
                    items(quoteTags) { tag ->
                        val selected = tag == selectedTag
                        Box(
                            modifier = Modifier
                                .clip(Shapes.Pill)
                                .background(if (selected) Ink else ChipNeutral)
                                .clickable { selectedTag = tag }
                                .padding(Space.L, Space.S)
                        ) {
                            Text(
                                tag,
                                color = if (selected) Color.White else Ink,
                                fontSize = TextSize.BodySmall,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(Space.Gutter, Space.M, Space.Gutter, Space.Gutter),
                    verticalArrangement = Arrangement.spacedBy(Space.S)
                ) {
                    items(filteredQuotes) { quote ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(Shapes.Card)
                                .background(quoteTints[quoteLibrary.indexOf(quote) % quoteTints.size])
                                .padding(Space.L),
                            verticalArrangement = Arrangement.spacedBy(Space.M)
                        ) {
                            Text(
                                quote.text,
                                fontSize = TextSize.BodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic,
                                lineHeight = LineHeight.Relaxed
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(quote.tag, color = TextSecondary, fontSize = TextSize.BodySmall, fontWeight = FontWeight.Medium)
                                val liked = quote.text in NotesStore.likedQuotes
                                Icon(
                                    if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = if (liked) "Remove from saved" else "Save quote",
                                    tint = if (liked) Coral else TextMuted,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable { NotesStore.toggleLiked(quote.text) }
                                        .padding(Space.S)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(Space.Gutter, Space.M, Space.Gutter, Space.Gutter),
                    verticalArrangement = Arrangement.spacedBy(Space.S)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(Shapes.Card)
                                .border(2.dp, Color(0xFFE6E3DC), Shapes.Card)
                                .clickable { showAddNote = true }
                                .padding(Space.L),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.M)
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(ChipNeutral),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                stringResource(R.string.quotes_notes_write_a_new_note),
                                color = TextSecondary,
                                fontSize = TextSize.Body,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    val userNotes = NotesStore.ordered()
                    if (userNotes.isEmpty() && NotesStore.likedQuotes.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.quotes_notes_write_down_goals_outfits_that),
                                color = TextSecondary,
                                fontSize = TextSize.BodySmall,
                                lineHeight = LineHeight.Body
                            )
                        }
                    }
                    items(userNotes, key = { "note-${it.id}" }) { note ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(Shapes.Card)
                                .background(CardNeutral)
                                .clickable { selectedNote = note }
                                .padding(Space.L),
                            verticalArrangement = Arrangement.spacedBy(Space.S)
                        ) {
                            Text(
                                if (note.pinned) {
                                    "PINNED · ${note.date.format(
                                        DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
                                    ).uppercase()}"
                                } else {
                                    "NOTE · ${note.date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())).uppercase()}"
                                },
                                color = TextSecondary,
                                fontSize = TextSize.Micro,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp
                            )
                            Text(note.body, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Medium, lineHeight = LineHeight.Relaxed)
                        }
                    }
                    items(NotesStore.likedQuotes.toList(), key = { "quote-$it" }) { quote ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(Shapes.Card)
                                .background(CoralTint)
                                .padding(Space.L),
                            verticalArrangement = Arrangement.spacedBy(Space.S)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    stringResource(R.string.quotes_notes_saved_quote),
                                    color = Coral,
                                    fontSize = TextSize.Micro,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.4.sp
                                )
                                Text(
                                    stringResource(R.string.quotes_notes_remove),
                                    color = Coral,
                                    fontSize = TextSize.Caption,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { NotesStore.toggleLiked(quote) }
                                )
                            }
                            Text(
                                quote,
                                fontSize = TextSize.BodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic,
                                lineHeight = LineHeight.Relaxed
                            )
                        }
                    }
                }
            }
        }

        if (showAddNote) {
            ReMakeUDialog(
                onDismissRequest = {
                    showAddNote = false
                    newNoteText = ""
                },
                title = "Write a new note",
                secondaryText = "Cancel",
                primaryText = "Save",
                primaryEnabled = newNoteText.isNotBlank(),
                onPrimary = {
                    NotesStore.add(newNoteText)
                    showAddNote = false
                    newNoteText = ""
                },
                content = {
                    OutlinedTextField(
                        value = newNoteText,
                        onValueChange = { newNoteText = it },
                        placeholder = { Text(stringResource(R.string.quotes_notes_what_s_on_your_mind)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ink,
                            unfocusedBorderColor = TextMuted
                        )
                    )
                }
            )
        }

        selectedNote?.let { note ->
            ReMakeUDialog(
                onDismissRequest = { selectedNote = null },
                title = if (note.pinned) "Pinned note" else "Note",
                message = note.body,
                secondaryText = "Delete",
                secondaryDestructive = true,
                onSecondary = {
                    NotesStore.remove(note.id)
                    selectedNote = null
                },
                primaryText = if (note.pinned) "Unpin" else "Pin to top",
                onPrimary = {
                    NotesStore.togglePinned(note.id)
                    selectedNote = null
                }
            )
        }
    }
}
