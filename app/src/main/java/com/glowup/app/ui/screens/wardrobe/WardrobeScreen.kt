package com.glowup.app.ui.screens.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.theme.*

private data class WardrobeItem(val category: String, val title: String, val meta: String, val tint: Color, val accent: Color)

private val initialItems = listOf(
    WardrobeItem("Top", "Linen shirt", "Blue · Office, Casual", BlueTint, Navy),
    WardrobeItem("Bottom", "Beige chinos", "Beige · Office, Date", CoralTint, Coral),
    WardrobeItem("Shoes", "White sneakers", "White · Daily, College", GreenTint, Green),
    WardrobeItem("Top", "Black tee", "Black · Daily, Casual", PurpleTint, Purple),
    WardrobeItem("Accessory", "Leather watch", "Brown · Office, Date", MaroonTint, Maroon),
)

private val tints = listOf(BlueTint to Navy, CoralTint to Coral, GreenTint to Green, PurpleTint to Purple, MaroonTint to Maroon)

@Composable
fun WardrobeScreen(navController: NavHostController) {
    val items = remember { initialItems.toMutableStateList() }
    var selectedItem by remember { mutableStateOf<WardrobeItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var newMeta by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("My wardrobe") { navController.popBackStack() }

            LazyRow(
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(listOf("All · ${items.size}", "Tops", "Bottoms", "Shoes", "Accessories")) { chip ->
                    val selected = chip.startsWith("All")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) Ink else ChipNeutral)
                            .padding(18.dp, 10.dp),
                    ) {
                        Text(chip, color = if (selected) Color.White else Ink, fontSize = 13.5.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(item.tint)
                            .clickable { selectedItem = item }
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(item.category.uppercase(), color = item.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Column {
                            Text(item.title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(item.meta, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .border(2.dp, Color(0xFFE6E3DC), RoundedCornerShape(22.dp))
                            .clickable { showAddDialog = true }
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(ChipNeutral),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Add item", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Text(
                "Your AI stylist and outfit ideas use this automatically.",
                color = TextMuted,
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(24.dp, 0.dp, 24.dp, 20.dp),
            )
        }

        selectedItem?.let { item ->
            GlowUpDialog(
                onDismissRequest = { selectedItem = null },
                title = item.title,
                message = "${item.category} · ${item.meta}",
                secondaryText = "Remove",
                secondaryDestructive = true,
                onSecondary = { items.remove(item); selectedItem = null },
                primaryText = "Close",
            )
        }

        if (showAddDialog) {
            GlowUpDialog(
                onDismissRequest = { showAddDialog = false; newTitle = ""; newCategory = ""; newMeta = "" },
                title = "Add item",
                secondaryText = "Cancel",
                primaryText = "Add",
                primaryEnabled = newTitle.isNotBlank(),
                onPrimary = {
                    val (tint, accent) = tints[items.size % tints.size]
                    items.add(
                        WardrobeItem(
                            newCategory.ifBlank { "Item" },
                            newTitle.trim(),
                            newMeta.ifBlank { "Added just now" },
                            tint,
                            accent,
                        ),
                    )
                    showAddDialog = false
                    newTitle = ""
                    newCategory = ""
                    newMeta = ""
                },
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("e.g. Grey hoodie") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                        )
                        OutlinedTextField(
                            value = newCategory,
                            onValueChange = { newCategory = it },
                            placeholder = { Text("Category, e.g. Top") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                        )
                        OutlinedTextField(
                            value = newMeta,
                            onValueChange = { newMeta = it },
                            placeholder = { Text("e.g. Grey · Casual") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                        )
                    }
                },
            )
        }
    }
}
