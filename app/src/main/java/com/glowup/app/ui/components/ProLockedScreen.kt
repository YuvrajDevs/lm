package com.glowup.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Overline
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

/** Shown when a link (notification, saved item) leads to something that needs Pro. */
@Composable
fun ProLockedScreen(navController: NavHostController, title: String, reason: String) {
    Column(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader(stringResource(R.string.common_pro_locked_re_makeu_pro)) { navController.goBack() }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.Xl, Space.Gutter, Space.L),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(84.dp).clip(CircleShape).background(Gold), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.ic_crown), contentDescription = null, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.height(Space.Xl))
            Text(title, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(Space.S))
            Text(reason, color = TextSecondary, fontSize = TextSize.Body, textAlign = TextAlign.Center, lineHeight = LineHeight.Body)
            Spacer(Modifier.height(Space.Section))
            Column(Modifier.fillMaxWidth().clip(Shapes.Card).background(YellowTint).padding(Space.Xl)) {
                Overline(stringResource(R.string.pro_locked_everything_in_pro), color = Amber)
                Spacer(Modifier.height(Space.M))
                proBenefits.forEach { benefit ->
                    Row(Modifier.padding(vertical = Space.Xs), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Amber, modifier = Modifier.padding(top = 2.dp).size(16.dp))
                        Spacer(Modifier.width(Space.S))
                        Text(benefit, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
                    }
                }
            }
        }
        AppButton(
            stringResource(R.string.pro_locked_see_pro_plans),
            variant = ButtonVariant.Primary,
            modifier = Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Section).fillMaxWidth()
        ) {
            ProStore.showPaywall(reason)
        }
    }
}
