package com.glowup.app.ui.screens.styling

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.glowup.app.R
import com.glowup.app.ui.components.GlowUpBottomNav
import com.glowup.app.ui.components.NavTab
import com.glowup.app.ui.components.OutfitBoard
import com.glowup.app.ui.components.OutfitCard
import com.glowup.app.ui.components.SwatchDot
import com.glowup.app.ui.data.style.SavedLooksStore
import com.glowup.app.ui.data.style.StyleLibrary
import com.glowup.app.ui.theme.*

@Composable
fun StylingScreen(navController: NavHostController) {
    var occasion by rememberSaveable { mutableStateOf("Office") }
    var offset by remember(occasion) { mutableStateOf(0) }
    val options = StyleLibrary.forOccasion(occasion)
    val pick = options[(StyleLibrary.pickOfTheDay(occasion) + offset) % options.size]
    val pickGuide = StyleLibrary.guide(pick.styleKey)!!
    val saved = SavedLooksStore.ids.mapNotNull { StyleLibrary.outfit(it) }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            item {
                Text("Styling", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(24.dp, 32.dp, 24.dp, 0.dp))
                Text(
                    "${StyleLibrary.outfits.size} looks across ${StyleLibrary.guides.size} styles",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(18.dp))
                Text("What are you dressing for?", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(10.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StyleLibrary.occasions) { option ->
                        val selected = option == occasion
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (selected) Ink else ChipNeutral)
                                .clickable { occasion = option }
                                .padding(18.dp, 10.dp),
                        ) {
                            Text(option, color = if (selected) Color.White else Ink, fontSize = 13.5.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(CardNeutral)
                        .clickable { navController.navigate("outfit/${pick.id}") }
                        .padding(12.dp),
                ) {
                    OutfitBoard(pick, height = 250.dp)
                    Column(modifier = Modifier.padding(8.dp, 14.dp, 8.dp, 4.dp)) {
                        Text(
                            "${if (offset == 0) "TODAY'S PICK" else "LOOK ${offset + 1} OF ${options.size}"} · ${occasion.uppercase()} · ${pickGuide.title.uppercase()}",
                            color = pickGuide.accent,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(pick.name, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(pick.pieces.joinToString(" · ") { it.name }, color = TextSecondary, fontSize = 12.5.sp, lineHeight = 18.sp, maxLines = 2)
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Ink)
                                    .clickable { navController.navigate("outfit/${pick.id}") },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("See the breakdown", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Surface)
                                    .clickable { offset = (offset + 1) % options.size }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Another", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Ink)
                        .clickable { navController.navigate("ai_stylist") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(painterResource(R.drawable.ic_style), contentDescription = null, modifier = Modifier.size(36.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ask your AI stylist", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Tell it what you own — get real answers", color = Color.White.copy(alpha = 0.55f), fontSize = 11.5.sp)
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Yellow).padding(8.dp, 4.dp)) {
                        Text("PRO", color = Ink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (saved.isNotEmpty()) {
                item {
                    SectionTitle("Saved looks", "${saved.size} saved")
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(saved, key = { it.id }) { outfit ->
                            OutfitCard(outfit, Modifier.width(200.dp), showStyle = true) { navController.navigate("outfit/${outfit.id}") }
                        }
                    }
                }
            }

            item { SectionTitle("Style guides", "Palettes, outfits and what goes with what") }

            items(StyleLibrary.guides.chunked(2)) { pair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 12.dp).fillMaxWidth().height(IntrinsicSize.Min),
                ) {
                    pair.forEach { guide ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(22.dp))
                                .background(guide.tint)
                                .clickable { navController.navigate("style_guide_detail/${guide.key}") }
                                .padding(16.dp),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                guide.palette.forEach { SwatchDot(it.color, 22.dp) }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(guide.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text(guide.blurb, color = guide.heroText, fontSize = 12.sp, lineHeight = 16.sp, minLines = 2)
                            Spacer(Modifier.height(10.dp))
                            Text("${guide.outfits.size} looks", color = guide.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(24.dp, 4.dp, 24.dp, 0.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardNeutral)
                        .clickable { navController.navigate("wardrobe") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Surface), contentAlignment = Alignment.Center) {
                        Image(painterResource(R.drawable.ic_closet), contentDescription = null, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("My wardrobe", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                        Text("Keep track of the clothes you own", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            GlowUpBottomNav(navController, NavTab.STYLING)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(24.dp, 24.dp, 24.dp, 12.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = TextSecondary, fontSize = 12.5.sp)
    }
}
