package com.glowup.app.ui.screens.glow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.data.glow.Evidence
import com.glowup.app.ui.data.glow.GlowCheckStore
import com.glowup.app.ui.data.glow.LooksRoutine
import com.glowup.app.ui.data.glow.LooksRoutines
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

@Composable
fun EvidenceBadge(evidence: Evidence, onTint: Boolean = false) {
    val (bg, fg) = when (evidence) {
        Evidence.STRONG -> GreenTint to GreenDeep
        Evidence.MODERATE -> BlueTint to Navy
        Evidence.LOW -> MaroonTint to Maroon
        Evidence.ADVICE -> PurpleTint to Purple
    }
    Text(
        evidence.label,
        color = fg,
        fontSize = TextSize.Micro,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(Shapes.Pill).background(if (onTint) Color.White else bg).padding(Space.S, Space.Xs)
    )
}

@Composable
fun LooksRoutinesScreen(navController: NavHostController) {
    val focus = GlowCheckStore.latest?.focus?.map { it.area }.orEmpty()
    val sorted = LooksRoutines.all.sortedBy { r -> focus.indexOf(r.area).let { if (it < 0) 99 else it } }
    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.common_routine_routines)) { navController.goBack() }
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(Space.Gutter, Space.L, Space.Gutter, Space.Section),
                verticalArrangement = Arrangement.spacedBy(Space.S)
            ) {
                item {
                    Text(
                        stringResource(R.string.looks_routine_what_the_paid_looksmaxxing_courses),
                        color = TextSecondary,
                        fontSize = TextSize.Body,
                        lineHeight = LineHeight.Body
                    )
                    if (focus.isNotEmpty()) {
                        Text(
                            stringResource(R.string.looks_routine_sorted_for_your_glow_map),
                            color = TextSecondary,
                            fontSize = TextSize.Caption,
                            modifier = Modifier.padding(top = Space.Xs)
                        )
                    }
                }
                items(sorted, key = { it.id }) { r -> RoutineRow(r, r.area in focus) { navController.navigate("glow_routine/${r.id}") } }
            }
        }
    }
}

@Composable
private fun RoutineRow(r: LooksRoutine, forYou: Boolean, onClick: () -> Unit) {
    val (tint, accent) = r.area.tint()
    Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(tint).clickable(onClick = onClick).padding(Space.L)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                r.area.title.uppercase(),
                color = accent,
                fontSize = TextSize.Micro,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                modifier = Modifier.weight(1f)
            )
            if (forYou) {
                Text(
                    stringResource(R.string.looks_routine_for_you),
                    color = Color.White,
                    fontSize = TextSize.Micro,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(Shapes.Pill).background(accent).padding(Space.S, Space.Xs)
                )
            }
        }
        Spacer(Modifier.height(Space.Xs))
        Text(r.title, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
        Text(
            r.summary,
            color = Ink.copy(alpha = 0.75f),
            fontSize = TextSize.BodySmall,
            lineHeight = LineHeight.Compact,
            maxLines = 2,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(Modifier.height(Space.S))
        Row(verticalAlignment = Alignment.CenterVertically) {
            EvidenceBadge(r.evidence, onTint = true)
            Spacer(Modifier.width(Space.S))
            Text(stringResource(R.string.common_1_s_min, r.minutes), color = TextSecondary, fontSize = TextSize.Caption)
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
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.Gutter)) {
                Column(Modifier.fillMaxWidth().clip(Shapes.Large).background(tint).padding(Space.Xl)) {
                    Text(r.title, fontSize = TextSize.Headline, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(Space.S))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EvidenceBadge(r.evidence, onTint = true)
                        Spacer(Modifier.width(Space.S))
                        Text("${r.minutes} min · ${if (r.weekly) "weekly" else "daily"}", color = TextSecondary, fontSize = TextSize.BodySmall)
                    }
                    Spacer(Modifier.height(Space.M))
                    Text(r.summary, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
                }
                Spacer(Modifier.height(Space.S))
                Row(Modifier.fillMaxWidth().clip(Shapes.Medium).background(CardNeutral).padding(Space.M, Space.S)) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(Space.S))
                    Text(r.evidence.blurb, color = TextSecondary, fontSize = TextSize.Caption, lineHeight = LineHeight.Compact)
                }

                Spacer(Modifier.height(Space.Xl))
                Text(stringResource(R.string.common_routine_how_to_do_it), fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(Space.S))
                r.steps.forEachIndexed { i, s ->
                    Row(Modifier.padding(vertical = Space.Xs)) {
                        Box(Modifier.size(24.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", color = Color.White, fontSize = TextSize.Caption, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(Space.M))
                        Text(s, fontSize = TextSize.BodyLarge, lineHeight = LineHeight.Relaxed)
                    }
                }
                r.safety?.let {
                    Spacer(Modifier.height(Space.M))
                    Row(Modifier.fillMaxWidth().clip(Shapes.Medium).background(MaroonTint).padding(Space.L, Space.M)) {
                        Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = Maroon, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Space.S))
                        Text(it, color = Maroon, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Body)
                    }
                }
                if (r.route != null && r.routeLabel != null) {
                    Spacer(Modifier.height(Space.M))
                    LinkRow(Icons.Filled.ChevronRight, r.routeLabel, "Go deeper") { navController.navigate(r.route) }
                }
            }
            Box(Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Gutter)) {
                AppButton(
                    if (added) {
                        stringResource(
                            R.string.looks_routine_in_your_routines
                        )
                    } else {
                        stringResource(R.string.looks_routine_add_1_s_to_my, r.routineTitle)
                    },
                    variant = if (added) ButtonVariant.Secondary else ButtonVariant.Primary,
                    enabled = !added
                ) {
                    RoutineStore.add(r.routineCategory, r.routineTitle, if (r.weekly) "WEEKLY" else "DAILY")
                }
            }
        }
    }
}
