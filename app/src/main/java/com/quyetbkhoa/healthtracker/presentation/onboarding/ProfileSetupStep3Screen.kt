package com.quyetbkhoa.healthtracker.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import com.quyetbkhoa.healthtracker.domain.model.Goal

@Composable
fun ProfileSetupStep3Screen(
    uiState: ProfileSetupUiState,
    onAction: (ProfileSetupAction) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { OnboardingTopBar(step = 3, onBack = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.spaceLarge)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            Text(
                text = stringResource(id = R.string.onboarding_step3_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

            Text(
                text = stringResource(id = R.string.onboarding_step3_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge))

            // Activity Level
            Text(
                text = stringResource(id = R.string.onboarding_activity_level),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
            ActivityLevel.entries.forEach { level ->
                val textRes = when (level) {
                    ActivityLevel.SEDENTARY -> R.string.onboarding_activity_sedentary
                    ActivityLevel.LIGHT -> R.string.onboarding_activity_light
                    ActivityLevel.MODERATE -> R.string.onboarding_activity_moderate
                    ActivityLevel.VERY_ACTIVE -> R.string.onboarding_activity_active
                    ActivityLevel.EXTRA_ACTIVE -> R.string.onboarding_activity_extra_active
                }
                SelectableCard(
                    text = stringResource(id = textRes),
                    isSelected = uiState.activityLevel == level,
                    onClick = { onAction(ProfileSetupAction.UpdateActivityLevel(level)) }
                )
                Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            }

            Spacer(modifier = Modifier.height(Dimens.spaceLarge))

            // Goal
            Text(
                text = stringResource(id = R.string.onboarding_goal),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
            Goal.entries.forEach { goal ->
                val textRes = when (goal) {
                    Goal.LOSE_WEIGHT -> R.string.onboarding_goal_lose
                    Goal.MAINTAIN -> R.string.onboarding_goal_maintain
                    Goal.GAIN_WEIGHT -> R.string.onboarding_goal_gain
                }
                SelectableCard(
                    text = stringResource(id = textRes),
                    isSelected = uiState.goal == goal,
                    onClick = { onAction(ProfileSetupAction.UpdateGoal(goal)) }
                )
                Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            }

            Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge))

            // Terms
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAction(ProfileSetupAction.UpdateAcceptedTerms(!uiState.acceptedTerms)) }
                    .padding(vertical = Dimens.spaceSmall)
            ) {
                RadioButton(
                    selected = uiState.acceptedTerms,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                Column {
                    Text(
                        text = stringResource(id = R.string.onboarding_terms_prefix),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(id = R.string.onboarding_terms_highlight),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            if (uiState.termsError != null) {
                Text(
                    text = stringResource(id = uiState.termsError),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = Dimens.spaceExtraLarge)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge))

            // Finish Button
            HealthPrimaryButton(
                onClick = { onAction(ProfileSetupAction.SubmitStep3) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeightLarge)
                    .padding(bottom = Dimens.spaceLarge)
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_continue),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun SelectableCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.buttonHeightLarge)
            .border(
                width = Dimens.borderWidthThin,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = Shape.pill
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = Shape.pill
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spaceLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
