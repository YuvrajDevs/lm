package com.glowup.app.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowup.app.R
import androidx.compose.material.icons.filled.Lock
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.ProfileStore
import com.glowup.app.ui.theme.*
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.design.ScreenTitle
import com.glowup.app.ui.design.ScreenSubtitle
import com.glowup.app.ui.design.Overline
import com.glowup.app.ui.design.PrimaryButton
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.StepIndicator
import com.glowup.app.ui.design.TextAction
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import com.glowup.app.ui.data.glow.GlowCheckStore

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
        StepIndicator(lastStep + 1, step, Modifier.padding(24.dp, 24.dp, 24.dp, 0.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 28.dp, 24.dp, 16.dp),
        ) {
            when (step) {
                0 -> {
                    Image(painterResource(R.drawable.ic_streak), contentDescription = null, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(18.dp))
                    ScreenTitle("Welcome to Re:MakeU")
                    ScreenSubtitle("Small daily habits for how you look, feel and talk. What should we call you?")
                    Spacer(Modifier.height(22.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(40) },
                        placeholder = { Text("Your first name") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                    )
                    Spacer(Modifier.height(Space.Section))
                    Overline("What you'll build")
                    Spacer(Modifier.height(Space.M))
                    PillarRow(Icons.Outlined.Face, CoralTint, Coral, "Look", "Skin, grooming, style and fitness routines")
                    PillarRow(Icons.Outlined.SelfImprovement, PurpleTint, Purple, "Feel", "Focus, sleep, mood and calm")
                    PillarRow(Icons.Outlined.RecordVoiceOver, BlueTint, Blue, "Talk", "Confidence and speaking practice")
                    Spacer(Modifier.height(Space.L))
                    PrivacyNote()
                }
                1 -> {
                    ScreenTitle("How old are you?")
                    ScreenSubtitle("We use this only to keep advice right for your age — for example, under-18s never get weight-loss goals. It stays on your phone.")
                    Spacer(Modifier.height(20.dp))
                    ProfileStore.ageBands.forEach { band ->
                        ChoiceRow(band, ageBand == band) { ageBand = band }
                    }
                }
                2 -> {
                    ScreenTitle("What do you want to work on?")
                    ScreenSubtitle("Pick as many as you like. We'll put these first on your home screen.")
                    Spacer(Modifier.height(20.dp))
                    ProfileStore.focusOptions.forEach { option ->
                        val selected = option in focus
                        ChoiceRow(option, selected) { focus = if (selected) focus - option else focus + option }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(Ink),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(painterResource(R.drawable.ic_bell_glyph), contentDescription = null, modifier = Modifier.size(38.dp))
                    }
                    Spacer(Modifier.height(18.dp))
                    ScreenTitle("Stay on track, ${name.trim().substringBefore(" ")}")
                    ScreenSubtitle("Re:MakeU can remind you about your routines, bedtime and wake-up. You choose every reminder — nothing is on until you turn it on.")
                    Spacer(Modifier.height(Space.Section))
                    Overline("For example")
                    Spacer(Modifier.height(Space.M))
                    ReminderPreview(Icons.Outlined.WbSunny, YellowTint, Amber, "Good morning", "Your morning routine is ready · 7:00")
                    ReminderPreview(Icons.Outlined.Spa, CoralTint, CoralDeep, "Skincare", "Evening routine, 3 steps · 21:00")
                    ReminderPreview(Icons.Outlined.Bedtime, PurpleTint, PurpleDeep, "Wind down", "Bedtime in 30 minutes · 22:30")
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp, 8.dp, 24.dp, 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PrimaryButton(
                text = if (step == lastStep) "Allow notifications" else "Continue",
                enabled = canContinue,
            ) { if (step < lastStep) step++ else askNotifications() }
            when {
                step == lastStep -> TextAction("Not now", Modifier.padding(top = Space.S)) { finish() }
                step > 0 -> TextAction("Back", Modifier.padding(top = Space.S)) { step-- }
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
            .padding(bottom = 10.dp)
            .heightIn(min = 52.dp)
            .clip(Shapes.Medium)
            .background(if (selected) Ink else CardNeutral)
            .clickable(onClick = onClick)
            .padding(18.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = if (selected) Color.White else Ink, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (selected) Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun PrivacyNote() {
    Row(
        modifier = Modifier.fillMaxWidth().clip(Shapes.Medium).background(GreenTint).padding(14.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "Private by design: no account, no ads, and everything you add stays on this phone.",
            color = GreenDeep,
            fontSize = TextSize.BodySmall,
            lineHeight = LineHeight.Compact,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PillarRow(icon: ImageVector, tint: Color, accent: Color, title: String, body: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
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
        Modifier.fillMaxWidth().padding(bottom = Space.S).clip(Shapes.Card).background(CardNeutral).padding(14.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
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
