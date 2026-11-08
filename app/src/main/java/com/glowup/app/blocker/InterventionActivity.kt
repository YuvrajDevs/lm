package com.glowup.app.blocker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowup.app.MainActivity
import com.glowup.app.notify.Notifications
import com.glowup.app.ui.data.BlockStore
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.theme.GlowUpTheme
import com.glowup.app.ui.theme.Ink
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
            statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        read(intent)
        setContent {
            GlowUpTheme {
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
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra(Notifications.EXTRA_ROUTE, route))
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
        LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
        val focus = FocusStore.active?.takeIf { it.isRunning && it.blockApps }
        Column(
            Modifier.fillMaxSize().background(Ink).systemBarsPadding().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Box(contentAlignment = Alignment.BottomEnd) {
                AppBadge(84)
                Box(Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFF9C79)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(if (focus != null) "You're in focus" else "$label is blocked", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            if (focus != null) {
                val left = focus.remaining(now)
                Text("%d:%02d".format(left / 60_000, left / 1000 % 60), color = Color(0xFF7FD9A8), fontSize = 54.sp, fontWeight = FontWeight.Bold)
                Text("left on ${focus.label.lowercase()} · $label opens after", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, textAlign = TextAlign.Center)
            } else {
                Text(detail ?: "", color = Color.White.copy(alpha = 0.6f), fontSize = 15.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.weight(1.2f))
            if (focus != null) {
                Box(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(999.dp)).background(Color.White).clickable { openGlowUp("focus") },
                    contentAlignment = Alignment.Center,
                ) { Text("Back to focus", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(10.dp))
            }
            Box(
                Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = 0.12f)).clickable { home() },
                contentAlignment = Alignment.Center,
            ) { Text("Go to home screen", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
            if (focus != null && !focus.strict) {
                Text(
                    "End focus early", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 14.dp).clickable { openGlowUp("focus") },
                )
            } else if (focus?.strict == true) {
                Text("Strict mode — this can't be skipped.", color = Color.White.copy(alpha = 0.45f), fontSize = 12.5.sp, modifier = Modifier.padding(top = 14.dp))
            }
        }
    }

    @Composable
    private fun PauseContent() {
        val label = remember(pkg) { Blocker.label(this, pkg) }
        var secondsLeft by remember(pkg) { mutableIntStateOf(BlockStore.pauseSeconds) }
        LaunchedEffect(pkg) { while (secondsLeft > 0) { delay(1000); secondsLeft-- } }
        BackHandler {
            if (secondsLeft == 0) { BlockStore.recordResisted(); home() }
        }
        val breath = rememberInfiniteTransition(label = "breath")
        val scale by breath.animateFloat(0.7f, 1.05f, infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")
        val opens = BlockStore.opensToday[pkg] ?: 1

        Column(
            Modifier.fillMaxSize().background(Color(0xFF1B1F2E)).systemBarsPadding().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(200.dp).scale(scale).clip(CircleShape).background(Color(0xFF8DB8F5).copy(alpha = 0.18f)))
                Box(Modifier.size(130.dp).scale(scale).clip(CircleShape).background(Color(0xFF8DB8F5).copy(alpha = 0.3f)))
                AppBadge(64)
            }
            Spacer(Modifier.height(28.dp))
            if (secondsLeft > 0) {
                Text(if (scale > 0.88f) "Breathe out…" else "Breathe in…", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Take a moment before opening $label", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
            } else {
                Text("Still want to open $label?", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(
                    if (opens <= 1) "First time today." else "That's $opens times today.",
                    color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp),
                )
            }
            Spacer(Modifier.weight(1.2f))
            AnimatedVisibility(secondsLeft == 0, enter = fadeIn()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(999.dp)).background(Color.White).clickable { BlockStore.recordResisted(); home() },
                        contentAlignment = Alignment.Center,
                    ) { Text("Not now", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(12.dp))
                    Text("Open for…", color = Color.White.copy(alpha = 0.55f), fontSize = 12.5.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 5, 15).forEach { m ->
                            Text(
                                "$m min", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = 0.12f)).clickable {
                                    BlockStore.allow(pkg, m)
                                    packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                                    finish()
                                }.padding(18.dp, 10.dp),
                            )
                        }
                    }
                }
            }
            if (secondsLeft > 0) Text("$secondsLeft", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
        }
    }
}
