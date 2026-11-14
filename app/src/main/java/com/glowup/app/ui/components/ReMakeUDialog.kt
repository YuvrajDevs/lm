package com.glowup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.ChipNeutral
import com.glowup.app.ui.theme.DialogSurface
import com.glowup.app.ui.theme.Disabled
import com.glowup.app.ui.theme.Ink
import com.glowup.app.ui.theme.Maroon
import com.glowup.app.ui.theme.TextMuted
import com.glowup.app.ui.theme.TextSecondary

/**
 * The app's single dialog shape — replaces Material's default AlertDialog everywhere so every
 * popup shares the same rounded card, typography, and pill-button styling as the rest of the UI.
 */
@Composable
fun ReMakeUDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String? = null,
    primaryText: String? = "OK",
    onPrimary: (() -> Unit)? = null,
    primaryEnabled: Boolean = true,
    primaryDestructive: Boolean = false,
    secondaryText: String? = "Cancel",
    onSecondary: (() -> Unit)? = null,
    secondaryDestructive: Boolean = false,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Shapes.Hero)
                .background(DialogSurface)
                .padding(Space.Gutter, Space.Gutter, Space.Gutter, Space.Xl)
        ) {
            Text(title, fontSize = TextSize.TitleLarge, fontWeight = FontWeight.Bold, lineHeight = LineHeight.Title)
            message?.let {
                Spacer(Modifier.height(Space.S))
                Text(it, color = TextSecondary, fontSize = TextSize.Body, lineHeight = LineHeight.Body)
            }
            content?.let {
                Spacer(Modifier.height(Space.M))
                it()
            }
            Spacer(Modifier.height(Space.Xl))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                secondaryText?.let {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(Shapes.Pill)
                            .background(ChipNeutral)
                            .clickable { (onSecondary ?: onDismissRequest)() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(it, color = if (secondaryDestructive) Maroon else Ink, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
                primaryText?.let {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(Shapes.Pill)
                            .background(
                                if (!primaryEnabled) {
                                    Disabled
                                } else if (primaryDestructive) {
                                    Maroon
                                } else {
                                    Ink
                                }
                            )
                            .clickable(enabled = primaryEnabled) { (onPrimary ?: onDismissRequest)() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(it, color = Color.White, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/** Styled replacement for a Material RadioButton row, used inside ReMakeUDialog picker lists. */
@Composable
fun ReMakeUOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.Small)
            .clickable(onClick = onClick)
            .padding(vertical = Space.M),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.M)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(2.dp, if (selected) Ink else TextMuted, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(Ink))
            }
        }
        Text(label, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.Medium)
    }
}
