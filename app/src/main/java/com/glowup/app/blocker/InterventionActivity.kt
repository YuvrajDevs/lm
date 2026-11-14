package com.glowup.app.blocker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.glowup.app.MainActivity
import com.glowup.app.R
import com.glowup.app.notify.Notifications
import com.glowup.app.ui.data.BlockStore
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.Ink
import com.glowup.app.ui.theme.Mint
import com.glowup.app.ui.theme.Peach
import com.glowup.app.ui.theme.ReMakeUTheme
import com.glowup.app.ui.theme.Sky
import kotlinx.coroutines.delay

/** The block screen (during focus or a schedule) and the mindful pause screen. */
class InterventionActivity : ComponentActivity() {
    companion object {
        const val EXTRA_PACKAGE = "pkg"
        const val EXTRA_MODE = "mode"
        const val EXTRA_DETAIL = "detail"
    }

    private var pkg by mutableStateOf("")
    private var mode by mutableStateOf("pause")
    private var detail by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dark screens: light status and navigation bar icons.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        read(intent)
        setContent {
            ReMakeUTheme {
                if (mode == "block") BlockContent() else PauseContent()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        read(intent)
    }

    private fun read(intent: Intent) {
        pkg = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()
        mode = intent.getStringExtra(EXTRA_MODE) ?: "pause"
        detail = intent.getStringExtra(EXTRA_DETAIL)
    }

    private fun home() {
        Blocker.goHome(this)
        finish()
    }

    private fun openGlowUp(route: String) {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra(Notifications.EXTRA_ROUTE, route)
        )
        finish()
    }

    @Composable
    private fun AppBadge(size: Int) {
        val bmp = remember(pkg) { Blocker.icon(this, pkg) }
        Box(Modifier.size(size.dp).clip(RoundedCornerShape((size / 4).dp)).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            bmp?.let { Image(it.asImageBitmap(), contentDescription = null, modifier = Modifier.size((size * 0.8f).dp)) }
        }
    }

    @Composable
    private fun BlockContent() {
        BackHandler { home() }
        val label = remember(pkg) { Blocker.label(this, pkg) }
        var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
        LaunchedEffect(Unit) {
            while (true) {
                now = System.currentTimeMillis()
                delay(1000)
            }
        }
        val focus = FocusStore.active?.takeIf { it.isRunning && it.blockApps }
        Column(
            Modifier.fillMaxSize().background(Ink).systemBarsPadding().padding(Space.Section),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Box(contentAlignment = Alignment.BottomEnd) {
                AppBadge(84)
                Box(Modifier.size(32.dp).clip(CircleShape).background(Peach), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(Space.Gutter))
            Text(
                if (focus !=
                    null
                ) {
                    stringResource(R.string.intervention_you_re_in_focus)
                } else {
                    stringResource(R.string.intervention_1_s_is_blocked, label)
                },
                color = Color.White,
                fontSize = TextSize.Display,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Space.S))
            if (focus != null) {
                val left = focus.remaining(now)
                Text("%d:%02d".format(left / 60_000, left / 1000 % 60), color = Mint, fontSize = TextSize.NumberLarge, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.intervention_left_on_1_s_2, focus.label.lowercase(), label),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = TextSize.Body,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(detail ?: "", color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.BodyLarge, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.weight(1.2f))
            if (focus != null) {
                AppButton(stringResource(R.string.intervention_back_to_focus), variant = ButtonVariant.OnDarkPrimary) { openGlowUp("focus") }
                Spacer(Modifier.height(Space.S))
            }
            AppButton(stringResource(R.string.intervention_go_to_home_screen), variant = ButtonVariant.OnDarkSecondary) { home() }
            if (focus != null && !focus.strict) {
                Text(
                    stringResource(R.string.intervention_end_focus_early),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = TextSize.BodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = Space.M).clickable { openGlowUp("focus") }
                )
            } else if (focus?.strict == true) {
                Text(
                    stringResource(R.string.intervention_strict_mode_this_can_t),
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = TextSize.BodySmall,
                    modifier = Modifier.padding(top = Space.M)
                )
            }
        }
    }

    @Composable
    private fun PauseContent() {
        val label = remember(pkg) { Blocker.label(this, pkg) }
        var secondsLeft by remember(pkg) { mutableIntStateOf(BlockStore.pauseSeconds) }
        LaunchedEffect(pkg) {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
        }
        BackHandler {
            if (secondsLeft == 0) {
                BlockStore.recordResisted()
                home()
            }
        }
        val breath = rememberInfiniteTransition(label = "breath")
        val scale by breath.animateFloat(0.7f, 1.05f, infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")
        val opens = BlockStore.opensToday[pkg] ?: 1

        Column(
            Modifier.fillMaxSize().background(Color(0xFF1B1F2E)).systemBarsPadding().padding(Space.Section),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(200.dp).scale(scale).clip(CircleShape).background(Sky.copy(alpha = 0.18f)))
                Box(Modifier.size(130.dp).scale(scale).clip(CircleShape).background(Sky.copy(alpha = 0.3f)))
                AppBadge(64)
            }
            Spacer(Modifier.height(Space.Section))
            if (secondsLeft > 0) {
                Text(
                    if (scale >
                        0.88f
                    ) {
                        stringResource(R.string.intervention_breathe_out)
                    } else {
                        stringResource(R.string.intervention_breathe_in)
                    },
                    color = Color.White,
                    fontSize = TextSize.Display,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.intervention_take_a_moment_before_opening, label),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = TextSize.Body,
                    modifier = Modifier.padding(top = Space.S)
                )
            } else {
                Text(
                    stringResource(R.string.intervention_still_want_to_open_1, label),
                    color = Color.White,
                    fontSize = TextSize.Headline,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    if (opens <= 1) stringResource(R.string.intervention_first_time_today) else stringResource(R.string.intervention_that_s_1_s_times, opens),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = TextSize.Body,
                    modifier = Modifier.padding(top = Space.S)
                )
            }
            Spacer(Modifier.weight(1.2f))
            AnimatedVisibility(secondsLeft == 0, enter = fadeIn()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppButton(stringResource(R.string.common_not_now), variant = ButtonVariant.OnDarkPrimary) {
                        BlockStore.recordResisted()
                        home()
                    }
                    Spacer(Modifier.height(Space.M))
                    Text(stringResource(R.string.intervention_open_for), color = Color.White.copy(alpha = 0.55f), fontSize = TextSize.BodySmall)
                    Spacer(Modifier.height(Space.S))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                        listOf(1, 5, 15).forEach { m ->
                            Text(
                                stringResource(R.string.common_1_s_min, m),
                                color = Color.White,
                                fontSize = TextSize.Body,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clip(Shapes.Pill).background(Color.White.copy(alpha = 0.12f)).clickable {
                                    BlockStore.allow(pkg, m)
                                    packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                                    finish()
                                }.padding(Space.L, Space.S)
                            )
                        }
                    }
                }
            }
            if (secondsLeft > 0) Text("$secondsLeft", color = Color.White.copy(alpha = 0.4f), fontSize = TextSize.Body)
        }
    }
}
