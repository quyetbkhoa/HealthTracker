package com.quyetbkhoa.healthtracker.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class HealthSemanticColors(
    val meal: Color,
    val onMeal: Color,
    val mealContainer: Color,
    val onMealContainer: Color,
    val activity: Color,
    val onActivity: Color,
    val activityContainer: Color,
    val onActivityContainer: Color,
    val chartBlue: Color,
    val chartGreen: Color,
    val chartOrange: Color,
    val chartPurple: Color
)

internal val LightHealthColors = HealthSemanticColors(
    meal = Color(0xFF087A35),
    onMeal = Color(0xFFFFFFFF),
    mealContainer = Color(0xFFD6F8DF),
    onMealContainer = Color(0xFF062B16),
    activity = Color(0xFFB94B00),
    onActivity = Color(0xFFFFFFFF),
    activityContainer = Color(0xFFFFDBC8),
    onActivityContainer = Color(0xFF3B1000),
    chartBlue = Color(0xFF315DA8),
    chartGreen = Color(0xFF087A35),
    chartOrange = Color(0xFFB94B00),
    chartPurple = Color(0xFF7652A8)
)

internal val DarkHealthColors = HealthSemanticColors(
    meal = Color(0xFF70DC8F),
    onMeal = Color(0xFF003919),
    mealContainer = Color(0xFF075C2A),
    onMealContainer = Color(0xFFA0F5B6),
    activity = Color(0xFFFFB690),
    onActivity = Color(0xFF5F2300),
    activityContainer = Color(0xFF843600),
    onActivityContainer = Color(0xFFFFDBC8),
    chartBlue = Color(0xFFAFC6FF),
    chartGreen = Color(0xFF70DC8F),
    chartOrange = Color(0xFFFFB690),
    chartPurple = Color(0xFFD4BBFF)
)

internal val PinkHealthColors = HealthSemanticColors(
    meal = PinkPrimary,
    onMeal = PinkOnPrimary,
    mealContainer = Color(0xFFFFD9E5),
    onMealContainer = Color(0xFF3E001C),
    activity = Color(0xFFC02D70),
    onActivity = Color(0xFFFFFFFF),
    activityContainer = Color(0xFFFFD8E9),
    onActivityContainer = Color(0xFF43001F),
    chartBlue = Color(0xFF9B2460),
    chartGreen = Color(0xFFB53A74),
    chartOrange = Color(0xFFD05A88),
    chartPurple = Color(0xFF7E3B72)
)

internal val LocalHealthColors = staticCompositionLocalOf { LightHealthColors }

val MaterialTheme.healthColors: HealthSemanticColors
    @Composable get() = LocalHealthColors.current
