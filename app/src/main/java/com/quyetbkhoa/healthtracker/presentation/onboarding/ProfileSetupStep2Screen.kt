package com.quyetbkhoa.healthtracker.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import com.quyetbkhoa.healthtracker.domain.model.Goal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProfileSetupStep2Screen(
    uiState: ProfileSetupUiState,
    onAction: (ProfileSetupAction) -> Unit,
    onBack: () -> Unit
) {
    var showEstimateInfo by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { OnboardingSetupHeader(step = 2, onBack = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(horizontal = Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)
        ) {
            OnboardingSectionCard {
                Text(text = stringResource(R.string.onboarding_activity_section), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                    ActivityLevel.entries.forEach { level ->
                        ActivityOption(level, uiState.activityLevel == level, Modifier.weight(1f)) { onAction(ProfileSetupAction.UpdateActivityLevel(level)) }
                    }
                }
            }
            OnboardingSectionCard {
                Text(text = stringResource(R.string.onboarding_goal_section), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                    Goal.entries.forEach { goal ->
                        GoalOption(goal, uiState.goal == goal, Modifier.weight(1f)) { onAction(ProfileSetupAction.UpdateGoal(goal)) }
                    }
                }
            }
            OnboardingSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.onboarding_estimate_section), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = { showEstimateInfo = true }) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = stringResource(R.string.onboarding_estimate_section), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    EstimateMetric(R.string.dashboard_icon_burned, R.string.onboarding_bmr, uiState.estimatedBmr, Modifier.weight(1f))
                    EstimateMetric(R.string.onboarding_icon_extra_active, R.string.onboarding_tdee, uiState.estimatedTdee, Modifier.weight(1f))
                    EstimateMetric(R.string.dashboard_icon_goal, R.string.onboarding_calorie_target, uiState.estimatedTarget, Modifier.weight(1f))
                }
            }
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAction(ProfileSetupAction.UpdateAcceptedTerms(!uiState.acceptedTerms))
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = uiState.acceptedTerms, onCheckedChange = null)
                    Text(
                        text = "${stringResource(R.string.onboarding_terms_prefix)} ${stringResource(R.string.onboarding_terms_highlight)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                uiState.termsError?.let { errorRes ->
                    Text(
                        text = stringResource(errorRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            HealthPrimaryButton(
                onClick = { onAction(ProfileSetupAction.SubmitProfile) },
                modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeightLarge).padding(bottom = Dimens.spaceLarge),
                enabled = !uiState.isSubmitting
            ) {
                Text(text = stringResource(R.string.onboarding_complete), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (showEstimateInfo) {
        AlertDialog(
            onDismissRequest = { showEstimateInfo = false },
            confirmButton = { TextButton(onClick = { showEstimateInfo = false }) { Text(stringResource(R.string.onboarding_ok)) } },
            title = { Text(stringResource(R.string.onboarding_estimate_section)) },
            text = { Text(stringResource(R.string.onboarding_estimate_note)) }
        )
    }
}

@Composable
private fun OnboardingSectionCard(content: @Composable ColumnScope.() -> Unit) {
    HealthElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.extraLarge
    ) {
        Column(modifier = Modifier.padding(Dimens.spaceMedium), content = content)
    }
}

@Composable
private fun ActivityOption(level: ActivityLevel, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val (iconRes, labelRes) = when (level) {
        ActivityLevel.SEDENTARY -> R.string.onboarding_icon_sedentary to R.string.onboarding_activity_short_sedentary
        ActivityLevel.LIGHT -> R.string.onboarding_icon_light to R.string.onboarding_activity_short_light
        ActivityLevel.MODERATE -> R.string.onboarding_icon_moderate to R.string.onboarding_activity_short_moderate
        ActivityLevel.VERY_ACTIVE -> R.string.onboarding_icon_active to R.string.onboarding_activity_short_active
        ActivityLevel.EXTRA_ACTIVE -> R.string.onboarding_icon_extra_active to R.string.onboarding_activity_short_extra_active
    }
    SelectionCard(iconRes, labelRes, null, isSelected, OnboardingDimens.activityCardHeight, modifier, onClick)
}

@Composable
private fun GoalOption(goal: Goal, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val (iconRes, labelRes, descriptionRes) = when (goal) {
        Goal.LOSE_WEIGHT -> Triple(R.string.onboarding_icon_height, R.string.onboarding_goal_lose, R.string.onboarding_goal_lose_description)
        Goal.MAINTAIN -> Triple(R.string.onboarding_icon_weight, R.string.onboarding_goal_maintain, R.string.onboarding_goal_maintain_description)
        Goal.GAIN_WEIGHT -> Triple(R.string.onboarding_icon_extra_active, R.string.onboarding_goal_gain, R.string.onboarding_goal_gain_description)
    }
    SelectionCard(iconRes, labelRes, descriptionRes, isSelected, OnboardingDimens.selectionCardHeight, modifier, onClick)
}

@Composable
private fun SelectionCard(iconRes: Int, titleRes: Int, descriptionRes: Int?, isSelected: Boolean, height: androidx.compose.ui.unit.Dp, modifier: Modifier, onClick: () -> Unit) {
    HealthElevatedCard(
        modifier = modifier.height(height).clickable(onClick = onClick),
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(Dimens.spaceSmall), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
            Box(modifier = Modifier.size(Dimens.buttonHeightMedium).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                Text(text = stringResource(iconRes), style = MaterialTheme.typography.titleLarge)
            }
            Box(modifier = Modifier.height(Dimens.buttonHeightMedium), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(titleRes),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (descriptionRes != null) {
                Box(modifier = Modifier.height(Dimens.buttonHeightMedium), contentAlignment = Alignment.TopCenter) {
                    Text(
                        text = stringResource(descriptionRes),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EstimateMetric(iconRes: Int, labelRes: Int, value: Int, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
        Box(modifier = Modifier.size(Dimens.buttonHeightMedium).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
            Text(text = stringResource(iconRes), style = MaterialTheme.typography.titleLarge)
        }
        Text(text = stringResource(labelRes), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(text = formatNumber(value), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = stringResource(R.string.onboarding_kcal_daily), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatNumber(value: Int): String = NumberFormat.getIntegerInstance(Locale.getDefault()).format(value)

@Composable
fun SelectableCard(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.buttonHeightLarge)
            .border(Dimens.borderWidthThin, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, Shape.large)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spaceLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier.size(Dimens.selectionIndicatorSize),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewProfileSetupStep2Screen() {
    HealthTrackerTheme {
        ProfileSetupStep2Screen(
            uiState = ProfileSetupUiState(estimatedBmr = 1_400, estimatedTdee = 1_900, estimatedTarget = 1_700),
            onAction = {},
            onBack = {}
        )
    }
}
