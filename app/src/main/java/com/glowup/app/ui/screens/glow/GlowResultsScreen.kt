package com.glowup.app.ui.screens.glow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.glow.AreaScore
import com.glowup.app.ui.data.glow.CheckResult
import com.glowup.app.ui.data.glow.FaceShapeGuide
import com.glowup.app.ui.data.glow.FocusArea
import com.glowup.app.ui.data.glow.GlowCheckStore
import com.glowup.app.ui.data.glow.GlowScoring
import com.glowup.app.ui.data.glow.LooksRoutines
import com.glowup.app.ui.data.glow.PostureAnalysis
import com.glowup.app.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

fun FocusArea.tint(): Pair<Color, Color> = when (this) {
    FocusArea.BODY -> GreenTint to Green
    FocusArea.POSTURE -> BlueTint to Navy
    FocusArea.SKIN -> CoralTint to Coral
    FocusArea.GROOMING -> PurpleTint to Purple
    FocusArea.SLEEP -> NavyTint to Navy
    FocusArea.STYLE -> YellowTint to Color(0xFF8A6A00)
    FocusArea.JAW_NECK -> TealTint to Teal
    FocusArea.SYMMETRY -> MaroonTint to Maroon
    FocusArea.CONFIDENCE -> GreenTint to Green
}

