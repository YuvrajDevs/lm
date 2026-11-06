package com.glowup.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.theme.*

/** Shown when a link (notification, saved item) leads to something that needs Pro. */
@Composable
fun ProLockedScreen(navController: NavHostController, title: String, reason: String) {
    Column(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader("Glow-Up Pro") { navController.goBack() }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(modifier = Modifier.size(84.dp).clip(CircleShape).background(Color(0xFFF5C542)), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.ic_crown), contentDescription = null, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(reason, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
        }
        Box(
            modifier = Modifier
                .padding(24.dp, 8.dp, 24.dp, 28.dp)
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Ink)
                .clickable { ProStore.showPaywall(reason) },
            contentAlignment = Alignment.Center,
        ) {
            Text("See Pro plans", color = Color.White, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
