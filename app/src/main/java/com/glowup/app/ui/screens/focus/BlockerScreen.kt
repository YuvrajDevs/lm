package com.glowup.app.ui.screens.focus

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.glowup.app.blocker.Blocker
import com.glowup.app.blocker.InstalledApp
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.BlockStore
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Sizes
import com.glowup.app.ui.design.TextSize
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.glowup.app.ui.data.DayStats
import java.time.LocalDate

private enum class ListKind(val title: String, val sub: String) {
    FOCUS("Blocked during focus", "Opening these while you focus shows the block screen."),
    PAUSE("Pause before opening", "A 5-second breath and a choice, every time."),
    SCHEDULE("Blocked on a schedule", "Blocked during the times you set below."),
}

@Composable
fun BlockerScreen(navController: NavHostController) {
    val context = LocalContext.current
    var usage by remember { mutableStateOf(Blocker.hasUsageAccess(context)) }
    var overlay by remember { mutableStateOf(Blocker.canOverlay(context)) }
    var consent by remember { mutableStateOf(ConsentStore.has(Consent.APP_USAGE)) }
    var askConsent by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf<ListKind?>(null) }
    var addSchedule by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        usage = Blocker.hasUsageAccess(context)
        overlay = Blocker.canOverlay(context)
        Blocker.sync(context)
        onPauseOrDispose { }
    }
    val ready = consent && usage && overlay

    picking?.let { kind ->
        AppPicker(kind) { picking = null; Blocker.sync(context) }
        return
    }

    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader("App blocker") { navController.goBack() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 16.dp, 24.dp, 28.dp)) {
                if (!ready) {
                    Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).padding(20.dp)) {
                        Text("Set up in 3 taps", color = Color.White, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold)
                        Text("Android needs your OK before any app can step in when you open another.", color = Color.White.copy(alpha = 0.65f), fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(14.dp))
                        SetupStep(1, "Agree to how it works", consent) { askConsent = true }
                        SetupStep(2, "Allow usage access", usage) {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                        SetupStep(3, "Allow display over other apps", overlay) {
                            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Re:MakeU only sees the name of the app on screen — never what you do in it.", color = Mint, fontSize = TextSize.Caption, lineHeight = LineHeight.Tight)
                    }
                } else {
                    val today = BlockStore.stats[LocalDate.now().toString()] ?: DayStats()
                    val week = BlockStore.weekTotals()
                    Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).padding(20.dp)) {
                        Text("THIS WEEK", color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
                        Text("~${BlockStore.minutesSaved(week)} min won back", color = Color.White, fontSize = TextSize.Headline, fontWeight = FontWeight.Bold)
                        Text("${week.blocks} blocks · ${week.resisted} times you chose not to open · today ${today.pauses} pauses", color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
                        Text("Estimate based on ${BlockStore.MINUTES_PER_RESISTED_OPEN} minutes per skipped open.", color = Color.White.copy(alpha = 0.4f), fontSize = TextSize.Micro, modifier = Modifier.padding(top = 6.dp))
                    }
                }

                Spacer(Modifier.height(18.dp))
                ListCard(Icons.Outlined.Block, ListKind.FOCUS, BlockStore.focusApps, PurpleTint, Purple) { picking = ListKind.FOCUS }
                ListCard(Icons.Outlined.SelfImprovement, ListKind.PAUSE, BlockStore.pauseApps, BlueTint, Navy) { picking = ListKind.PAUSE }
                ListCard(Icons.Outlined.Schedule, ListKind.SCHEDULE, BlockStore.scheduleApps, CoralTint, Coral) { picking = ListKind.SCHEDULE }

                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Schedules", fontSize = TextSize.Title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Row(
                        Modifier.clip(Shapes.Pill).background(ChipNeutral).clickable {
                            if (ProStore.isPro || BlockStore.schedules.isEmpty()) addSchedule = true else ProStore.showPaywall("More than one blocking schedule is part of Pro")
                        }.padding(12.dp, 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Add", fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (BlockStore.schedules.isEmpty()) {
                    Text("e.g. block social apps on weekdays 9–5, or after 11 pm so you sleep.", color = TextSecondary, fontSize = TextSize.BodySmall)
                }
                BlockStore.schedules.forEach { s ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(Shapes.Card).background(CardNeutral).padding(16.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(s.label, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${formatClock(s.start)}–${formatClock(s.end)} · " + s.days.sorted().joinToString(" ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2) },
                                color = TextSecondary, fontSize = TextSize.Caption,
                            )
                        }
                        Switch(s.enabled, { BlockStore.setScheduleEnabled(s.id, it); Blocker.sync(context) }, colors = SwitchDefaults.colors(checkedTrackColor = Ink, checkedThumbColor = Color.White))
                        Box(Modifier.size(36.dp).clip(CircleShape).clickable { BlockStore.removeSchedule(s.id); Blocker.sync(context) }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete schedule", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Pause length", fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 5, 10, 15).forEach { s -> ChoicePill("$s s", BlockStore.pauseSeconds == s, Modifier.weight(1f)) { BlockStore.updatePauseSeconds(s) } }
                }
                if (!ProStore.isPro) {
                    Text("Free: up to ${BlockStore.FREE_MAX_APPS} apps per list and one schedule. Pro: unlimited, plus strict focus.", color = TextSecondary, fontSize = TextSize.Caption, lineHeight = LineHeight.Compact, modifier = Modifier.padding(top = 14.dp))
                }
            }
        }
    }

    if (askConsent) ConsentSheet(Consent.APP_USAGE, Icons.Outlined.Block, onAllow = { askConsent = false; consent = true }, onDecline = { askConsent = false })
    if (addSchedule) ScheduleDialog { addSchedule = false; Blocker.sync(context) }
}

