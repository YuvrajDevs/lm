package com.glowup.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowup.app.R
import com.glowup.app.ui.data.RoutineSession
import com.glowup.app.ui.data.RoutineStore
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.Green
import com.glowup.app.ui.theme.Ink
import java.time.LocalDate

/**
 * Floats over whichever page a routine was opened on, so the user can tick it off right where
 * they did it. Hidden on Home and the routines list, which have their own check-offs.
 */
@Composable
fun RoutineDoneBar(currentRoute: String?) {
    val routine = RoutineSession.activeId?.let(RoutineStore::find)
    val visible = routine != null &&
        currentRoute !in setOf("home", "routines", "onboarding", null) &&
        !routine.isDoneOn(LocalDate.now())
    Box(Modifier.fillMaxSize().navigationBarsPadding(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .padding(Space.L, 0.dp, Space.L, 104.dp)
                    .fillMaxWidth()
                    .shadow(12.dp, Shapes.Pill)
                    .clip(Shapes.Pill)
                    .background(Ink)
                    .padding(start = Space.L, end = Space.S, top = Space.S, bottom = Space.S),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.routine_done_bar_routine),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = TextSize.Micro,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        routine?.title.orEmpty(),
                        color = Color.White,
                        fontSize = TextSize.Body,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    Modifier.size(34.dp).clip(CircleShape).clickable { RoutineSession.clear() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.routine_done_bar_hide),
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(Space.Xs))
                Row(
                    modifier = Modifier
                        .clip(Shapes.Pill)
                        .background(Green)
                        .clickable {
                            routine?.let { RoutineStore.toggleToday(it.id) }
                            RoutineSession.clear()
                        }
                        .padding(Space.M, Space.S),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(Space.S))
                    Text(stringResource(R.string.routine_done_bar_mark_done), color = Color.White, fontSize = TextSize.BodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
