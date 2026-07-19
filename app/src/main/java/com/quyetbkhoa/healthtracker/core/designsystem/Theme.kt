package com.quyetbkhoa.healthtracker.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

enum class AppThemeType {
    LIGHT,
    DARK,
    PINK,
    SYSTEM
}

enum class AppFontSize(val scale: Float) {
    SMALL(0.9f),
    MEDIUM(1f),
    LARGE(1.1f)
}

internal val LocalAppFontScale = staticCompositionLocalOf { 1f }

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceDim = LightSurfaceDim,
    surfaceBright = LightSurfaceBright,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = DarkPrimary,
    scrim = Scrim,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceDim = DarkSurfaceDim,
    surfaceBright = DarkSurfaceBright,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = LightPrimary,
    scrim = Scrim,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

private val PinkColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = PinkOnPrimary,
    primaryContainer = PinkPrimaryContainer,
    onPrimaryContainer = PinkOnPrimaryContainer,
    secondary = PinkSecondary,
    onSecondary = PinkOnSecondary,
    secondaryContainer = PinkSecondaryContainer,
    onSecondaryContainer = PinkOnSecondaryContainer,
    tertiary = PinkTertiary,
    onTertiary = PinkOnTertiary,
    tertiaryContainer = PinkTertiaryContainer,
    onTertiaryContainer = PinkOnTertiaryContainer,
    background = PinkBackground,
    onBackground = PinkOnBackground,
    surface = PinkSurface,
    onSurface = PinkOnSurface,
    surfaceVariant = PinkSurfaceVariant,
    onSurfaceVariant = PinkOnSurfaceVariant,
    outline = PinkOutline,
    outlineVariant = PinkOutlineVariant,
    surfaceContainer = PinkSurfaceContainer,
    surfaceContainerHigh = PinkSurfaceContainerHigh,
    surfaceDim = PinkSurfaceDim,
    surfaceBright = PinkSurfaceBright,
    surfaceContainerLowest = PinkSurfaceContainerLowest,
    surfaceContainerLow = PinkSurfaceContainerLow,
    surfaceContainerHighest = PinkSurfaceContainerHighest,
    inverseSurface = PinkInverseSurface,
    inverseOnSurface = PinkInverseOnSurface,
    inversePrimary = DarkPrimary,
    scrim = Scrim,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

@Composable
fun HealthTrackerTheme(
    themeType: AppThemeType = AppThemeType.SYSTEM,
    fontSize: AppFontSize = AppFontSize.MEDIUM,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val systemDensity = LocalDensity.current
    val appDensity = Density(
        density = systemDensity.density,
        fontScale = systemDensity.fontScale * fontSize.scale
    )
    val (colorScheme, healthColors) = when (themeType) {
        AppThemeType.LIGHT -> LightColorScheme to LightHealthColors
        AppThemeType.DARK -> DarkColorScheme to DarkHealthColors
        AppThemeType.PINK -> PinkColorScheme to PinkHealthColors
        AppThemeType.SYSTEM -> if (isDark) {
            DarkColorScheme to DarkHealthColors
        } else {
            LightColorScheme to LightHealthColors
        }
    }
    CompositionLocalProvider(
        LocalHealthColors provides healthColors,
        LocalAppFontScale provides fontSize.scale,
        LocalDensity provides appDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
