package com.quyetbkhoa.healthtracker.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import com.quyetbkhoa.healthtracker.domain.model.MealType

@Immutable
data class MealTypeColorPalette(
    val container: Color,
    val content: Color,
    val accent: Color
)

@Composable
fun mealTypeColorPalette(type: MealType): MealTypeColorPalette = when (type) {
    MealType.BREAKFAST -> MealTypeColorPalette(
        container = MaterialTheme.colorScheme.tertiaryContainer,
        content = MaterialTheme.colorScheme.onTertiaryContainer,
        accent = MaterialTheme.colorScheme.tertiary
    )
    MealType.LUNCH -> MealTypeColorPalette(
        container = MaterialTheme.healthColors.mealContainer,
        content = MaterialTheme.healthColors.onMealContainer,
        accent = MaterialTheme.healthColors.meal
    )
    MealType.DINNER -> MealTypeColorPalette(
        container = MaterialTheme.colorScheme.primaryContainer,
        content = MaterialTheme.colorScheme.onPrimaryContainer,
        accent = MaterialTheme.colorScheme.primary
    )
    MealType.SNACK -> MealTypeColorPalette(
        container = MaterialTheme.colorScheme.secondaryContainer,
        content = MaterialTheme.colorScheme.onSecondaryContainer,
        accent = MaterialTheme.colorScheme.secondary
    )
}
