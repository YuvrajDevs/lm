package com.glowup.app.ui.screens.glow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.glow.Evidence
import com.glowup.app.ui.data.glow.GlowCheckStore
import com.glowup.app.ui.data.glow.LooksRoutine
import com.glowup.app.ui.data.glow.LooksRoutines
import com.glowup.app.ui.theme.*

@Composable
fun EvidenceBadge(evidence: Evidence, onTint: Boolean = false) {
    val (bg, fg) = when (evidence) {
        Evidence.STRONG -> GreenTint to Color(0xFF1E7A4F)
        Evidence.MODERATE -> BlueTint to Navy
        Evidence.LOW -> MaroonTint to Maroon
        Evidence.ADVICE -> PurpleTint to Purple
    }
    Text(evidence.label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (onTint) Color.White else bg).padding(9.dp, 4.dp))
}

@Composable
fun LooksRoutinesScreen(navController: NavHostController) {
    val focus = GlowCheckStore.latest?.focus?.map { it.area }.orEmpty()
    val sorted = LooksRoutines.all.sortedBy { r -> focus.indexOf(r.area).let { if (it < 0) 99 else it } }
    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader("Routines") { navController.goBack() }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("What the paid looksmaxxing courses teach — with honest labels for what actually works.", color = TextSecondary, fontSize = 13.5.sp, lineHeight = 19.sp)
                    if (focus.isNotEmpty()) Text("Sorted for your Glow-Up map.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
                items(sorted, key = { it.id }) { r -> RoutineRow(r, r.area in focus) { navController.navigate("glow_routine/${r.id}") } }
            }
        }
    }
}

@Composable
private fun RoutineRow(r: LooksRoutine, forYou: Boolean, onClick: () -> Unit) {
    val (tint, accent) = r.area.tint()
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(tint).clickable(onClick = onClick).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(r.area.title.uppercase(), color = accent, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp, modifier = Modifier.weight(1f))
            if (forYou) Text("FOR YOU", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(accent).padding(8.dp, 3.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(r.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(r.summary, color = Ink.copy(alpha = 0.75f), fontSize = 12.5.sp, lineHeight = 17.sp, maxLines = 2, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            EvidenceBadge(r.evidence, onTint = true)
            Spacer(Modifier.width(8.dp))
            Text("${r.minutes} min", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = accent)
        }
    }
}

@Composable
fun LooksRoutineDetailScreen(navController: NavHostController, id: String) {
    val r = LooksRoutines.get(id) ?: return
    val (tint, accent) = r.area.tint()
    val added = RoutineStore.items.any { it.title.equals(r.routineTitle, ignoreCase = true) }
    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(r.area.title) { navController.goBack() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 16.dp, 24.dp, 24.dp)) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(tint).padding(20.dp)) {
                    Text(r.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EvidenceBadge(r.evidence, onTint = true)
                        Spacer(Modifier.width(8.dp))
                        Text("${r.minutes} min · ${if (r.weekly) "weekly" else "daily"}", color = TextSecondary, fontSize = 12.5.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(r.summary, fontSize = 14.sp, lineHeight = 20.sp)
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardNeutral).padding(14.dp, 10.dp)) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(r.evidence.blurb, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                }

                Spacer(Modifier.height(22.dp))
                Text("How to do it", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                r.steps.forEachIndexed { i, s ->
                    Row(Modifier.padding(vertical = 5.dp)) {
                        Box(Modifier.size(24.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(s, fontSize = 14.5.sp, lineHeight = 21.sp)
                    }
                }
                r.safety?.let {
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MaroonTint).padding(16.dp, 14.dp)) {
                        Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = Maroon, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(it, color = Maroon, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
                if (r.route != null && r.routeLabel != null) {
                    Spacer(Modifier.height(14.dp))
                    LinkRow(Icons.Filled.ChevronRight, r.routeLabel, "Go deeper") { navController.navigate(r.route) }
                }
            }
            Box(Modifier.padding(24.dp, 8.dp, 24.dp, 24.dp)) {
                Row(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(999.dp)).background(if (added) ChipNeutral else Ink)
                        .clickable(enabled = !added) { RoutineStore.add(r.routineCategory, r.routineTitle, if (r.weekly) "WEEKLY" else "DAILY") },
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (added) { Icon(Icons.Filled.Check, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)) }
                    Text(if (added) "In your routines" else "Add \"${r.routineTitle}\" to my routines", color = if (added) Ink else Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
    }
}
