package com.glowup.app.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import androidx.compose.material.icons.filled.Lock
import com.glowup.app.ui.components.rememberNotificationPermission
import com.glowup.app.ui.data.ProfileStore
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
        com.glowup.app.ui.data.glow.GlowCheckStore.openAfterOnboarding = true
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp, 24.dp, 24.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(lastStep + 1) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (index <= step) Ink else ChipNeutral),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp, 28.dp, 24.dp, 16.dp),
        ) {
            when (step) {
                0 -> {
                    Image(painterResource(R.drawable.ic_streak), contentDescription = null, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(18.dp))
                    Title("Welcome to Glow-Up")
                    Subtitle("Small daily habits for how you look, feel and talk. What should we call you?")
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
                    Spacer(Modifier.height(18.dp))
                    PrivacyNote()
                }
                1 -> {
                    Title("How old are you?")
                    Subtitle("We use this only to keep advice right for your age — for example, under-18s never get weight-loss goals. It stays on your phone.")
                    Spacer(Modifier.height(20.dp))
                    ProfileStore.ageBands.forEach { band ->
                        ChoiceRow(band, ageBand == band) { ageBand = band }
                    }
                }
                2 -> {
                    Title("What do you want to work on?")
                    Subtitle("Pick as many as you like. We'll put these first on your home screen.")
                    Spacer(Modifier.height(20.dp))
                    ProfileStore.focusOptions.forEach { option ->
                        val selected = option in focus
                        ChoiceRow(option, selected) { focus = if (selected) focus - option else focus + option }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.size(72.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Ink),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(painterResource(R.drawable.ic_bell_glyph), contentDescription = null, modifier = Modifier.size(38.dp))
                    }
                    Spacer(Modifier.height(18.dp))
                    Title("Stay on track, ${name.trim().substringBefore(" ")}")
                    Subtitle("Glow-Up can remind you about your routines, bedtime and wake-up. You choose every reminder — nothing is on until you turn it on.")
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp, 8.dp, 24.dp, 28.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (canContinue) Ink else Color(0xFFD8D5CE))
                    .clickable(enabled = canContinue) { if (step < lastStep) step++ else askNotifications() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    when (step) {
                        lastStep -> "Allow notifications"
                        else -> "Continue"
                    },
                    color = Color.White,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (step == lastStep) {
                Text(
                    "Not now",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 14.dp).clickable { finish() },
                )
            } else if (step > 0) {
                Text(
                    "Back",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 14.dp).clickable { step-- },
                )
            }
        }
    }
}

@Composable
private fun Title(text: String) {
    Text(text, fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
}

@Composable
private fun Subtitle(text: String) {
    Spacer(Modifier.height(8.dp))
    Text(text, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Ink else CardNeutral)
            .clickable(onClick = onClick)
            .padding(18.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = if (selected) Color.White else Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (selected) Text("✓", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PrivacyNote() {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(GreenTint).padding(14.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Icon(
            androidx.compose.material.icons.Icons.Filled.Lock,
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "Private by design: no account, no ads, and everything you add stays on this phone.",
            color = Color(0xFF1E7A4F),
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
