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
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.glowup.app.R
import com.glowup.app.billing.Billing
import com.glowup.app.ui.data.PlanType
import com.glowup.app.ui.data.ProStore
import com.glowup.app.ui.data.program.Program
import com.glowup.app.ui.data.style.StyleLibrary
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

private val benefits = listOf(
    "All ${Program.TOTAL} programme sessions — the full 12 weeks",
    "Every 30-day fitness programme, 15–30 min sessions and targets that rise as you get stronger",
    "Strict focus, unlimited app blocking and schedules",
    "Unlimited wardrobe, capsule builder and packing lists",
    "Every style guide and all ${StyleLibrary.outfits.size} outfit breakdowns",
    "All 30 speaking scenarios, every Talk lesson and grooming guide",
    "Every mood pattern, all sleep sounds and an extra streak freeze"
)

/** Plans popup. Shown above every screen whenever [ProStore.paywallReason] is set. */
@Composable
fun ProPaywall() {
    val reason = ProStore.paywallReason ?: return
    val context = LocalContext.current
    var selected by remember { mutableStateOf(ProStore.plans.first()) }
    var message by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { ProStore.dismissPaywall() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().padding(top = Space.Huge), contentAlignment = Alignment.BottomCenter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(Surface)
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Ink)
                            .padding(Space.Gutter, Space.Gutter, Space.Gutter, Space.Gutter)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.common_close),
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.TopEnd).size(36.dp).clip(CircleShape).clickable { ProStore.dismissPaywall() }.padding(Space.S)
                        )
                        Column {
                            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Gold), contentAlignment = Alignment.Center) {
                                Image(painterResource(R.drawable.ic_crown), contentDescription = null, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.height(Space.M))
                            Text(
                                if (ProStore.isPro) {
                                    stringResource(
                                        R.string.pro_paywall_you_re_on_re_makeu
                                    )
                                } else {
                                    stringResource(R.string.common_pro_locked_re_makeu_pro)
                                },
                                color = Color.White,
                                fontSize = TextSize.Display,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(Space.Xs))
                            Text(
                                if (ProStore.isPro) stringResource(R.string.pro_paywall_everything_is_unlocked_thanks_for) else reason,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = TextSize.Body,
                                lineHeight = LineHeight.Body
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(Space.Gutter, Space.Xl, Space.Gutter, 0.dp)) {
                        benefits.forEach { benefit ->
                            Row(modifier = Modifier.padding(bottom = Space.S), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(GreenTint), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Green, modifier = Modifier.size(14.dp))
                                }
                                Spacer(Modifier.width(Space.M))
                                Text(benefit, fontSize = TextSize.Body, fontWeight = FontWeight.Medium, lineHeight = LineHeight.Body)
                            }
                        }

                        if (!ProStore.isPro) {
                            Spacer(Modifier.height(Space.S))
                            ProStore.plans.forEach { plan ->
                                val isSelected = plan == selected
                                val trial = Billing.trialDays(plan)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = Space.S)
                                        .clip(Shapes.Medium)
                                        .background(if (isSelected) YellowTint else CardNeutral)
                                        .border(2.dp, if (isSelected) Ink else Color.Transparent, Shapes.Medium)
                                        .selectable(selected = isSelected, role = Role.RadioButton) {
                                            selected = plan
                                            message = null
                                        }
                                        .padding(Space.L, Space.M),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Ink else Color.Transparent)
                                            .border(2.dp, if (isSelected) Ink else TextMuted, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                    }
                                    Spacer(Modifier.width(Space.M))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(plan.title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Bold)
                                            plan.badge?.let {
                                                Spacer(Modifier.width(Space.S))
                                                Box(modifier = Modifier.clip(Shapes.Pill).background(Ink).padding(Space.S, Space.Xs)) {
                                                    Text(it, color = Color.White, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text(
                                            if (trial !=
                                                null
                                            ) {
                                                stringResource(R.string.pro_paywall_1_s_day_free_trial, trial, plan.note)
                                            } else {
                                                plan.note
                                            },
                                            color = TextSecondary,
                                            fontSize = TextSize.Caption
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(Billing.price(plan) ?: plan.fallbackPrice, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                                        Text(plan.period, color = TextSecondary, fontSize = TextSize.Caption)
                                    }
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.L)) {
                    (message ?: Billing.lastError)?.let {
                        Text(
                            it,
                            color = Maroon,
                            fontSize = TextSize.BodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = Space.S)
                        )
                    }
                    if (ProStore.isPro) {
                        CtaButton("Manage subscription") {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}"))
                            )
                        }
                    } else {
                        val trial = Billing.trialDays(selected)
                        CtaButton(if (trial != null) "Start $trial-day free trial" else "Continue with ${selected.title}") {
                            Billing.clearError()
                            val activity = context.findActivity()
                            message = when {
                                activity == null -> "Something went wrong. Please try again."
                                Billing.status != Billing.Status.READY -> {
                                    Billing.retry()
                                    "Google Play isn't available right now. Check you're signed in to the Play Store and try again."
                                }
                                !Billing.isLoaded(selected) -> {
                                    Billing.retry()
                                    "This plan isn't available yet. Please try again shortly."
                                }
                                !Billing.purchase(activity, selected) -> "Couldn't open Google Play. Please try again."
                                else -> null
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = Space.M),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            stringResource(R.string.pro_paywall_restore_purchases),
                            color = TextSecondary,
                            fontSize = TextSize.BodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clip(Shapes.Pill).clickable {
                                message = "Checking Google Play…"
                                Billing.refreshPurchases { found ->
                                    message = if (found) null else "No Pro purchase found on this Google account."
                                }
                            }.padding(Space.S, Space.Xs)
                        )
                    }
                    if (!ProStore.isPro && selected.type == PlanType.SUBSCRIPTION) {
                        Text(
                            stringResource(R.string.pro_paywall_renews_automatically_until_you_cancel),
                            color = TextMuted,
                            fontSize = TextSize.Micro,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = Space.Xs)
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
            .clip(Shapes.Pill)
            .background(Ink)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

/** Small "PRO" pill with a crown, shown on locked items. */
@Composable
fun ProBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(Shapes.Pill).background(Gold).padding(Space.S, Space.Xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painterResource(R.drawable.ic_crown), contentDescription = null, modifier = Modifier.size(11.dp))
        Spacer(Modifier.width(Space.Xs))
        Text(stringResource(R.string.common_pro), color = Ink, fontSize = TextSize.Micro, fontWeight = FontWeight.Bold)
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
        contentAlignment = Alignment.Center
    ) {
        Image(
            painterResource(R.drawable.ic_crown),
            contentDescription = if (ProStore.isPro) "Re:MakeU Pro" else "Upgrade to Pro",
            modifier = Modifier.size(24.dp)
        )
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
