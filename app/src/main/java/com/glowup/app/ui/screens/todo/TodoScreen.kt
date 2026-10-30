package com.glowup.app.ui.screens.todo

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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.data.TodoBucket
import com.glowup.app.ui.data.TodoEntry
import com.glowup.app.ui.data.TodoStore
import com.glowup.app.ui.theme.*

@Composable
fun TodoScreen(navController: NavHostController) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newTodoText by remember { mutableStateOf("") }
    var newTodoMeta by remember { mutableStateOf("") }
    var newTodoBucket by remember { mutableStateOf(TodoBucket.TODAY) }

    val todayItems = TodoStore.items.filter { it.bucket == TodoBucket.TODAY }
    val weekItems = TodoStore.items.filter { it.bucket == TodoBucket.WEEK }

    fun openAddDialog(bucket: TodoBucket) {
        newTodoBucket = bucket
        showAddDialog = true
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader("To-do") { navController.popBackStack() }

            Row(
                modifier = Modifier
                    .padding(24.dp, 14.dp, 24.dp, 0.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(ChipNeutral)
                    .clickable { openAddDialog(TodoBucket.TODAY) }
                    .padding(18.dp, 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Text("Add a to-do...", color = TextSecondary, fontSize = 14.sp)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 24.dp),
            ) {
                item {
                    Text("TODAY", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                    Spacer(Modifier.height(10.dp))
                }
                items(todayItems, key = { it.id }) { item ->
                    TodoRow(item) { TodoStore.toggle(item.id) }
                    Spacer(Modifier.height(8.dp))
                }
                item { Spacer(Modifier.height(10.dp)) }
                item {
                    Text("THIS WEEK", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                    Spacer(Modifier.height(10.dp))
                }
                items(weekItems, key = { it.id }) { item ->
                    TodoRow(item) { TodoStore.toggle(item.id) }
                    Spacer(Modifier.height(10.dp))
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp, 0.dp, 24.dp, 26.dp)
                .size(62.dp)
                .clip(CircleShape)
                .background(Ink)
                .clickable { openAddDialog(TodoBucket.TODAY) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add to-do", tint = Color.White, modifier = Modifier.size(22.dp))
        }

        if (showAddDialog) {
            GlowUpDialog(
                onDismissRequest = { showAddDialog = false; newTodoText = ""; newTodoMeta = "" },
                title = "Add a to-do",
                secondaryText = "Cancel",
                primaryText = "Add",
                primaryEnabled = newTodoText.isNotBlank(),
                onPrimary = {
                    TodoStore.add(newTodoText.trim(), newTodoBucket, newTodoMeta.trim())
                    showAddDialog = false
                    newTodoText = ""
                    newTodoMeta = ""
                },
                content = {
                    Column {
                        OutlinedTextField(
                            value = newTodoText,
                            onValueChange = { newTodoText = it },
                            placeholder = { Text("e.g. Pick up dry cleaning") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Ink,
                                unfocusedBorderColor = TextMuted,
                            ),
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newTodoMeta,
                            onValueChange = { newTodoMeta = it },
                            placeholder = { Text("Due time (optional)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Ink,
                                unfocusedBorderColor = TextMuted,
                            ),
                        )
                        Spacer(Modifier.height(14.dp))
                        Text("When?", color = TextSecondary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TodoBucket.entries.forEach { bucket ->
                                val selected = newTodoBucket == bucket
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (selected) Ink else ChipNeutral)
                                        .clickable { newTodoBucket = bucket }
                                        .padding(16.dp, 9.dp),
                                ) {
                                    Text(
                                        bucket.label,
                                        color = if (selected) Color.White else Ink,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun TodoRow(item: TodoEntry, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardNeutral)
            .clickable { onToggle() }
            .padding(14.dp, 12.dp)
            .alpha(if (item.done) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (item.done) {
            Image(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(24.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(2.dp, Ink, CircleShape),
            )
        }
        Column {
            Text(
                item.title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (item.done) TextDecoration.LineThrough else null,
            )
            item.meta?.let {
                Text(it, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