@Composable
private fun SetupStep(n: Int, title: String, done: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(Shapes.Medium).background(Color.White.copy(alpha = 0.08f)).clickable(enabled = !done, onClick = onClick).padding(14.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(26.dp).clip(CircleShape).background(if (done) Mint else Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            if (done) Icon(Icons.Filled.Check, contentDescription = "Done", tint = Ink, modifier = Modifier.size(15.dp))
            else Text("$n", color = Color.White, fontSize = TextSize.Caption, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text(title, color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (!done) Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
private fun ListCard(icon: ImageVector, kind: ListKind, apps: List<String>, tint: Color, accent: Color, onClick: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(Shapes.Card).background(tint).clickable(onClick = onClick).padding(18.dp, 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(kind.title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                Text(kind.sub, color = TextSecondary, fontSize = TextSize.Caption)
            }
            Text(if (apps.isEmpty()) "Choose" else "${apps.size}", color = accent, fontSize = TextSize.BodySmall, fontWeight = FontWeight.Bold)
        }
        if (apps.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                apps.take(6).forEach { pkg ->
                    val bmp = remember(pkg) { Blocker.icon(context, pkg) }
                    Box(Modifier.size(34.dp).clip(Shapes.Small).background(Color.White), contentAlignment = Alignment.Center) {
                        bmp?.let { Image(it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(26.dp)) }
                    }
                }
                if (apps.size > 6) Text("+${apps.size - 6}", fontSize = TextSize.Caption, fontWeight = FontWeight.Bold, color = accent, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
    }
}

@Composable
private fun AppPicker(kind: ListKind, onDone: () -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val apps by produceState(emptyList<InstalledApp>()) { value = withContext(Dispatchers.IO) { Blocker.launchableApps(context) } }
    val list = when (kind) { ListKind.FOCUS -> BlockStore.focusApps; ListKind.PAUSE -> BlockStore.pauseApps; ListKind.SCHEDULE -> BlockStore.scheduleApps }
    val suggested = setOf("com.instagram.android", "com.zhiliaoapp.musically", "com.google.android.youtube", "com.snapchat.android", "com.twitter.android", "com.facebook.katana", "com.reddit.frontpage", "in.mohalla.sharechat", "com.netflix.mediaclient")
    BackHandler(onBack = onDone)
    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader(kind.title, onDone)
        OutlinedTextField(
            value = query, onValueChange = { query = it }, singleLine = true,
            placeholder = { Text("Search apps") }, leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            shape = Shapes.Pill,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = Color.Transparent, unfocusedContainerColor = CardNeutral, focusedContainerColor = CardNeutral),
            modifier = Modifier.fillMaxWidth().padding(24.dp, 14.dp, 24.dp, 6.dp),
        )
        // Order is fixed when the picker opens, so rows don't jump as you tick them.
        val initial = remember { list.toSet() }
        val ordered = remember(apps) { apps.sortedWith(compareBy({ it.packageName !in initial }, { it.packageName !in suggested }, { it.label.lowercase() })) }
        val shown = ordered.filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(24.dp, 8.dp, 24.dp, 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (apps.isEmpty()) item { Text("Loading apps…", color = TextSecondary, modifier = Modifier.padding(8.dp)) }
            items(shown, key = { it.packageName }) { app ->
                val on = app.packageName in list
                val bmp = remember(app.packageName) { Blocker.icon(context, app.packageName) }
                Row(
                    Modifier.fillMaxWidth().clip(Shapes.Medium).background(if (on) ChipNeutral else Color.Transparent).clickable {
                        val ok = when (kind) {
                            ListKind.FOCUS -> BlockStore.toggleFocusApp(app.packageName)
                            ListKind.PAUSE -> BlockStore.togglePauseApp(app.packageName)
                            ListKind.SCHEDULE -> BlockStore.toggleScheduleApp(app.packageName)
                        }
                        if (!ok) ProStore.showPaywall("Block more than ${BlockStore.FREE_MAX_APPS} apps with Pro")
                    }.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { bmp?.let { Image(it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(36.dp)) } }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(app.label, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Medium)
                        if (app.packageName in suggested) Text("Common distraction", color = TextSecondary, fontSize = TextSize.Caption)
                    }
                    Box(Modifier.size(24.dp).clip(CircleShape).background(if (on) Ink else ChipNeutral), contentAlignment = Alignment.Center) {
                        if (on) Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
        Box(Modifier.padding(24.dp, 8.dp, 24.dp, 24.dp)) {
            Box(Modifier.fillMaxWidth().height(Sizes.Button).clip(Shapes.Pill).background(Ink).clickable(onClick = onDone), contentAlignment = Alignment.Center) {
                Text("Done · ${list.size} selected", color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleDialog(onDone: () -> Unit) {
    var label by remember { mutableStateOf("Work hours") }
    var days by remember { mutableStateOf(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) }
    var start by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var end by remember { mutableStateOf(LocalTime.of(17, 0)) }
    ReMakeUDialog(
        onDismissRequest = onDone, title = "New schedule", secondaryText = "Cancel", primaryText = "Add", primaryEnabled = days.isNotEmpty() && start != end,
        onPrimary = { BlockStore.addSchedule(label, days, start, end); onDone() },
        content = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Work hours", "Study time", "Bedtime", "Morning").forEach { l -> ChoicePill(l, label == l) {
                    label = l
                    when (l) {
                        "Bedtime" -> { start = LocalTime.of(23, 0); end = LocalTime.of(7, 0); days = DayOfWeek.entries.toSet() }
                        "Morning" -> { start = LocalTime.of(6, 0); end = LocalTime.of(9, 0); days = DayOfWeek.entries.toSet() }
                        "Study time" -> { start = LocalTime.of(18, 0); end = LocalTime.of(21, 0) }
                        else -> { start = LocalTime.of(9, 0); end = LocalTime.of(17, 0) }
                    }
                } }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                DayOfWeek.entries.forEach { d ->
                    val on = d in days
                    Box(Modifier.weight(1f).aspectRatio(1f).clip(CircleShape).background(if (on) Ink else ChipNeutral).clickable { days = if (on) days - d else days + d }, contentAlignment = Alignment.Center) {
                        Text(d.getDisplayName(TextStyle.NARROW, Locale.getDefault()), color = if (on) Color.White else Ink, fontSize = TextSize.Caption, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TimeAdjust("From", start, Modifier.weight(1f)) { start = it }
                TimeAdjust("To", end, Modifier.weight(1f)) { end = it }
            }
        },
    )
}

@Composable
private fun TimeAdjust(label: String, time: LocalTime, modifier: Modifier, onChange: (LocalTime) -> Unit) {
    Column(modifier.clip(Shapes.Medium).background(ChipNeutral).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = TextSize.Caption)
        Text(formatClock(time), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("−", fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onChange(time.minusMinutes(30)) }.padding(8.dp, 0.dp))
            Text("+", fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onChange(time.plusMinutes(30)) }.padding(8.dp, 0.dp))
        }
    }
}
