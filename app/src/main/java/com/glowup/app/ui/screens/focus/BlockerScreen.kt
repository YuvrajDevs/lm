package com.glowup.app.ui.screens.focus

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.blocker.Blocker
import com.glowup.app.blocker.InstalledApp
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.BlockStore
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.DayStats
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.formatClock
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.AppSwitch
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class ListKind(val title: String, val sub: String) {
    FOCUS("Blocked during focus", "Opening these while you focus shows the block screen."),
    PAUSE("Pause before opening", "A 5-second breath and a choice, every time."),
    SCHEDULE("Blocked on a schedule", "Blocked during the times you set below.")
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
        AppPicker(kind) {
            picking = null
            Blocker.sync(context)
        }
        return
    }

    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.blocker_app_blocker)) { navController.goBack() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.Section)) {
                if (!ready) {
                    Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).padding(Space.Xl)) {
                        Text(
                            stringResource(R.string.blocker_set_up_in_3_taps),
                            color = Color.White,
                            fontSize = TextSize.TitleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.blocker_android_needs_your_ok_before),
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = TextSize.BodySmall,
                            lineHeight = LineHeight.Compact,
                            modifier = Modifier.padding(top = Space.Xs)
                        )
                        Spacer(Modifier.height(Space.M))
                        SetupStep(1, "Agree to how it works", consent) { askConsent = true }
                        SetupStep(2, "Allow usage access", usage) {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                        SetupStep(3, "Allow display over other apps", overlay) {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                        Spacer(Modifier.height(Space.S))
                        Text(stringResource(R.string.blocker_re_makeu_only_sees_the), color = Mint, fontSize = TextSize.Caption, lineHeight = LineHeight.Tight)
                    }
                } else {
                    val today = BlockStore.stats[LocalDate.now().toString()] ?: DayStats()
                    val week = BlockStore.weekTotals()
                    Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).padding(Space.Xl)) {
                        Text(
                            stringResource(R.string.common_this_week),
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = TextSize.Micro,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            stringResource(R.string.blocker_1_s_min_won_back, BlockStore.minutesSaved(week)),
                            color = Color.White,
                            fontSize = TextSize.Headline,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.blocker_1_s_blocks_2_s, week.blocks, week.resisted, today.pauses),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = TextSize.BodySmall,
                            lineHeight = LineHeight.Compact
                        )
                        Text(
                            stringResource(R.string.blocker_estimate_based_on_1_s, BlockStore.MINUTES_PER_RESISTED_OPEN),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = TextSize.Micro,
                            modifier = Modifier.padding(top = Space.S)
                        )
                    }
                }

                Spacer(Modifier.height(Space.L))
                ListCard(Icons.Outlined.Block, ListKind.FOCUS, BlockStore.focusApps, PurpleTint, Purple) { picking = ListKind.FOCUS }
                ListCard(Icons.Outlined.SelfImprovement, ListKind.PAUSE, BlockStore.pauseApps, BlueTint, Navy) { picking = ListKind.PAUSE }
                ListCard(Icons.Outlined.Schedule, ListKind.SCHEDULE, BlockStore.scheduleApps, CoralTint, Coral) { picking = ListKind.SCHEDULE }

                Spacer(Modifier.height(Space.M))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.blocker_schedules), fontSize = TextSize.Title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Row(
                        Modifier.clip(Shapes.Pill).background(ChipNeutral).clickable {
                            if (ProStore.isPro ||
                                BlockStore.schedules.isEmpty()
                            ) {
                                addSchedule = true
                            } else {
                                ProStore.showPaywall("More than one blocking schedule is part of Pro")
                            }
                        }.padding(Space.M, Space.S),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.common_add), fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(Space.S))
                if (BlockStore.schedules.isEmpty()) {
                    Text(stringResource(R.string.blocker_e_g_block_social_apps), color = TextSecondary, fontSize = TextSize.BodySmall)
                }
                BlockStore.schedules.forEach { s ->
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = Space.S).clip(Shapes.Card).background(CardNeutral).padding(Space.L, Space.S),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(s.label, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${formatClock(s.start)}–${formatClock(s.end)} · " +
                                    s.days.sorted().joinToString(" ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2) },
                                color = TextSecondary,
                                fontSize = TextSize.Caption
                            )
                        }
                        AppSwitch(s.enabled, s.label) {
                            BlockStore.setScheduleEnabled(s.id, it)
                            Blocker.sync(context)
                        }
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).clickable {
                                BlockStore.removeSchedule(s.id)
                                Blocker.sync(context)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.blocker_delete_schedule),
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Space.L))
                Text(stringResource(R.string.blocker_pause_length), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(Space.S))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                    listOf(3, 5, 10, 15).forEach { s ->
                        ChoicePill(stringResource(R.string.blocker_1_s_s, s), BlockStore.pauseSeconds == s, Modifier.weight(1f)) {
                            BlockStore.updatePauseSeconds(s)
                        }
                    }
                }
                if (!ProStore.isPro) {
                    Text(
                        stringResource(R.string.blocker_free_up_to_1_s, BlockStore.FREE_MAX_APPS),
                        color = TextSecondary,
                        fontSize = TextSize.Caption,
                        lineHeight = LineHeight.Compact,
                        modifier = Modifier.padding(top = Space.M)
                    )
                }
            }
        }
    }

    if (askConsent) {
        ConsentSheet(Consent.APP_USAGE, Icons.Outlined.Block, onAllow = {
            askConsent = false
            consent = true
        }, onDecline = { askConsent = false })
    }
    if (addSchedule) {
        ScheduleDialog {
            addSchedule = false
            Blocker.sync(context)
        }
    }
}

