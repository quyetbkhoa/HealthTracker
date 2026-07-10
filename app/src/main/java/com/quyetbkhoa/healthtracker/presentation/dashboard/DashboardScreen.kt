package com.quyetbkhoa.healthtracker.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    if (uiState.isLoading || !uiState.hasProfile) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(
                    if (uiState.isLoading) R.string.dashboard_loading else R.string.dashboard_no_profile
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        DashboardHero(uiState, onNavigateToSettings)
        Column(
            modifier = Modifier.padding(Dimens.dashboardHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)
        ) {
            AdviceCard(uiState.remainingCalories)
            QuickAccess(onAction)
            TodayDetails(uiState)
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))
        }
    }
}

@Composable
private fun DashboardHero(uiState: DashboardUiState, onNavigateToSettings: () -> Unit) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"))
    val date = LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, d 'tháng' M yyyy", Locale.forLanguageTag("vi-VN"))
    ).replaceFirstChar { it.titlecase(Locale.forLanguageTag("vi-VN")) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = Dimens.dashboardHeroCorner, bottomEnd = Dimens.dashboardHeroCorner))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .statusBarsPadding()
            .padding(Dimens.dashboardHeroPadding)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_today_health),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                )
                Text(
                    text = stringResource(R.string.dashboard_date, date),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Box(
                modifier = Modifier
                    .size(Dimens.dashboardHeaderIconSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f))
                    .clickable(onClick = onNavigateToSettings),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.dashboard_food_emoji),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.spaceLarge))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
        ) {
            CalorieRing(
                progress = uiState.progress,
                remainingCalories = uiState.remainingCalories,
                isExceeded = uiState.isExceeded,
                modifier = Modifier.weight(1.2f).aspectRatio(1f)
            )
            Column(
                modifier = Modifier.weight(0.9f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
            ) {
                HeroMetric(R.string.dashboard_tdee, numberFormat.format(uiState.targetCalories), MaterialTheme.colorScheme.onPrimaryContainer)
                HeroMetric(R.string.dashboard_consumed, numberFormat.format(uiState.consumedCalories), MaterialTheme.colorScheme.secondaryContainer)
                HeroMetric(R.string.dashboard_burned, numberFormat.format(uiState.exerciseCalories), MaterialTheme.colorScheme.tertiaryContainer)
                HeroMetric(
                    if (uiState.isExceeded) R.string.dashboard_exceeded else R.string.dashboard_remaining_burn,
                    numberFormat.format(kotlin.math.abs(uiState.remainingCalories)),
                    if (uiState.isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
    }
}

@Composable
private fun CalorieRing(progress: Float, remainingCalories: Int, isExceeded: Boolean, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f)
    val progressColor = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer
    val outerStroke = Dimens.dashboardRingStroke
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outer = outerStroke.toPx()
            drawCircle(trackColor, style = Stroke(outer))
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(outer / 2, outer / 2),
                size = Size(size.width - outer, size.height - outer),
                style = Stroke(outer, cap = StrokeCap.Round)
            )
        }
        Text(
            text = stringResource(
                if (isExceeded) R.string.dashboard_ring_exceeded else R.string.dashboard_ring_remaining,
                kotlin.math.abs(remainingCalories)
            ),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun HeroMetric(labelRes: Int, value: String, dotColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
        Box(modifier = Modifier.size(Dimens.iconSizeSmall).clip(CircleShape).background(dotColor))
        Column {
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = stringResource(R.string.dashboard_kcal_value, value),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun AdviceCard(remainingCalories: Int) {
    val formatted = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(kotlin.math.abs(remainingCalories))
    DashboardCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)) {
            EmojiBox(R.string.dashboard_meal_emoji, MaterialTheme.colorScheme.surfaceContainerHigh)
            Column {
                Text(
                    text = stringResource(R.string.dashboard_advice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(
                        if (remainingCalories < 0) R.string.dashboard_advice_exceeded else R.string.dashboard_advice_message,
                        formatted
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickAccess(onAction: (DashboardAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)) {
        SectionTitle(R.string.dashboard_quick_access)
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)) {
            QuickCard(
                emojiRes = R.string.dashboard_food_emoji,
                titleRes = R.string.dashboard_add_meal,
                hintRes = R.string.dashboard_add_meal_hint,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                onClick = { onAction(DashboardAction.AddMeal) },
                modifier = Modifier.weight(1f)
            )
            QuickCard(
                emojiRes = R.string.dashboard_activity_emoji,
                titleRes = R.string.dashboard_add_activity,
                hintRes = R.string.dashboard_add_activity_hint,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = { onAction(DashboardAction.AddActivity) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickCard(emojiRes: Int, titleRes: Int, hintRes: Int, containerColor: Color, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier.height(Dimens.dashboardQuickCardHeight).clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.dashboardCardCorner),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(Dimens.dashboardCardElevation)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            EmojiBox(emojiRes, containerColor)
            Text(text = stringResource(titleRes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = stringResource(hintRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TodayDetails(uiState: DashboardUiState) {
    DashboardCard {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)) {
            SectionTitle(R.string.dashboard_today_details)
            ProgressMetric(
                R.string.dashboard_consumed,
                stringResource(R.string.dashboard_calorie_progress, uiState.consumedCalories.toString(), uiState.allowedCalories.toString()),
                uiState.progress,
                if (uiState.isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            ProgressMetric(
                R.string.dashboard_burned,
                stringResource(R.string.dashboard_kcal_value, uiState.exerciseCalories.toString()),
                if (uiState.targetCalories > 0) uiState.exerciseCalories.toFloat() / uiState.targetCalories else 0f,
                MaterialTheme.colorScheme.tertiaryContainer
            )
            ProgressMetric(R.string.dashboard_water, stringResource(R.string.dashboard_water_progress, "0", "2.5"), uiState.waterProgress, MaterialTheme.colorScheme.inversePrimary)
        }
    }
}

@Composable
private fun ProgressMetric(labelRes: Int, value: String, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(labelRes), style = MaterialTheme.typography.bodyLarge)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(modifier = Modifier.fillMaxWidth().height(Dimens.dashboardProgressHeight).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(Dimens.dashboardProgressHeight).clip(CircleShape).background(color))
        }
    }
}

@Composable
private fun DashboardCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.dashboardCardCorner),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(Dimens.dashboardCardElevation)
    ) {
        Box(modifier = Modifier.padding(Dimens.spaceLarge)) { content() }
    }
}

@Composable
private fun EmojiBox(emojiRes: Int, containerColor: Color) {
    Box(
        modifier = Modifier.size(Dimens.dashboardIconContainer).clip(RoundedCornerShape(Dimens.spaceMedium)).background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(emojiRes), style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun SectionTitle(titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
