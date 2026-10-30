package com.glowup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Small pill showing a day-streak count with a flame icon, used across routines, habits, and goals. */
@Composable
fun StreakChip(days: Int, accent: Color, modifier: Modifier = Modifier) {
    val label = when {
        days <= 0 -> "Not started yet"
        days == 1 -> "1 day streak"
        else -> "$days day streak"
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .padding(9.dp, 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (days > 0) {
            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp).padding(end = 2.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.size(2.dp))
        }
        Text(label, color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Row of 7 dots (Mon..Sun) marking which days of the current week a habit was completed. */
@Composable
fun WeekStreakDots(completedDays: List<Boolean>, accent: Color, modifier: Modifier = Modifier) {
    require(completedDays.size == 7)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        completedDays.forEachIndexed { index, done ->
            Box(
                modifier = Modifier
                    .padding(end = if (index == 6) 0.dp else 5.dp)
                    .size(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (done) accent else accent.copy(alpha = 0.18f)),
            )
        }
    }
}
