package com.quyetbkhoa.healthtracker.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class AppThemeType {
    LIGHT,
    DARK,
    PINK,
    SYSTEM
}

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = White,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = White,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = LightGreenBg,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = LightOutline
)

private val PinkColorScheme = lightColorScheme(
    primary = Pink_Primary,
    onPrimary = White,
    secondary = Pink_Primary,
    onSecondary = White,
    tertiary = Pink_Primary,
    onTertiary = White,
    background = Pink_LightGreenBg,
    onBackground = Pink_TextPrimary,
    surface = Pink_Surface,
    onSurface = Pink_TextPrimary,
    onSurfaceVariant = Pink_TextSecondary,
    outline = Pink_Outline
)

@Composable
fun HealthTrackerTheme(
    themeType: AppThemeType = AppThemeType.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeType) {
        AppThemeType.LIGHT -> LightColorScheme
        AppThemeType.DARK -> DarkColorScheme
        AppThemeType.PINK -> PinkColorScheme
        AppThemeType.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
