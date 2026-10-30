package com.glowup.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GlowUpColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Surface,
    background = Surface,
    surface = Surface,
    onBackground = Ink,
    onSurface = Ink,
    secondaryContainer = CardNeutral,
)

@Composable
fun GlowUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GlowUpColorScheme,
        typography = GlowUpTypography,
        content = content,
    )
}
