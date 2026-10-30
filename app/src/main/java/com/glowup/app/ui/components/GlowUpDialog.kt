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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.glowup.app.ui.theme.ChipNeutral
import com.glowup.app.ui.theme.Ink
import com.glowup.app.ui.theme.Maroon
import com.glowup.app.ui.theme.TextMuted
import com.glowup.app.ui.theme.TextSecondary

/**
 * The app's single dialog shape — replaces Material's default AlertDialog everywhere so every
 * popup shares the same rounded card, typography, and pill-button styling as the rest of the UI.
 */
@Composable
fun GlowUpDialog(
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
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFFFAF9F6))
                .padding(24.dp, 24.dp, 24.dp, 20.dp),
        ) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
            message?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
            }
            content?.let {
                Spacer(Modifier.height(14.dp))
                it()
            }
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                secondaryText?.let {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(ChipNeutral)
                            .clickable { (onSecondary ?: onDismissRequest)() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(it, color = if (secondaryDestructive) Maroon else Ink, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                primaryText?.let {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (!primaryEnabled) Color(0xFFD8D5CE) else if (primaryDestructive) Maroon else Ink)
                            .clickable(enabled = primaryEnabled) { (onPrimary ?: onDismissRequest)() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(it, color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/** Styled replacement for a Material RadioButton row, used inside GlowUpDialog picker lists. */
@Composable
fun GlowUpOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(2.dp, if (selected) Ink else TextMuted, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(Ink))
            }
        }
        Text(label, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
    }
}