@Composable
private fun SetupStep(n: Int, title: String, done: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(
            vertical = Space.Xs
        ).clip(Shapes.Medium).background(Color.White.copy(alpha = 0.08f)).clickable(enabled = !done, onClick = onClick).padding(Space.M, Space.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(26.dp).clip(CircleShape).background(if (done) Mint else Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            if (done) {
                Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.common_done), tint = Ink, modifier = Modifier.size(15.dp))
            } else {
                Text("$n", color = Color.White, fontSize = TextSize.Caption, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(Space.M))
        Text(title, color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (!done) Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
private fun ListCard(icon: ImageVector, kind: ListKind, apps: List<String>, tint: Color, accent: Color, onClick: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth().padding(bottom = Space.S).clip(Shapes.Card).background(tint).clickable(onClick = onClick).padding(Space.L, Space.L)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Space.S))
            Column(Modifier.weight(1f)) {
                Text(kind.title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                Text(kind.sub, color = TextSecondary, fontSize = TextSize.Caption)
            }
            Spacer(Modifier.width(Space.M))
            Text(
                if (apps.isEmpty()) stringResource(R.string.blocker_choose) else "${apps.size}",
                color = accent,
                fontSize = TextSize.BodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        if (apps.isNotEmpty()) {
            Spacer(Modifier.height(Space.S))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                apps.take(6).forEach { pkg ->
                    val bmp = remember(pkg) { Blocker.icon(context, pkg) }
                    Box(Modifier.size(34.dp).clip(Shapes.Small).background(Color.White), contentAlignment = Alignment.Center) {
                        bmp?.let { Image(it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(26.dp)) }
                    }
                }
                if (apps.size >
                    6
                ) {
                    Text(
                        "+${apps.size - 6}",
                        fontSize = TextSize.Caption,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppPicker(kind: ListKind, onDone: () -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val apps by produceState(emptyList<InstalledApp>()) { value = withContext(Dispatchers.IO) { Blocker.launchableApps(context) } }
    val list = when (kind) {
        ListKind.FOCUS -> BlockStore.focusApps
        ListKind.PAUSE -> BlockStore.pauseApps
        ListKind.SCHEDULE -> BlockStore.scheduleApps
    }
    val suggested =
        setOf("com.instagram.android", "com.zhiliaoapp.musically", "com.google.android.youtube", "com.snapchat.android", "com.twitter.android", "com.facebook.katana", "com.reddit.frontpage", "in.mohalla.sharechat", "com.netflix.mediaclient")
    BackHandler(onBack = onDone)
    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader(kind.title, onDone)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text(stringResource(R.string.blocker_search_apps)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            shape = Shapes.Pill,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Ink,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = CardNeutral,
                focusedContainerColor = CardNeutral
            ),
            modifier = Modifier.fillMaxWidth().padding(Space.Gutter, Space.M, Space.Gutter, Space.S)
        )
        // Order is fixed when the picker opens, so rows don't jump as you tick them.
        val initial = remember { list.toSet() }
        val ordered = remember(apps) { apps.sortedWith(compareBy({ it.packageName !in initial }, { it.packageName !in suggested }, { it.label.lowercase() })) }
        val shown = ordered.filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(Space.Gutter, Space.S, Space.Gutter, Space.L),
            verticalArrangement = Arrangement.spacedBy(Space.S)
        ) {
            if (apps.isEmpty()) item { Text(stringResource(R.string.blocker_loading_apps), color = TextSecondary, modifier = Modifier.padding(Space.S)) }
            items(shown, key = { it.packageName }) { app ->
                val on = app.packageName in list
                val bmp = remember(app.packageName) { Blocker.icon(context, app.packageName) }
                Row(
                    Modifier.fillMaxWidth().clip(
                        Shapes.Medium
                    ).background(if (on) ChipNeutral else Color.Transparent).toggleable(value = on, role = Role.Checkbox) {
                        val ok = when (kind) {
                            ListKind.FOCUS -> BlockStore.toggleFocusApp(app.packageName)
                            ListKind.PAUSE -> BlockStore.togglePauseApp(app.packageName)
                            ListKind.SCHEDULE -> BlockStore.toggleScheduleApp(app.packageName)
                        }
                        if (!ok) ProStore.showPaywall("Block more than ${BlockStore.FREE_MAX_APPS} apps with Pro")
                    }.padding(Space.S),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        bmp?.let { Image(it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(36.dp)) }
                    }
                    Spacer(Modifier.width(Space.M))
                    Column(Modifier.weight(1f)) {
                        Text(app.label, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Medium)
                        if (app.packageName in
                            suggested
                        ) {
                            Text(stringResource(R.string.blocker_common_distraction), color = TextSecondary, fontSize = TextSize.Caption)
                        }
                    }
                    Box(Modifier.size(24.dp).clip(CircleShape).background(if (on) Ink else ChipNeutral), contentAlignment = Alignment.Center) {
                        if (on) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = stringResource(R.string.common_selected),
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
        Box(Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Gutter)) {
            AppButton(stringResource(R.string.blocker_done_1_s_selected, list.size), variant = ButtonVariant.Primary, onClick = onDone)
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
        onDismissRequest = onDone,
        title = "New schedule",
        secondaryText = "Cancel",
        primaryText = "Add",
        primaryEnabled = days.isNotEmpty() && start != end,
        onPrimary = {
            BlockStore.addSchedule(label, days, start, end)
            onDone()
        },
        content = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.S), verticalArrangement = Arrangement.spacedBy(Space.S)) {
                listOf("Work hours", "Study time", "Bedtime", "Morning").forEach { l ->
                    ChoicePill(l, label == l) {
                        label = l
                        when (l) {
                            "Bedtime" -> {
                                start = LocalTime.of(23, 0)
                                end = LocalTime.of(7, 0)
                                days = DayOfWeek.entries.toSet()
                            }
                            "Morning" -> {
                                start = LocalTime.of(6, 0)
                                end = LocalTime.of(9, 0)
                                days = DayOfWeek.entries.toSet()
                            }
                            "Study time" -> {
                                start = LocalTime.of(18, 0)
                                end = LocalTime.of(21, 0)
                            }
                            else -> {
                                start = LocalTime.of(9, 0)
                                end = LocalTime.of(17, 0)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(Space.M))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.Xs)) {
                DayOfWeek.entries.forEach { d ->
                    val on = d in days
                    Box(
                        Modifier.weight(1f).aspectRatio(1f).clip(CircleShape).background(if (on) Ink else ChipNeutral).clickable {
                            days =
                                if (on) days - d else days + d
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            d.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                            color = if (on) Color.White else Ink,
                            fontSize = TextSize.Caption,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(Space.M))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                TimeAdjust("From", start, Modifier.weight(1f)) { start = it }
                TimeAdjust("To", end, Modifier.weight(1f)) { end = it }
            }
        }
    )
}

@Composable
private fun TimeAdjust(label: String, time: LocalTime, modifier: Modifier, onChange: (LocalTime) -> Unit) {
    Column(modifier.clip(Shapes.Medium).background(ChipNeutral).padding(Space.M), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = TextSize.Caption)
        Text(formatClock(time), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            Text(
                "−",
                fontSize = TextSize.TitleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onChange(time.minusMinutes(30))
                }.padding(Space.S, 0.dp)
            )
            Text(
                "+",
                fontSize = TextSize.TitleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onChange(time.plusMinutes(30))
                }.padding(Space.S, 0.dp)
            )
        }
    }
}
