package com.glowup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.TaskAlt
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
import androidx.compose.ui.window.Dialog
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.theme.CardNeutral
import com.glowup.app.ui.theme.ChipNeutral
import com.glowup.app.ui.theme.Green
import com.glowup.app.ui.theme.GreenTint
import com.glowup.app.ui.theme.Ink
import com.glowup.app.ui.theme.TextSecondary

/**
 * Shown once before a feature first uses something sensitive (microphone, camera, body data,
 * app usage). It says exactly what is collected, why, and where it stays — before Android's own
 * permission prompt appears. Agreeing is recorded in [ConsentStore] and can be withdrawn from
 * Settings → Your data.
 */
@Composable
fun ConsentSheet(consent: Consent, icon: ImageVector, onAllow: () -> Unit, onDecline: () -> Unit) {
    Dialog(onDismissRequest = onDecline) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFFFAF9F6))
                .padding(24.dp, 24.dp, 24.dp, 20.dp),
        ) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(Ink), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(consent.title, fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text("Here's exactly what happens before you say yes.", color = TextSecondary, fontSize = 13.5.sp)
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardNeutral).padding(16.dp, 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ConsentLine(Icons.Outlined.Info, "What we collect", consent.collects)
                ConsentLine(Icons.Outlined.TaskAlt, "What it's for", consent.purpose)
                ConsentLine(Icons.Outlined.PhoneAndroid, "Where it stays", consent.storage)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(GreenTint).padding(12.dp, 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = Green, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("Change your mind any time in Settings → Your data.", color = Color(0xFF1E7A4F), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(999.dp)).background(ChipNeutral).clickable(onClick = onDecline),
                    contentAlignment = Alignment.Center,
                ) { Text("Not now", color = Ink, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold) }
                Box(
                    modifier = Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(999.dp)).background(Ink).clickable {
                        ConsentStore.grant(consent)
                        onAllow()
                    },
                    contentAlignment = Alignment.Center,
                ) { Text("Allow", color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun ConsentLine(icon: ImageVector, label: String, text: String) {
    Row {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.padding(top = 1.dp).size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label.uppercase(), color = TextSecondary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(2.dp))
            Text(text, fontSize = 13.5.sp, lineHeight = 19.sp, color = Ink)
        }
    }
}
