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
internal fun ActivityLevelSuggestionCard(uiState: DashboardUiState, onOpenSettings: () -> Unit) {
    val level = uiState.suggestedActivityLevel ?: return
    val levelName = stringResource(
        when (level) {
            com.quyetbkhoa.healthtracker.domain.model.ActivityLevel.SEDENTARY -> R.string.onboarding_activity_short_sedentary
            com.quyetbkhoa.healthtracker.domain.model.ActivityLevel.LIGHT -> R.string.onboarding_activity_short_light
            com.quyetbkhoa.healthtracker.domain.model.ActivityLevel.MODERATE -> R.string.onboarding_activity_short_moderate
            com.quyetbkhoa.healthtracker.domain.model.ActivityLevel.VERY_ACTIVE -> R.string.onboarding_activity_short_active
            com.quyetbkhoa.healthtracker.domain.model.ActivityLevel.EXTRA_ACTIVE -> R.string.onboarding_activity_short_extra_active
        }
    )
    DashboardCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenSettings)) {
        Column(modifier = Modifier.padding(Dimens.spaceMedium), verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
            Text(stringResource(R.string.dashboard_activity_suggestion_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.dashboard_activity_suggestion_message, levelName, formatNumber(uiState.suggestedTdeeCalories)),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun DashboardLoadingState(isLoading: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(if (isLoading) R.string.dashboard_loading else R.string.dashboard_no_profile),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
internal fun DashboardHeader(userName: String) {
    val locale = LocalConfiguration.current.locales[0]
    val formattedDate = LocalDate.now()
        .format(DateTimeFormatter.ofPattern(stringResource(R.string.dashboard_date_pattern), locale))
        .replaceFirstChar { it.titlecase(locale) }
    val displayName = userName.substringBeforeLast(' ').ifBlank { stringResource(R.string.dashboard_guest_name) }
    val greeting = buildAnnotatedString {
        append(stringResource(R.string.dashboard_greeting, ""))
        withStyle(SpanStyle(color = MaterialTheme.healthColors.meal)) {
            append(displayName)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = greeting,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            marquee = true
        )
        DashboardBrandMark()
        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
        Text(
            text = stringResource(R.string.dashboard_date_with_icon, formattedDate),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.healthColors.meal,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            marquee = true
        )
    }
}

/** Reserved visual slot for the dashboard brand asset. */
@Composable
internal fun DashboardBrandMark() {
    Box(
        modifier = Modifier.size(Dimens.iconSizeLarge),
        contentAlignment = Alignment.Center
    ) {
        HealthIconText(
            text = stringResource(R.string.dashboard_brand_mark),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
internal fun CalorieOverviewCard(uiState: DashboardUiState) {
    val isExceeded = uiState.isExceeded
    val containerColor = if (isExceeded) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.healthColors.mealContainer
    }
    val contentColor = if (isExceeded) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.healthColors.onMealContainer
    }
    HealthElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DashboardDimens.overviewHeight)
                .padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoalRing(
                targetCalories = uiState.targetCalories,
                progress = uiState.progress,
                isExceeded = isExceeded,
                contentColor = contentColor,
                modifier = Modifier.size(DashboardDimens.goalRingSize)
            )
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
            ) {
                OverviewMetric(
                    R.string.dashboard_icon_eaten,
                    R.string.dashboard_consumed,
                    uiState.consumedCalories,
                    contentColor = contentColor
                )
                DashboardSubtleDivider()
                OverviewMetric(
                    R.string.dashboard_icon_burned,
                    R.string.dashboard_burned,
                    uiState.exerciseCalories,
                    contentColor = contentColor
                )
                DashboardSubtleDivider()
                OverviewMetric(
                    R.string.dashboard_icon_remaining,
                    if (isExceeded) R.string.dashboard_exceeded else R.string.dashboard_remaining,
                    abs(uiState.remainingCalories),
                    contentColor = contentColor
                )
            }
        }
    }
}

@Composable
internal fun DashboardSubtleDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    )
}

@Composable
internal fun GoalRing(
    targetCalories: Int,
    progress: Float,
    isExceeded: Boolean,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val progressColor = if (isExceeded) scheme.error else MaterialTheme.healthColors.meal
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = DashboardDimens.progressStroke.toPx()
            drawCircle(
                color = contentColor.copy(alpha = 0.16f),
                style = Stroke(stroke),
                radius = (size.minDimension-stroke)/2f
                )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = progress.coerceIn(0f, 1f) * 360f,
                useCenter = false,
                topLeft = Offset(
                    x = stroke/2f,
                    y = stroke/2f
                ),
                size = Size(size.width-stroke , size.height-stroke ),
                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.dashboard_goal), style = MaterialTheme.typography.labelLarge, color = contentColor)
            Text(text = formatNumber(targetCalories), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = contentColor)

        }
    }
}

@Composable
internal fun OverviewMetric(
    iconRes: Int,
    labelRes: Int,
    value: Int,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(labelRes),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.78f),
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatNumber(value),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            HealthIconText(text = stringResource(iconRes), style = MaterialTheme.typography.displaySmall, color = contentColor)

    }
}

@Composable
internal fun CalorieAdviceCard(uiState: DashboardUiState) {
    val evaluation = uiState.calorieEvaluation
    var isInfoVisible by remember { mutableStateOf(false) }
    val isOnTarget = evaluation.status == DailyCalorieStatus.GOOD
    val containerColor = if (isOnTarget) {
        MaterialTheme.healthColors.mealContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (isOnTarget) {
        MaterialTheme.healthColors.onMealContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    val advice = when (evaluation.status) {
        DailyCalorieStatus.NEEDS_MORE -> DashboardAdvice(
            title = stringResource(R.string.dashboard_advice_need_more_title),
            detail = stringResource(
                R.string.dashboard_advice_short_need_more_detail,
                formatNumber(evaluation.caloriesToBoundary)
            )
        )
        DailyCalorieStatus.GOOD -> DashboardAdvice(
            title = stringResource(R.string.dashboard_advice_good_title),
            detail = stringResource(R.string.dashboard_advice_short_good_detail)
        )
        DailyCalorieStatus.EXCEEDED -> DashboardAdvice(
            title = stringResource(R.string.dashboard_advice_exceeded_title),
            detail = stringResource(
                R.string.dashboard_advice_short_exceeded_detail,
                formatNumber(evaluation.caloriesToBoundary)
            )
        )
    }
    HealthElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.extraLarge,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.spaceMedium, top = Dimens.spaceSmall, bottom = Dimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HealthIconText(
                text = stringResource(if (isOnTarget) R.string.dashboard_mood_happy else R.string.dashboard_mood_sad),
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)) {
                Text(
                    text = advice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = advice.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.78f)
                )
            }
            IconButton(onClick = { isInfoVisible = true }) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.dashboard_target_range_info),
                    tint = contentColor
                )
            }
        }
    }

    if (isInfoVisible) {
        AlertDialog(
            onDismissRequest = { isInfoVisible = false },
            icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
            title = { Text(stringResource(R.string.dashboard_target_range_info_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.dashboard_target_range_info_message,
                        formatNumber(evaluation.lowerBound),
                        formatNumber(evaluation.upperBound),
                        formatNumber(evaluation.upperBound)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { isInfoVisible = false }) {
                    Text(stringResource(R.string.dashboard_target_range_info_confirm))
                }
            }
        )
    }
}

internal data class DashboardAdvice(
    val title: String,
    val detail: String
)
