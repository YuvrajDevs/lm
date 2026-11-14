package com.glowup.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.theme.*
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.TextSize

/** Shown when a link (notification, saved item) leads to something that needs Pro. */
@Composable
fun ProLockedScreen(navController: NavHostController, title: String, reason: String) {
    Column(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader("Re:MakeU Pro") { navController.goBack() }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(modifier = Modifier.size(84.dp).clip(CircleShape).background(Gold), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.ic_crown), contentDescription = null, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text(title, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(reason, color = TextSecondary, fontSize = TextSize.Body, textAlign = TextAlign.Center, lineHeight = LineHeight.Body)
        }
        Box(
            modifier = Modifier
                .padding(24.dp, 8.dp, 24.dp, 28.dp)
                .fillMaxWidth()
                .height(58.dp)
                .clip(Shapes.Pill)
                .background(Ink)
                .clickable { ProStore.showPaywall(reason) },
            contentAlignment = Alignment.Center,
        ) {
            Text("See Pro plans", color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
