package com.glowup.app.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.glowup.app.R
import com.glowup.app.billing.Billing
import com.glowup.app.ui.data.PlanType
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.program.Program
import com.glowup.app.ui.data.style.StyleLibrary
import com.glowup.app.ui.theme.*

private val Gold = Color(0xFFF5C542)

private val benefits = listOf(
    "All ${Program.TOTAL} programme sessions — the full 12 weeks",
    "Every style guide and all ${StyleLibrary.outfits.size} outfit breakdowns",
    "Every Talk lesson and grooming guide",
    "All 8 sleep sounds and every morning session",
    "Every new session and look we add",
)

/** Plans popup. Shown above every screen whenever [ProStore.paywallReason] is set. */
@Composable
fun ProPaywall() {
    val reason = ProStore.paywallReason ?: return
    val context = LocalContext.current
    var selected by remember { mutableStateOf(ProStore.plans.first()) }
    var message by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { ProStore.dismissPaywall() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.BottomCenter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(Surface)
                    .navigationBarsPadding(),
            ) {
                Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Ink)
                            .padding(24.dp, 26.dp, 24.dp, 24.dp),
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.TopEnd).size(36.dp).clip(CircleShape).clickable { ProStore.dismissPaywall() }.padding(6.dp),
                        )
                        Column {
                            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Gold), contentAlignment = Alignment.Center) {
                                Image(painterResource(R.drawable.ic_crown), contentDescription = null, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(if (ProStore.isPro) "You're on Glow-Up Pro" else "Glow-Up Pro", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(if (ProStore.isPro) "Everything is unlocked. Thanks for supporting Glow-Up." else reason, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp, 20.dp, 24.dp, 0.dp)) {
                        benefits.forEach { benefit ->
                            Row(modifier = Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(GreenTint), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Green, modifier = Modifier.size(14.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(benefit, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 19.sp)
                            }
                        }

                        if (!ProStore.isPro) {
                            Spacer(Modifier.height(10.dp))
                            ProStore.plans.forEach { plan ->
                                val isSelected = plan == selected
                                val trial = Billing.trialDays(plan)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isSelected) YellowTint else CardNeutral)
                                        .border(2.dp, if (isSelected) Ink else Color.Transparent, RoundedCornerShape(18.dp))
                                        .clickable { selected = plan; message = null }
                                        .padding(16.dp, 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Ink else Color.Transparent)
                                            .border(2.dp, if (isSelected) Ink else TextMuted, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (isSelected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(plan.title, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                                            plan.badge?.let {
                                                Spacer(Modifier.width(8.dp))
                                                Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Ink).padding(8.dp, 3.dp)) {
                                                    Text(it, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text(if (trial != null) "$trial-day free trial · ${plan.note}" else plan.note, color = TextSecondary, fontSize = 12.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(Billing.price(plan) ?: plan.fallbackPrice, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text(plan.period, color = TextSecondary, fontSize = 11.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(24.dp, 8.dp, 24.dp, 18.dp)) {
                    (message ?: Billing.lastError)?.let {
                        Text(it, color = Maroon, fontSize = 12.5.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    }
                    if (ProStore.isPro) {
                        CtaButton("Manage subscription") {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}")),
                            )
                        }
                    } else {
                        val trial = Billing.trialDays(selected)
                        CtaButton(if (trial != null) "Start $trial-day free trial" else "Continue with ${selected.title}") {
                            Billing.clearError()
                            val activity = context.findActivity()
                            message = when {
                                activity == null -> "Something went wrong. Please try again."
                                Billing.status != Billing.Status.READY -> { Billing.retry(); "Google Play isn't available right now. Check you're signed in to the Play Store and try again." }
                                !Billing.isLoaded(selected) -> { Billing.retry(); "This plan isn't available yet. Please try again shortly." }
                                !Billing.purchase(activity, selected) -> "Couldn't open Google Play. Please try again."
                                else -> null
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "Restore purchases",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clip(RoundedCornerShape(999.dp)).clickable {
                                message = "Checking Google Play…"
                                Billing.refreshPurchases { found ->
                                    message = if (found) null else "No Pro purchase found on this Google account."
                                }
                            }.padding(10.dp, 4.dp),
                        )
                    }
                    if (!ProStore.isPro && selected.type == PlanType.SUBSCRIPTION) {
                        Text(
                            "Renews automatically until you cancel in Google Play. Cancel anytime.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CtaButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Ink)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Small "PRO" pill with a crown, shown on locked items. */
@Composable
fun ProBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(999.dp)).background(Gold).padding(7.dp, 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(R.drawable.ic_crown), contentDescription = null, modifier = Modifier.size(11.dp))
        Spacer(Modifier.width(3.dp))
        Text("PRO", color = Ink, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
    }
}

/** Crown button for the Home header. */
@Composable
fun ProCrownButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Gold)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(R.drawable.ic_crown), contentDescription = if (ProStore.isPro) "Glow-Up Pro" else "Upgrade to Pro", modifier = Modifier.size(24.dp))
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
