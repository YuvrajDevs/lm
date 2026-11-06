package com.glowup.app.ui.screens.settings

import com.glowup.app.BuildConfig
import com.glowup.app.billing.Billing
import com.glowup.app.ui.data.ProStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.goBack
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.glowup.app.R
import com.glowup.app.audio.SleepSoundService
import com.glowup.app.ui.components.GlowUpDialog
import com.glowup.app.ui.components.GlowUpOptionRow
import com.glowup.app.ui.data.GroomingStore
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.ProgressStore
import com.glowup.app.ui.data.Storage
import com.glowup.app.ui.data.Stores
import com.glowup.app.ui.theme.*
import java.io.File
import java.time.LocalDate

private data class SettingsRow(val label: String, val value: String? = null, val danger: Boolean = false)

private val faqs = listOf(
    "Where is my data stored?" to "Everything — routines, notes, moods, wardrobe and progress — is saved only on this phone. Nothing is uploaded.",
    "How do points and streaks work?" to "You earn points for checking off routines, to-dos, lessons, mood check-ins, confidence cards and practice sessions. Any day you earn points counts towards your streak. Every 100 points is a new level.",
    "Why didn't a reminder arrive?" to "Make sure notifications are allowed for Glow-Up. Some phones also delay alarms to save battery — allowing exact alarms on the Sleep screen helps wake-up alarms arrive on time.",
    "Do sleep sounds need the internet?" to "No. They're generated on your phone and keep playing with the screen off until the sleep timer fades them out.",
    "What do I get with Pro?" to "The full 12-week programme, every style guide and outfit breakdown, every lesson and guide, and all sleep sounds and morning sessions. Manage or cancel any time in Google Play.",
    "Is my speaking practice recorded?" to "Only while you practise, so you can listen back. The recording is deleted as soon as you leave the screen.",
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
                modifier = Modifier.fillMaxWidth().padding(24.dp, 32.dp, 24.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Ink)
                        .clickable { navController.goBack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.size(48.dp))
            }

            Row(
                modifier = Modifier
                    .padding(24.dp, 14.dp, 24.dp, 0.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Ink)
                    .clickable { nameDraft = ProfileStore.name; dialog = "Name" }
                    .padding(16.dp, 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Image(painterResource(R.drawable.ic_profile), contentDescription = null, modifier = Modifier.size(46.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(ProfileStore.name.ifBlank { "Your profile" }, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text((if (ProStore.isPro) "Glow-Up Pro · " else "") + "Level ${ProgressStore.level} · ${ProgressStore.points} points", color = Color.White.copy(alpha = 0.5f), fontSize = 12.5.sp)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 28.dp),
            ) {
                settingsSection(
                    "Profile",
                    listOf(
                        SettingsRow("Name", ProfileStore.name.ifBlank { null }),
                        SettingsRow("Face shape", ProfileStore.faceShape ?: "Not set"),
                        SettingsRow("Focus areas", ProfileStore.focusAreas.size.takeIf { it > 0 }?.let { "$it selected" } ?: "None"),
                    ),
                    onRowClick = { label ->
                        nameDraft = ProfileStore.name
                        focusDraft = ProfileStore.focusAreas.toSet()
                        dialog = label
                    },
                )
                settingsSection(
                    "Glow-Up Pro",
                    listOfNotNull(
                        SettingsRow(if (ProStore.isPro) "Your Pro plan" else "Upgrade to Pro", if (ProStore.isPro) "Active" else null),
                        SettingsRow("Restore purchases"),
                        if (BuildConfig.DEBUG) SettingsRow("Developer: simulate Pro", if (ProStore.debugOverride) "On" else "Off") else null,
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
                    },
                )
                settingsSection(
                    "App",
                    listOf(
                        SettingsRow("Reminders"),
                        SettingsRow("Sleep & alarms"),
                        SettingsRow("System notification settings"),
                    ),
                    onRowClick = { label ->
                        when (label) {
                            "Reminders" -> navController.navigate("notifications")
                            "Sleep & alarms" -> navController.navigate("sleep")
                            else -> context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                            )
                        }
                    },
                )
                settingsSection(
                    "Privacy & data",
                    listOf(
                        SettingsRow("Export my data"),
                        SettingsRow("Delete all my data", danger = true),
                    ),
                    onRowClick = { label ->
                        if (label == "Export my data") exportData(context) else showDeleteConfirm = true
                    },
                )
                settingsSection(
                    "Support",
                    listOf(
                        SettingsRow("Help & FAQ"),
                        SettingsRow("Rate Glow-Up"),
                        SettingsRow("Version", versionName(context)),
                    ),
                    onRowClick = { label ->
                        when (label) {
                            "Help & FAQ" -> dialog = label
                            "Rate Glow-Up" -> openStoreListing(context)
                        }
                    },
                    last = true,
                )
            }
        }

        when (dialog) {
            "Name" -> GlowUpDialog(
                onDismissRequest = { dialog = null },
                title = "Your name",
                secondaryText = "Cancel",
                primaryText = "Save",
                primaryEnabled = nameDraft.isNotBlank(),
                onPrimary = { ProfileStore.updateName(nameDraft); dialog = null },
                content = {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it.take(40) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                    )
                },
            )
            "Face shape" -> PickerDialog(
                title = "Face shape",
                options = listOf("Not set") + GroomingStore.faceShapes,
                selected = ProfileStore.faceShape ?: "Not set",
                onSelect = { ProfileStore.updateFaceShape(it.takeIf { shape -> shape != "Not set" }); dialog = null },
                onDismiss = { dialog = null },
            )
            "Focus areas" -> GlowUpDialog(
                onDismissRequest = { dialog = null },
                title = "Focus areas",
                secondaryText = "Cancel",
                primaryText = "Save",
                primaryEnabled = focusDraft.isNotEmpty(),
                onPrimary = { ProfileStore.updateFocus(ProfileStore.focusOptions.filter { it in focusDraft }); dialog = null },
                content = {
                    Column {
                        ProfileStore.focusOptions.forEach { option ->
                            val checked = option in focusDraft
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { focusDraft = if (checked) focusDraft - option else focusDraft + option }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = Ink, uncheckedColor = TextMuted),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(option, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                },
            )
            "Help & FAQ" -> GlowUpDialog(
                onDismissRequest = { dialog = null },
                title = "Help & FAQ",
                primaryText = "Close",
                secondaryText = null,
                content = {
                    Column(
                        modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        faqs.forEach { (question, answer) ->
                            Column {
                                Text(question, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text(answer, color = TextSecondary, fontSize = 13.5.sp, lineHeight = 19.sp)
                            }
                        }
                    }
                },
            )
        }

        restoreMessage?.let { text ->
            GlowUpDialog(
                onDismissRequest = { restoreMessage = null },
                title = "Restore purchases",
                message = text,
                primaryText = "OK",
                secondaryText = null,
                onPrimary = { restoreMessage = null },
            )
        }

        if (showDeleteConfirm) {
            GlowUpDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = "Delete all your data?",
                message = "This permanently deletes your profile, routines, notes, moods, wardrobe, reminders and progress from this phone. This can't be undone.",
                secondaryText = "Cancel",
                primaryText = "Delete",
                primaryDestructive = true,
                onPrimary = {
                    showDeleteConfirm = false
                    SleepSoundService.stop(context)
                    Stores.deleteAll()
                    Billing.refreshPurchases()
                    navController.navigate("onboarding") { popUpTo(navController.graph.id) { inclusive = true } }
                },
            )
        }
    }
}

