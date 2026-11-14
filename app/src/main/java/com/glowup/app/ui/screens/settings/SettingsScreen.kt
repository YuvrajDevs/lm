package com.glowup.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.BuildConfig
import com.glowup.app.R
import com.glowup.app.billing.Billing
import com.glowup.app.blocker.Blocker
import com.glowup.app.ui.components.DeleteEverythingDialog
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.ReMakeUOptionRow
import com.glowup.app.ui.components.exportMyData
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.GroomingStore
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.data.WardrobeStore
import com.glowup.app.ui.data.fitness.FitnessStore
import com.glowup.app.ui.data.glow.GlowCheckStore
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

private data class SettingsRow(val label: String, val value: String? = null, val danger: Boolean = false)

private val faqs = listOf(
    "Where is my data stored?" to
        "Only on this phone. Re:MakeU has no account and no servers, and nothing is uploaded. Settings → Your data shows everything that's kept, why, and lets you export or delete any of it.",
    "How do points and streaks work?" to
        "You earn points for checking off routines, to-dos, lessons, mood check-ins, sleep logs, confidence cards and practice sessions. Any day you earn points counts towards your streak. Every 100 points is a new level.",
    "What are streak freezes?" to
        "Every 7 days in a row earns a freeze (you can bank 2, or 3 with Pro). If you miss a whole day, a banked freeze is used automatically so your streak survives. Frozen days keep the streak alive but don't add to it.",
    "How is sleep debt worked out?" to
        "From the nights you log: how far short of your sleep need you've been over the last 14 nights. The latest week counts fully, the week before counts half, and extra sleep pays debt back. It's an estimate from your logs, not a medical measurement.",
    "How do mood patterns work?" to
        "When you check in, you can tag what's been going on. Re:MakeU also knows which days you focused, did your routines, learned or slept well. Once there are at least 5 days with and 5 without something, it compares your average mood — all on your phone.",
    "Why didn't a reminder arrive?" to
        "Make sure notifications are allowed for Re:MakeU. Some phones also delay alarms to save battery — allowing exact alarms on the Sleep screen helps wake-up alarms arrive on time.",
    "Do sleep sounds need the internet?" to "No. They're generated on your phone and keep playing with the screen off until the sleep timer fades them out.",
    "What do I get with Pro?" to
        "The full 12-week programme, every fitness programme with auto-progression, strict focus and unlimited app blocking, an unlimited wardrobe with capsule and packing tools, every style guide, all speaking scenarios, every mood pattern, all sleep sounds and an extra streak freeze. Manage or cancel any time in Google Play.",
    "Is my speaking practice recorded?" to
        "No audio is ever saved. In Analyse mode your phone turns your words into text to measure pace and filler words, and only that text and the numbers are kept. Listen-back recordings are deleted as soon as you leave the screen."
)

