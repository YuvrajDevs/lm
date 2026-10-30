package com.glowup.app.ui.screens.notes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.theme.*

private data class Quote(val text: String, val tag: String, val tint: Color, val hasHeartIcon: Boolean = false)

private val quotes = listOf(
    Quote("\"Small, boring habits — done daily — beat big plans done never.\"", "Habits", CoralTint, true),
    Quote("\"Confidence is a skill you practise, not a trait you're born with.\"", "Confidence", CardNeutral),
    Quote("\"Dress for the day you want, not just the one you're having.\"", "Style", BlueTint),
    Quote("\"Sleep is the easiest glow-up most people skip.\"", "Sleep", GreenTint),
)

private data class Note(val label: String, val body: String, val tint: Color, val labelColor: Color, val italic: Boolean = false)

private val notes = listOf(
    Note("GOAL · PINNED", "Feel confident enough to speak up in team meetings by December.", CardNeutral, TextSecondary),
    Note("NOTE · OCT 24", "Linen shirt + chinos combo worked really well for the Saturday brunch — repeat for similar occasions.", CardNeutral, TextSecondary),
    Note("SAVED QUOTE", "\"Small, boring habits — done daily — beat big plans done never.\"", CoralTint, Coral, italic = true),
)

@Composable
fun QuotesNotesScreen(navController: NavHostController) {
    var showQuotes by remember { mutableStateOf(true) }
    var likedQuotes by remember { mutableStateOf(quotes.filter { it.hasHeartIcon }.map { it.text }.toSet()) }
    val noteItems = remember { notes.toMutableStateList() }
    var showAddNote by remember { mutableStateOf(false) }
    var newNoteText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("Quotes & Notes") { navController.popBackStack() }

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
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(24.dp, 14.dp, 24.dp, 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(quotes) { quote ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(quote.tint)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(quote.text, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic, lineHeight = 22.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(quote.tag, color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                                val liked = quote.text in likedQuotes
                                Icon(
                                    if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (liked) Coral else TextMuted,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            likedQuotes = if (liked) likedQuotes - quote.text else likedQuotes + quote.text
                                        },
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
                    items(noteItems) { note ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(note.tint)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(note.label, color = note.labelColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                            Text(
                                note.body,
                                fontSize = 14.5.sp,
                                fontWeight = if (note.italic) FontWeight.SemiBold else FontWeight.Medium,
                                fontStyle = if (note.italic) FontStyle.Italic else FontStyle.Normal,
                                lineHeight = 21.sp,
                            )
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
                    noteItems.add(0, Note("NOTE · TODAY", newNoteText.trim(), CardNeutral, TextSecondary))
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
    }
}
