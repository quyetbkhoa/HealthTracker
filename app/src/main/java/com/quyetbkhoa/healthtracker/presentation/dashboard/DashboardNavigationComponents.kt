package com.quyetbkhoa.healthtracker.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.presentation.designsystem.Dimens
import com.quyetbkhoa.healthtracker.presentation.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.presentation.designsystem.Shape
import com.quyetbkhoa.healthtracker.presentation.designsystem.healthColors
import com.quyetbkhoa.healthtracker.presentation.designsystem.mealTypeColorPalette
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthIconText
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieStatus
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieEvaluation
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
internal fun DashboardBottomBar(
    onNavigateToSettings: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = Dimens.spaceLarge, topEnd = Dimens.spaceLarge),
        shadowElevation = Dimens.spaceSmall
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.spaceSmall),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationItem(R.string.dashboard_icon_home, R.string.dashboard_home, true, {})
            NavigationItem(R.string.dashboard_icon_meals_nav, R.string.dashboard_meals, false, onNavigateToMeals)
            NavigationItem(
                R.string.dashboard_icon_activity_nav,
                R.string.dashboard_activity,
                false,
                onNavigateToActivity
            )
            NavigationItem(R.string.dashboard_icon_statistics, R.string.dashboard_statistics, false, onNavigateToStatistics)
            NavigationItem(R.string.dashboard_icon_settings, R.string.dashboard_settings, false, onNavigateToSettings)
        }
    }
}

@Composable
internal fun RowScope.NavigationItem(iconRes: Int, labelRes: Int, selected: Boolean, onClick: () -> Unit) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val density = LocalDensity.current
    val screenWidthDp = with(density) {
        LocalWindowInfo.current.containerSize.width.toDp().value
    }
    val fontScale = density.fontScale
    val labelStyle = if (screenWidthDp < COMPACT_NAVIGATION_WIDTH_DP || fontScale > LARGE_FONT_SCALE) {
        MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp)
    } else {
        MaterialTheme.typography.labelMedium
    }
    Column(
        modifier = Modifier.weight(1f).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
    ) {
        HealthIconText(text = stringResource(iconRes), style = MaterialTheme.typography.headlineSmall, color = contentColor)
        Text(
            text = stringResource(labelRes),
            modifier = Modifier.fillMaxWidth(),
            style = labelStyle,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .width(Dimens.progressBarWidth)
                .height(Dimens.progressBarHeight)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                )
        )
    }
}

@Composable
internal fun DashboardCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    HealthElevatedCard(
        modifier = modifier,
        shape = Shape.extraLarge,
        content = { content() }
    )
}

internal fun formatNumber(value: Int): String = NumberFormat.getIntegerInstance(Locale.getDefault()).format(value)

internal const val COMPACT_NAVIGATION_WIDTH_DP = 360
internal const val LARGE_FONT_SCALE = 1.15f


@Composable
@Preview
internal fun PreviewDashboardScreen() {
    HealthTrackerTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                isLoading = false,
                hasProfile = true,
                targetCalories = 2_000,
                consumedCalories = 1_900,
                calorieEvaluation = DailyCalorieEvaluation(
                    status = DailyCalorieStatus.GOOD,
                    lowerBound = 1_800,
                    upperBound = 2_200,
                    caloriesToBoundary = 0
                ),
                userName = "Nguyễn An"
            ),
            onAction = {},
            onNavigateToSettings = {},
            onNavigateToActivityHistory = {},
            onNavigateToStatistics = {}
        )
    }
}