@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    var dialog by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var restoreMessage by remember { mutableStateOf<String?>(null) }
    var nameDraft by remember { mutableStateOf(ProfileStore.name) }
    var focusDraft by remember { mutableStateOf(ProfileStore.focusAreas.toSet()) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Space.Gutter, Space.Xxl, Space.Gutter, 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Ink)
                        .clickable { navController.goBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    stringResource(R.string.settings_settings),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = TextSize.TitleLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(48.dp))
            }

            Row(
                modifier = Modifier
                    .padding(Space.Gutter, Space.M, Space.Gutter, 0.dp)
                    .fillMaxWidth()
                    .clip(Shapes.Large)
                    .background(Ink)
                    .clickable {
                        nameDraft = ProfileStore.name
                        dialog = "Name"
                    }
                    .padding(Space.L, Space.L),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.M)
            ) {
                Image(painterResource(R.drawable.ic_profile), contentDescription = null, modifier = Modifier.size(46.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(ProfileStore.name.ifBlank { "Your profile" }, color = Color.White, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                    Text(
                        (if (ProStore.isPro) stringResource(R.string.settings_re_makeu_pro) else "") +
                            "Level ${ProgressStore.level} · ${ProgressStore.points} points",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = TextSize.BodySmall
                    )
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f).testTag("settings_list"),
                contentPadding = PaddingValues(Space.Gutter, Space.L, Space.Gutter, Space.Section)
            ) {
                settingsSection(
                    "Profile",
                    listOf(
                        SettingsRow("Name", ProfileStore.name.ifBlank { null }),
                        SettingsRow("Age", ProfileStore.ageBand ?: "Not set"),
                        SettingsRow("Face shape", ProfileStore.faceShape ?: "Not set"),
                        SettingsRow("Focus areas", ProfileStore.focusAreas.size.takeIf { it > 0 }?.let { "$it selected" } ?: "None")
                    ),
                    onRowClick = { label ->
                        nameDraft = ProfileStore.name
                        focusDraft = ProfileStore.focusAreas.toSet()
                        dialog = label
                    }
                )
                settingsSection(
                    "Membership",
                    listOfNotNull(
                        SettingsRow(if (ProStore.isPro) "Your Pro plan" else "Upgrade to Pro", if (ProStore.isPro) "Active" else null),
                        SettingsRow("Restore purchases"),
                        if (BuildConfig.DEBUG) SettingsRow("Developer: simulate Pro", if (ProStore.debugOverride) "On" else "Off") else null
                    ),
                    onRowClick = { label ->
                        when (label) {
                            "Restore purchases" -> {
                                restoreMessage = "Checking Google Play…"
                                Billing.refreshPurchases { found ->
                                    restoreMessage = if (found) "Pro restored — everything is unlocked." else "No Pro purchase found on this Google account."
                                }
                            }
                            "Developer: simulate Pro" -> ProStore.updateDebugOverride(!ProStore.debugOverride)
                            else -> ProStore.showPaywall()
                        }
                    }
                )
                settingsSection(
                    "Your plans",
                    listOf(
                        SettingsRow("Glow Check", GlowCheckStore.daysUntilRescan()?.let { if (it == 0) "Rescan due" else "Rescan in $it d" } ?: "Not taken"),
                        SettingsRow("Fitness plan", if (FitnessStore.setUp) FitnessStore.goal?.label else "Not set up"),
                        SettingsRow("App blocker", if (Blocker.ready(context)) "On" else "Off"),
                        SettingsRow("My wardrobe", "${WardrobeStore.items.size} items")
                    ),
                    onRowClick = { label ->
                        navController.navigate(
                            when (label) {
                                "Glow Check" -> if (GlowCheckStore.latest == null) "glow_check" else "glow_results"
                                "Fitness plan" -> if (FitnessStore.setUp) "fitness_setup" else "fitness"
                                "App blocker" -> "blocker"
                                else -> "wardrobe"
                            }
                        )
                    }
                )
                settingsSection(
                    "App",
                    listOf(
                        SettingsRow("Reminders"),
                        SettingsRow("Sleep & alarms"),
                        SettingsRow("System notification settings")
                    ),
                    onRowClick = { label ->
                        when (label) {
                            "Reminders" -> navController.navigate("notifications")
                            "Sleep & alarms" -> navController.navigate("sleep")
                            else -> context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            )
                        }
                    }
                )
                settingsSection(
                    "Privacy & data",
                    listOf(
                        SettingsRow("Your data", "On this phone only"),
                        SettingsRow("Export my data"),
                        SettingsRow("Delete all my data", danger = true)
                    ),
                    onRowClick = { label ->
                        when (label) {
                            "Your data" -> navController.navigate("your_data")
                            "Export my data" -> exportMyData(context)
                            else -> showDeleteConfirm = true
                        }
                    }
                )
                settingsSection(
                    "Support",
                    listOf(
                        SettingsRow("Help & FAQ"),
                        SettingsRow("Rate Re:MakeU"),
                        SettingsRow("Version", versionName(context))
                    ),
                    onRowClick = { label ->
                        when (label) {
                            "Help & FAQ" -> dialog = label
                            "Rate Re:MakeU" -> openStoreListing(context)
                        }
                    },
                    last = true
                )
            }
        }

        when (dialog) {
            "Name" -> ReMakeUDialog(
                onDismissRequest = { dialog = null },
                title = "Your name",
                secondaryText = "Cancel",
                primaryText = "Save",
                primaryEnabled = nameDraft.isNotBlank(),
                onPrimary = {
                    ProfileStore.updateName(nameDraft)
                    dialog = null
                },
                content = {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it.take(40) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted)
                    )
                }
            )
            "Age" -> PickerDialog(
                title = "Your age",
                options = ProfileStore.ageBands,
                selected = ProfileStore.ageBand ?: "",
                onSelect = {
                    ProfileStore.updateAgeBand(it)
                    dialog = null
                },
                onDismiss = { dialog = null }
            )
            "Face shape" -> PickerDialog(
                title = "Face shape",
                options = listOf("Not set") + GroomingStore.faceShapes,
                selected = ProfileStore.faceShape ?: "Not set",
                onSelect = {
                    ProfileStore.updateFaceShape(it.takeIf { shape -> shape != "Not set" })
                    dialog = null
                },
                onDismiss = { dialog = null }
            )
            "Focus areas" -> ReMakeUDialog(
                onDismissRequest = { dialog = null },
                title = "Focus areas",
                secondaryText = "Cancel",
                primaryText = "Save",
                primaryEnabled = focusDraft.isNotEmpty(),
                onPrimary = {
                    ProfileStore.updateFocus(ProfileStore.focusOptions.filter { it in focusDraft })
                    dialog = null
                },
                content = {
                    Column {
                        ProfileStore.focusOptions.forEach { option ->
                            val checked = option in focusDraft
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(Shapes.Small)
                                    .clickable { focusDraft = if (checked) focusDraft - option else focusDraft + option }
                                    .padding(vertical = Space.S, horizontal = Space.Xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = Ink, uncheckedColor = TextMuted)
                                )
                                Spacer(Modifier.width(Space.S))
                                Text(option, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            )
            "Help & FAQ" -> ReMakeUDialog(
                onDismissRequest = { dialog = null },
                title = "Help & FAQ",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column(
                        modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(Space.M)
                    ) {
                        faqs.forEach { (question, answer) ->
                            Column {
                                Text(question, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(Space.Xs))
                                Text(answer, color = TextSecondary, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
                            }
                        }
                    }
                }
            )
        }

        restoreMessage?.let { text ->
            ReMakeUDialog(
                onDismissRequest = { restoreMessage = null },
                title = "Restore purchases",
                message = text,
                primaryText = "OK",
                secondaryText = null,
                onPrimary = { restoreMessage = null }
            )
        }

        if (showDeleteConfirm) DeleteEverythingDialog(navController) { showDeleteConfirm = false }
    }
}

