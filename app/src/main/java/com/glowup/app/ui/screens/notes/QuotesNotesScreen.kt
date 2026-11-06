package com.glowup.app.ui.screens.notes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.goBack
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.data.NotesStore
import com.glowup.app.ui.data.UserNote
import com.glowup.app.ui.data.quoteLibrary
import com.glowup.app.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

private val quoteTints = listOf(CoralTint, CardNeutral, BlueTint, GreenTint, PurpleTint)
private val noteDateFormat = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

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
            DetailHeader("Quotes & Notes") { navController.goBack() }

            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp, 14.dp, 24.dp, 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Quotes" to true, "My Notes" to false).forEach { (label, isQuotes) ->
                    val selected = showQuotes == isQuotes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) Ink else ChipNeutral)
                            .clickable { showQuotes = isQuotes }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = if (selected) Color.White else Ink, fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                    }
                }
            }

            if (showQuotes) {
                LazyRow(
                    contentPadding = PaddingValues(24.dp, 12.dp, 24.dp, 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(quoteTags) { tag ->
                        val selected = tag == selectedTag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (selected) Ink else ChipNeutral)
                                .clickable { selectedTag = tag }
                                .padding(16.dp, 9.dp),
                        ) {
                            Text(tag, color = if (selected) Color.White else Ink, fontSize = 12.5.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(24.dp, 14.dp, 24.dp, 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filteredQuotes) { quote ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(quoteTints[quoteLibrary.indexOf(quote) % quoteTints.size])
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(quote.text, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic, lineHeight = 22.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(quote.tag, color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                                val liked = quote.text in NotesStore.likedQuotes
                                Icon(
                                    if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = if (liked) "Remove from saved" else "Save quote",
                                    tint = if (liked) Coral else TextMuted,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable { NotesStore.toggleLiked(quote.text) }
                                        .padding(6.dp),
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(24.dp, 14.dp, 24.dp, 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .border(2.dp, Color(0xFFE6E3DC), RoundedCornerShape(20.dp))
                                .clickable { showAddNote = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(ChipNeutral),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                            }
                            Text("Write a new note", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    val userNotes = NotesStore.ordered()
                    if (userNotes.isEmpty() && NotesStore.likedQuotes.isEmpty()) {
                        item {
                            Text(
                                "Write down goals, outfits that worked, or anything you want to remember. Quotes you heart show up here too.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                            )
                        }
                    }
                    items(userNotes, key = { "note-${it.id}" }) { note ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(CardNeutral)
                                .clickable { selectedNote = note }
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                if (note.pinned) "PINNED · ${note.date.format(noteDateFormat).uppercase()}" else "NOTE · ${note.date.format(noteDateFormat).uppercase()}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp,
                            )
                            Text(note.body, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, lineHeight = 21.sp)
                        }
                    }
                    items(NotesStore.likedQuotes.toList(), key = { "quote-$it" }) { quote ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(CoralTint)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SAVED QUOTE", color = Coral, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                                Text(
                                    "Remove",
                                    color = Coral,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { NotesStore.toggleLiked(quote) },
                                )
                            }
                            Text(quote, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic, lineHeight = 21.sp)
                        }
                    }
                }
            }
        }

        if (showAddNote) {
            GlowUpDialog(
                onDismissRequest = { showAddNote = false; newNoteText = "" },
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
                        placeholder = { Text("What's on your mind?") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ink,
                            unfocusedBorderColor = TextMuted,
                        ),
                    )
                },
            )
        }

        selectedNote?.let { note ->
            GlowUpDialog(
                onDismissRequest = { selectedNote = null },
                title = if (note.pinned) "Pinned note" else "Note",
                message = note.body,
                secondaryText = "Delete",
                secondaryDestructive = true,
                onSecondary = { NotesStore.remove(note.id); selectedNote = null },
                primaryText = if (note.pinned) "Unpin" else "Pin to top",
                onPrimary = { NotesStore.togglePinned(note.id); selectedNote = null },
            )
        }
    }
}
