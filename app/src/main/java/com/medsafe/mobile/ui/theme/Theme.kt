package com.medsafe.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = MedSafeGreen,
    onPrimary = MedSafeBackground,
    secondary = MedSafeAccent,
    background = MedSafeBackground,
    onBackground = MedSafeOnBackground,
    surface = MedSafeSurface,
    onSurface = MedSafeOnBackground,
)

private val DarkColors = darkColorScheme(
    primary = MedSafeGreenLight,
    onPrimary = MedSafeOnBackground,
    secondary = MedSafeAccent,
    background = MedSafeGreenDark,
    onBackground = MedSafeBackground,
    surface = MedSafeGreenDark,
    onSurface = MedSafeBackground,
)

@Composable
fun MedSafeSeniorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MedSafeTypography,
        content = content,
    )
}