private fun openStoreListing(context: Context) {
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
    val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
    runCatching { context.startActivity(market) }.onFailure { runCatching { context.startActivity(web) } }
}

private fun versionName(context: Context): String = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: ""

@Composable
private fun PickerDialog(title: String, options: List<String>, selected: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    ReMakeUDialog(
        onDismissRequest = onDismiss,
        title = title,
        primaryText = "Close",
        secondaryText = null,
        onPrimary = onDismiss,
        content = {
            Column {
                options.forEach { option ->
                    ReMakeUOptionRow(option, selected = option == selected) { onSelect(option) }
                }
            }
        }
    )
}

private fun LazyListScope.settingsSection(title: String, rows: List<SettingsRow>, onRowClick: (String) -> Unit, last: Boolean = false) {
    item {
        Text(
            title.uppercase(),
            color = TextSecondary,
            fontSize = TextSize.BodySmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
            modifier = Modifier.padding(bottom = Space.S)
        )
    }
    items(rows) { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Space.S)
                .clip(Shapes.Medium)
                .background(if (row.danger) MaroonTint else CardNeutral)
                .clickable { onRowClick(row.label) }
                .padding(Space.L, Space.M),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                row.label,
                fontSize = TextSize.BodyLarge,
                fontWeight = if (row.danger) FontWeight.SemiBold else FontWeight.Medium,
                color = if (row.danger) Maroon else Ink,
                modifier = Modifier.weight(1f)
            )
            row.value?.let {
                Text(it, fontSize = TextSize.BodySmall, color = TextSecondary, modifier = Modifier.padding(end = Space.S))
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = if (row.danger) Maroon else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
    item { Spacer(Modifier.height(if (last) 0.dp else Space.S)) }
}
