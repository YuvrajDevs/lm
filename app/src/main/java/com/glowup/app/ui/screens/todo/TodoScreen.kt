package com.glowup.app.ui.screens.todo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.TodoBucket
import com.glowup.app.ui.data.TodoEntry
import com.glowup.app.ui.data.TodoStore
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
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
            DetailHeader(stringResource(R.string.todo_to_do)) { navController.goBack() }

            Row(
                modifier = Modifier
                    .padding(Space.Gutter, Space.M, Space.Gutter, 0.dp)
                    .fillMaxWidth()
                    .clip(Shapes.Card)
                    .background(ChipNeutral)
                    .clickable { openAddDialog(TodoBucket.TODAY) }
                    .padding(Space.L, Space.M),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.M)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.todo_add_a_to_do), color = TextSecondary, fontSize = TextSize.Body)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(Space.Gutter, Space.L, Space.Gutter, Space.Gutter)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            stringResource(R.string.common_today),
                            color = TextSecondary,
                            fontSize = TextSize.BodySmall,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp
                        )
                        if (TodoStore.items.any { it.done }) {
                            Text(
                                stringResource(R.string.todo_clear_completed),
                                color = TextSecondary,
                                fontSize = TextSize.BodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { TodoStore.clearDone() }
                            )
                        }
                    }
                    Spacer(Modifier.height(Space.S))
                }
                if (todayItems.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.todo_nothing_for_today_yet),
                            color = TextSecondary,
                            fontSize = TextSize.BodySmall,
                            modifier = Modifier.padding(bottom = Space.S)
                        )
                    }
                }
                items(todayItems, key = { it.id }) { item ->
                    TodoRow(item, onDelete = { TodoStore.remove(item.id) }) { TodoStore.toggle(item.id) }
                    Spacer(Modifier.height(Space.S))
                }
                item { Spacer(Modifier.height(Space.S)) }
                item {
                    Text(
                        stringResource(R.string.common_this_week),
                        color = TextSecondary,
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(Space.S))
                }
                if (weekItems.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.todo_nothing_for_this_week_yet),
                            color = TextSecondary,
                            fontSize = TextSize.BodySmall,
                            modifier = Modifier.padding(bottom = Space.S)
                        )
                    }
                }
                items(weekItems, key = { it.id }) { item ->
                    TodoRow(item, onDelete = { TodoStore.remove(item.id) }) { TodoStore.toggle(item.id) }
                    Spacer(Modifier.height(Space.S))
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Space.Gutter, 0.dp, Space.Gutter, Space.Gutter)
                .size(62.dp)
                .clip(CircleShape)
                .background(Ink)
                .clickable { openAddDialog(TodoBucket.TODAY) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.todo_add_to_do), tint = Color.White, modifier = Modifier.size(22.dp))
        }

        if (showAddDialog) {
            ReMakeUDialog(
                onDismissRequest = {
                    showAddDialog = false
                    newTodoText = ""
                    newTodoMeta = ""
                },
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
                            placeholder = { Text(stringResource(R.string.todo_e_g_pick_up_dry)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Ink,
                                unfocusedBorderColor = TextMuted
                            )
                        )
                        Spacer(Modifier.height(Space.M))
                        OutlinedTextField(
                            value = newTodoMeta,
                            onValueChange = { newTodoMeta = it },
                            placeholder = { Text(stringResource(R.string.todo_due_time_optional)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Ink,
                                unfocusedBorderColor = TextMuted
                            )
                        )
                        Spacer(Modifier.height(Space.M))
                        Text(stringResource(R.string.todo_when), color = TextSecondary, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(Space.S))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                            TodoBucket.entries.forEach { bucket ->
                                val selected = newTodoBucket == bucket
                                Box(
                                    modifier = Modifier
                                        .clip(Shapes.Pill)
                                        .background(if (selected) Ink else ChipNeutral)
                                        .clickable { newTodoBucket = bucket }
                                        .padding(Space.L, Space.S)
                                ) {
                                    Text(
                                        bucket.label,
                                        color = if (selected) Color.White else Ink,
                                        fontSize = TextSize.BodySmall,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun TodoRow(item: TodoEntry, onDelete: () -> Unit, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.Medium)
            .background(CardNeutral)
            .clickable { onToggle() }
            .padding(Space.M, Space.M)
            .alpha(if (item.done) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.M)
    ) {
        if (item.done) {
            Image(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(24.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(2.dp, Ink, CircleShape)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                fontSize = TextSize.BodyLarge,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (item.done) TextDecoration.LineThrough else null
            )
            item.meta?.let {
                Text(it, color = TextSecondary, fontSize = TextSize.Caption)
            }
        }
        Icon(
            Icons.Filled.Close,
            contentDescription = stringResource(R.string.common_delete),
            tint = TextMuted,
            modifier = Modifier.size(28.dp).clip(CircleShape).clickable(onClick = onDelete).padding(Space.S)
        )
    }
}