@Composable
fun GlowResultsScreen(navController: NavHostController) {
    val latest = GlowCheckStore.latest
    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader("Glow-Up map") { navController.goBack() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 18.dp, 24.dp, 28.dp)) {
                if (latest == null) {
                    Text("No check yet", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Take the Glow-Up Check to get your personalised map and plan.", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
                } else {
                    ResultsContent(latest, GlowCheckStore.previous, navController)
                }
                Spacer(Modifier.height(20.dp))
                val due = GlowCheckStore.daysUntilRescan()
                Box(
                    Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(999.dp)).background(if (latest == null || due == 0) Ink else ChipNeutral).clickable { navController.navigate("glow_check") },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        when {
                            latest == null -> "Start Glow-Up Check"
                            due == 0 -> "Rescan now — it's been 4 weeks"
                            else -> "Rescan (recommended in $due days)"
                        },
                        color = if (latest == null || due == 0) Color.White else Ink, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun ResultsContent(result: CheckResult, previous: CheckResult?, navController: NavHostController) {
    val minor = ProfileStore.isMinor
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Ink).padding(20.dp)) {
        Text("YOUR GLOW-UP MAP · " + result.date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())).uppercase(), color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            if (result.focus.isEmpty()) "You're in great shape. Keep your habits going."
            else "Your biggest wins are in ${naturalList(result.focus.map { it.area.title.lowercase() })}.",
            color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 25.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text("Ranked by how much each can change for you — not a score of your looks.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.5.sp, lineHeight = 17.sp)
    }

    if (previous != null) {
        Spacer(Modifier.height(14.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(GreenTint).padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Green, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Since your last check", color = Color(0xFF1E7A4F), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            GlowScoring.changes(previous, result).forEach {
                Text("• $it", color = Color(0xFF1E7A4F), fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }

    if (result.focus.isNotEmpty()) {
        SectionTitle("Focus on these")
        result.focus.forEachIndexed { i, area -> FocusCard(i + 1, area, navController) }
    }
    if (result.strengths.isNotEmpty()) {
        SectionTitle("What's working")
        result.strengths.forEach { s ->
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(20.dp)).background(CardNeutral).padding(16.dp, 14.dp)) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFE0B400), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(s.area.title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(s.reason, color = TextSecondary, fontSize = 12.5.sp, lineHeight = 17.sp)
                }
            }
        }
    }

    SectionTitle("Your measurements")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Metric("Face shape", result.faceShape ?: "—", if (result.face != null) "measured" else if (result.faceShape != null) "you chose" else "not set", Modifier.weight(1f)) {
            navController.navigate("face_shape")
        }
        Metric("Posture", result.neckAngle?.let { "$it°" } ?: "—", result.neckAngle?.let { PostureAnalysis.level(it).label } ?: "not scanned", Modifier.weight(1f))
    }
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Metric("Symmetry", result.face?.symmetry?.let { "$it" } ?: "—", if (result.face != null) "most people: 88–97" else "not scanned", Modifier.weight(1f))
        if (!minor) Metric("Waist : height", result.waistToHeight?.let { "%.2f".format(it) } ?: "—", result.waistToHeight?.let { if (it < 0.5) "healthy range" else "aim under 0.50" } ?: "not added", Modifier.weight(1f))
        else Spacer(Modifier.weight(1f))
    }

    SectionTitle("Keep going")
    LinkRow(Icons.Outlined.ContentCut, "Haircuts & beards for your face", "What to ask your barber") { navController.navigate("face_shape") }
    LinkRow(Icons.Outlined.Spa, "Looksmaxxing routines", "${LooksRoutines.all.size} routines with honest evidence labels") { navController.navigate("glow_routines") }
    LinkRow(Icons.Filled.PhotoCamera, "Progress photos", "Private, side-by-side, never in your gallery") { navController.navigate("progress_photos") }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(22.dp))
    Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun FocusCard(rank: Int, score: AreaScore, navController: NavHostController) {
    val (tint, accent) = score.area.tint()
    val routines = LooksRoutines.forArea(score.area)
    Column(Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(24.dp)).background(tint).padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(30.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Text("$rank", color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(score.area.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(score.area.tagline, color = TextSecondary, fontSize = 12.sp)
            }
            Text(score.roomLabel, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White).padding(9.dp, 4.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(score.reason, fontSize = 13.5.sp, lineHeight = 19.sp, color = Ink.copy(alpha = 0.85f))
        routines.firstOrNull()?.let { r ->
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White).clickable { navController.navigate("glow_routine/${r.id}") }.padding(14.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(r.title, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = accent)
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, sub: String, modifier: Modifier, onClick: (() -> Unit)? = null) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(CardNeutral).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(14.dp, 12.dp)) {
        Text(label, color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(sub, color = TextSecondary, fontSize = 11.5.sp)
    }
}

@Composable
fun LinkRow(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(20.dp)).background(CardNeutral).clickable(onClick = onClick).padding(16.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
            Text(sub, color = TextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}

@Composable
fun FaceShapeScreen(navController: NavHostController) {
    val selected = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(ProfileStore.faceShape ?: "Oval") }
    val guide = FaceShapeGuide.forShape(selected.value) ?: FaceShapeGuide.all.first()
    val note = FaceShapeGuide.beardNote(GlowCheckStore.latest?.face?.lowerFaceRatio)
    Box(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            DetailHeader("Face shape") { navController.goBack() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 14.dp, bottom = 28.dp)) {
                androidx.compose.foundation.lazy.LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FaceShapeGuide.all.size) { i ->
                        val s = FaceShapeGuide.all[i].shape
                        com.glowup.app.ui.screens.fitness.ChoicePill(s + if (s == ProfileStore.faceShape) " · you" else "", selected.value == s) { selected.value = s }
                    }
                }
                Column(Modifier.padding(24.dp, 16.dp, 24.dp, 0.dp)) {
                    Text("${guide.shape} face", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text(guide.summary, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 6.dp))
                    SectionTitle("Haircuts that suit you")
                    guide.haircuts.forEach { TipCard(it.name, it.why, PurpleTint) }
                    Spacer(Modifier.height(4.dp))
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Ink).padding(18.dp)) {
                        Text("WHAT TO TELL YOUR BARBER", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                        Text(guide.barberScript, color = Color.White, fontSize = 14.5.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                    SectionTitle("Avoid")
                    guide.avoid.forEach { Text("• $it", fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(vertical = 2.dp)) }
                    SectionTitle("Beard")
                    guide.beards.forEach { TipCard(it.name, it.why, TealTint) }
                    if (selected.value == ProfileStore.faceShape) note?.let { Text(it, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 4.dp)) }
                    SectionTitle("Glasses")
                    Text(guide.glasses, fontSize = 14.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Face shape is a guide, not a rule — your hair type and what you like matter just as much.", color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun TipCard(title: String, body: String, tint: Color) {
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(20.dp)).background(tint).padding(16.dp, 14.dp)) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(body, fontSize = 13.sp, lineHeight = 18.sp, color = Ink.copy(alpha = 0.8f), modifier = Modifier.padding(top = 2.dp))
    }
}

/** "a", "a and b", "a, b and c". */
fun naturalList(items: List<String>): String = when (items.size) {
    0 -> ""
    1 -> items[0]
    else -> items.dropLast(1).joinToString(", ") + " and " + items.last()
}
