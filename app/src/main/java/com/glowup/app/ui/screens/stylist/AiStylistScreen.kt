package com.glowup.app.ui.screens.stylist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.theme.*

private data class ChatMessage(val fromUser: Boolean, val text: String, val note: String? = null)

private val chatMessages = listOf(
    ChatMessage(false, "Hey Arjun! Tell me what you're dressing for and I'll work with what's in your wardrobe."),
    ChatMessage(true, "What should I wear for a first date at a casual restaurant?"),
    ChatMessage(
        false,
        "Your navy linen shirt with the beige chinos — smart but relaxed, and breathable for Mumbai humidity. Add your leather watch to pull it together.",
        "Used: your wardrobe, budget, Mumbai weather",
    ),
    ChatMessage(true, "Does it suit my face shape?"),
    ChatMessage(false, "I can help with outfits, not ratings or face scores — that's not something I do here. Want fit or colour tips instead?"),
)

private fun stylistReplyFor(question: String): String {
    val q = question.lowercase()
    return when {
        "jeans" in q -> "Black jeans pair well with your white sneakers and a plain tee, or your navy shirt for a smarter look."
        "office" in q -> "Try your linen shirt with chinos — breathable, smart, and already in your wardrobe."
        "face" in q || "rating" in q || "score" in q -> "I can help with outfits, not ratings or face scores — that's not something I do here. Want fit or colour tips instead?"
        else -> "Based on what's in your wardrobe, I'd go with your navy linen shirt and beige chinos — smart but relaxed for most occasions."
    }
}

@Composable
fun AiStylistScreen(navController: NavHostController) {
    var input by remember { mutableStateOf("") }
    val messages = remember { chatMessages.toMutableStateList() }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun send(text: String) {
        if (text.isBlank()) return
        messages.add(ChatMessage(true, text.trim()))
        messages.add(ChatMessage(false, stylistReplyFor(text)))
        input = ""
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp, 24.dp, 24.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AI Stylist", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("7 of 10 messages today", color = TextSecondary, fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.size(48.dp))
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(messages) { msg ->
                    if (msg.fromUser) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                                    .background(Ink)
                                    .padding(14.dp, 12.dp),
                            ) {
                                Text(msg.text, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Image(painterResource(R.drawable.ic_area_style), contentDescription = null, modifier = Modifier.size(30.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 260.dp)
                                        .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                                        .background(ChipNeutral)
                                        .padding(14.dp, 12.dp),
                                ) {
                                    Text(msg.text, fontSize = 14.sp, lineHeight = 20.sp)
                                }
                                msg.note?.let {
                                    Text(it, color = TextMuted, fontSize = 10.5.sp, modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                        }
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 10.dp),
            ) {
                items(listOf("Office outfit ideas", "What goes with black jeans?")) { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(ChipNeutral)
                            .clickable { send(chip) }
                            .padding(14.dp, 9.dp),
                    ) {
                        Text(chip, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 0.dp, 20.dp, 28.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(CardNeutral)
                    .padding(6.dp, 6.dp, 6.dp, 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask your stylist...", color = TextSecondary, fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Ink)
                        .clickable { send(input) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