private fun exportData(context: Context) {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "glowup-data-${LocalDate.now()}.json")
    file.writeText(Storage.exportJson())
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val share = Intent(Intent.ACTION_SEND)
        .setType("application/json")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .putExtra(Intent.EXTRA_SUBJECT, "My Glow-Up data")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(share, "Export my data"))
}

private fun openStoreListing(context: Context) {
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
    val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
    runCatching { context.startActivity(market) }.onFailure { runCatching { context.startActivity(web) } }
}

private fun versionName(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: ""

@Composable
private fun PickerDialog(title: String, options: List<String>, selected: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    GlowUpDialog(
        onDismissRequest = onDismiss,
        title = title,
        primaryText = "Close",
        secondaryText = null,
        onPrimary = onDismiss,
        content = {
            Column {
                options.forEach { option ->
                    GlowUpOptionRow(option, selected = option == selected) { onSelect(option) }
                }
            }
        },
    )
}

private fun androidx.compose.foundation.lazy.LazyListScope.settingsSection(
    title: String,
    rows: List<SettingsRow>,
    onRowClick: (String) -> Unit,
    last: Boolean = false,
) {
    item {
        Text(
            title.uppercase(),
            color = TextSecondary,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
            modifier = Modifier.padding(bottom = 10.dp),
        )
    }
    items(rows) { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (row.danger) MaroonTint else CardNeutral)
                .clickable { onRowClick(row.label) }
                .padding(15.dp, 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                row.label,
                fontSize = 14.5.sp,
                fontWeight = if (row.danger) FontWeight.SemiBold else FontWeight.Medium,
                color = if (row.danger) Maroon else Ink,
                modifier = Modifier.weight(1f),
            )
            row.value?.let {
                Text(it, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(end = 6.dp))
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = if (row.danger) Maroon else TextSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
    item { Spacer(Modifier.height(if (last) 0.dp else 10.dp)) }
}
