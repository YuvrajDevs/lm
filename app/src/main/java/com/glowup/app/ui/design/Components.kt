package com.glowup.app.ui.design

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowup.app.ui.theme.CardNeutral
import com.glowup.app.ui.theme.ChipNeutral
import com.glowup.app.ui.theme.Ink
import com.glowup.app.ui.theme.TextSecondary

/** Visual weight of a pill button. The `OnDark` variants are for buttons placed on Ink surfaces. */
enum class ButtonVariant { Primary, Secondary, OnDarkPrimary, OnDarkSecondary }

/**
 * The app's one pill button. Every full-width action uses this so height, type and states stay
 * identical everywhere. Prefer [PrimaryButton] / [SecondaryButton] at call sites.
 */
@Composable
fun AppButton(
    text: String,
    variant: ButtonVariant,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val (bg, fg) = when {
        // Disabled stays legible (4.9:1) but clearly inactive: no dark fill, muted label.
        !enabled -> ChipNeutral to TextSecondary
        variant == ButtonVariant.Primary -> Ink to Color.White
        variant == ButtonVariant.Secondary -> ChipNeutral to Ink
        variant == ButtonVariant.OnDarkPrimary -> Color.White to Ink
        else -> Color.White.copy(alpha = 0.12f) to Color.White
    }
    Row(
        modifier.height(Sizes.Button).clip(Shapes.Pill).background(bg).clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = Space.L),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(it, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Space.S))
        }
        Text(text, color = fg, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** The one main action on a screen: full-width, dark, pill-shaped. */
@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onDark: Boolean = false,
    onClick: () -> Unit
) = AppButton(text, if (onDark) ButtonVariant.OnDarkPrimary else ButtonVariant.Primary, modifier, enabled, icon, onClick)

/** A secondary action next to or below a primary one. */
@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier.fillMaxWidth(), onDark: Boolean = false, icon: ImageVector? = null, onClick: () -> Unit) =
    AppButton(text, if (onDark) ButtonVariant.OnDarkSecondary else ButtonVariant.Secondary, modifier, true, icon, onClick)

/** A quiet text-only action ("Back", "Not now", "Skip"), sized for a comfortable tap. */
@Composable
fun TextAction(text: String, modifier: Modifier = Modifier, color: Color = TextSecondary, onClick: () -> Unit) {
    Text(
        text,
        color = color,
        fontSize = TextSize.Body,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = modifier.clip(Shapes.Pill).clickable(role = Role.Button, onClick = onClick).padding(horizontal = Space.L, vertical = Space.M)
    )
}

/** Single-choice or filter chip. */
@Composable
fun Chip(label: String, selected: Boolean, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(40.dp)
            .clip(Shapes.Pill)
            .background(if (selected) Ink else ChipNeutral)
            .selectable(selected = selected, onClick = onClick)
            .padding(horizontal = Space.M),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(label, color = if (selected) Color.White else Ink, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        trailing?.let {
            Spacer(Modifier.width(Space.S))
            it()
        }
    }
}

/** Section heading with an optional supporting line and trailing action. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, subtitle: String? = null, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
            subtitle?.let { Text(it, color = TextSecondary, fontSize = TextSize.Caption, modifier = Modifier.padding(top = 2.dp)) }
        }
        if (action != null && onAction != null) {
            Text(
                action,
                fontSize = TextSize.BodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clip(Shapes.Pill).background(ChipNeutral).clickable(role = Role.Button, onClick = onAction).padding(Space.M, Space.S)
            )
        }
    }
}

/** Uppercase label above a group of content. */
@Composable
fun Overline(text: String, modifier: Modifier = Modifier, color: Color = TextSecondary) {
    Text(text.uppercase(), color = color, fontSize = TextSize.Micro, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, modifier = modifier)
}

/** Large title + supporting line used at the top of each step in a multi-step flow. */
@Composable
fun StepTitle(title: String, subtitle: String? = null) {
    Text(title, fontSize = TextSize.Display, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Display)
    subtitle?.let {
        Spacer(Modifier.height(Space.S))
        Text(it, color = TextSecondary, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
    }
}

/** Compact number tile: label, big value, optional sub-line. */
@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier, sub: String? = null, valueColor: Color = Ink) {
    Column(modifier.clip(Shapes.Card).background(CardNeutral).padding(horizontal = Space.M, vertical = Space.M)) {
        Text(label, color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.Medium, maxLines = 1)
        Text(value, color = valueColor, fontSize = TextSize.Headline, fontWeight = FontWeight.Bold, maxLines = 1)
        sub?.let { Text(it, color = TextSecondary, fontSize = TextSize.Caption, maxLines = 1) }
    }
}

/** Progress dashes shown at the top of multi-step flows. */
@Composable
fun StepIndicator(steps: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.S)) {
        repeat(steps) { i ->
            Box(Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (i <= current) Ink else ChipNeutral))
        }
    }
}

/** Large page/step title. */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier, color: Color = Ink) {
    Text(text, color = color, fontSize = TextSize.Display, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Display, modifier = modifier)
}

/** Supporting line under a [ScreenTitle]. */
@Composable
fun ScreenSubtitle(text: String, modifier: Modifier = Modifier) {
    Spacer(Modifier.height(Space.S))
    Text(text, color = TextSecondary, fontSize = TextSize.Body, lineHeight = LineHeight.Body, modifier = modifier)
}
