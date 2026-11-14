package com.glowup.app.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.data.glow.GlowCheckStore
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Overline
import com.glowup.app.ui.design.PrimaryButton
import com.glowup.app.ui.design.ScreenSubtitle
import com.glowup.app.ui.design.ScreenTitle
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.StepIndicator
import com.glowup.app.ui.design.TextAction
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*

@Composable
fun OnboardingScreen(navController: NavHostController) {
    var step by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var focus by remember { mutableStateOf(setOf<String>()) }
    var ageBand by remember { mutableStateOf<String?>(null) }

    fun finish() {
        ProfileStore.completeOnboarding(name, ProfileStore.focusOptions.filter { it in focus }, ProfileStore.faceShape, ageBand)
        navController.navigate("home") { popUpTo("onboarding") { inclusive = true } }
        // Home opens the personalised check once, so backing out of it lands on Home.
        GlowCheckStore.openAfterOnboarding = true
    }

    val askNotifications = rememberNotificationPermission { finish() }

    val lastStep = 3
    val canContinue = when (step) {
        0 -> name.isNotBlank()
        1 -> ageBand != null
        2 -> focus.isNotEmpty()
        else -> true
    }

    Column(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding().imePadding()) {
        StepIndicator(lastStep + 1, step, Modifier.padding(Space.Gutter, Space.Gutter, Space.Gutter, 0.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.Section, Space.Gutter, Space.L)
        ) {
            when (step) {
                0 -> {
                    Image(painterResource(R.drawable.ic_streak), contentDescription = null, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(Space.L))
                    ScreenTitle(stringResource(R.string.onboarding_welcome_to_re_makeu))
                    ScreenSubtitle(stringResource(R.string.onboarding_small_daily_habits_for_how))
                    Spacer(Modifier.height(Space.Xl))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(40) },
                        placeholder = { Text(stringResource(R.string.onboarding_your_first_name)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted)
                    )
                    Spacer(Modifier.height(Space.Section))
                    Overline(stringResource(R.string.onboarding_what_you_ll_build))
                    Spacer(Modifier.height(Space.M))
                    PillarRow(Icons.Outlined.Face, CoralTint, Coral, "Look", "Skin, grooming, style and fitness routines")
                    PillarRow(Icons.Outlined.SelfImprovement, PurpleTint, Purple, "Feel", "Focus, sleep, mood and calm")
                    PillarRow(Icons.Outlined.RecordVoiceOver, BlueTint, Blue, "Talk", "Confidence and speaking practice")
                    Spacer(Modifier.height(Space.L))
                    PrivacyNote()
                }
                1 -> {
                    ScreenTitle(stringResource(R.string.onboarding_how_old_are_you))
                    ScreenSubtitle(stringResource(R.string.onboarding_we_use_this_only_to))
                    Spacer(Modifier.height(Space.Xl))
                    ProfileStore.ageBands.forEach { band ->
                        ChoiceRow(band, ageBand == band) { ageBand = band }
                    }
                }
                2 -> {
                    ScreenTitle(stringResource(R.string.onboarding_what_do_you_want_to))
                    ScreenSubtitle(stringResource(R.string.onboarding_pick_as_many_as_you))
                    Spacer(Modifier.height(Space.Xl))
                    ProfileStore.focusOptions.forEach { option ->
                        val selected = option in focus
                        ChoiceRow(option, selected) { focus = if (selected) focus - option else focus + option }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(Ink),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(painterResource(R.drawable.ic_bell_glyph), contentDescription = null, modifier = Modifier.size(38.dp))
                    }
                    Spacer(Modifier.height(Space.L))
                    ScreenTitle("Stay on track, ${name.trim().substringBefore(" ")}")
                    ScreenSubtitle(stringResource(R.string.onboarding_re_makeu_can_remind_you))
                    Spacer(Modifier.height(Space.Section))
                    Overline(stringResource(R.string.onboarding_for_example))
                    Spacer(Modifier.height(Space.M))
                    ReminderPreview(Icons.Outlined.WbSunny, YellowTint, Amber, "Good morning", "Your morning routine is ready · 7:00")
                    ReminderPreview(Icons.Outlined.Spa, CoralTint, CoralDeep, "Skincare", "Evening routine, 3 steps · 21:00")
                    ReminderPreview(Icons.Outlined.Bedtime, PurpleTint, PurpleDeep, "Wind down", "Bedtime in 30 minutes · 22:30")
                }
            }
        }

        Column(modifier = Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Xl), horizontalAlignment = Alignment.CenterHorizontally) {
            PrimaryButton(
                text = if (step == lastStep) stringResource(R.string.onboarding_allow_notifications) else stringResource(R.string.common_setup_continue),
                enabled = canContinue
            ) { if (step < lastStep) step++ else askNotifications() }
            when {
                step == lastStep -> TextAction(stringResource(R.string.common_not_now), Modifier.padding(top = Space.S)) { finish() }
                step > 0 -> TextAction(stringResource(R.string.common_back), Modifier.padding(top = Space.S)) { step-- }
                else -> Spacer(Modifier.height(Space.S))
            }
        }
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Space.S)
            .heightIn(min = 52.dp)
            .clip(Shapes.Medium)
            .background(if (selected) Ink else CardNeutral)
            .selectable(selected = selected, onClick = onClick)
            .padding(Space.L, Space.L),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = if (selected) Color.White else Ink, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = stringResource(R.string.common_selected),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PrivacyNote() {
    Row(
        modifier = Modifier.fillMaxWidth().clip(Shapes.Medium).background(GreenTint).padding(Space.M, Space.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(Space.S))
        Text(
            stringResource(R.string.onboarding_private_by_design_no_account),
            color = GreenDeep,
            fontSize = TextSize.BodySmall,
            lineHeight = LineHeight.Compact,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PillarRow(icon: ImageVector, tint: Color, accent: Color, title: String, body: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Space.S), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(tint), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(Space.M))
        Column {
            Text(title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
            Text(body, color = TextSecondary, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
        }
    }
}

/** A mock notification so people can see what they're agreeing to before the system prompt. */
@Composable
private fun ReminderPreview(icon: ImageVector, tint: Color, accent: Color, title: String, body: String) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = Space.S).clip(Shapes.Card).background(CardNeutral).padding(Space.M, Space.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(36.dp).clip(Shapes.Small).background(tint), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(Space.M))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold)
            Text(body, color = TextSecondary, fontSize = TextSize.Caption)
        }
    }
}
