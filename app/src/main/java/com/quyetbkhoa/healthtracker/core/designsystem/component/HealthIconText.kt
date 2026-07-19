package com.quyetbkhoa.healthtracker.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import com.quyetbkhoa.healthtracker.core.designsystem.LocalAppFontScale

/** Emoji and glyph icon whose size is independent from the in-app text-size setting. */
@Composable
fun HealthIconText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    val currentDensity = LocalDensity.current
    val appFontScale = LocalAppFontScale.current
    val iconDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale / appFontScale
    )
    CompositionLocalProvider(LocalDensity provides iconDensity) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            color = color,
            fontWeight = fontWeight,
            maxLines = 1
        )
    }
}
