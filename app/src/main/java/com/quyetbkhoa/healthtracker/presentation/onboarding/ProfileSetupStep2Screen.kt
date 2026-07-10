package com.quyetbkhoa.healthtracker.presentation.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton

@Composable
fun ProfileSetupStep2Screen(
    uiState: ProfileSetupUiState,
    onAction: (ProfileSetupAction) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { OnboardingTopBar(step = 2, onBack = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.spaceLarge)
        ) {
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            Text(
                text = stringResource(id = R.string.onboarding_step2_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

            Text(
                text = stringResource(id = R.string.onboarding_step2_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge))

            // Weight
            Text(
                text = stringResource(id = R.string.onboarding_weight),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            OutlinedTextField(
                value = uiState.weightStr,
                onValueChange = { onAction(ProfileSetupAction.UpdateWeight(it)) },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.onboarding_placeholder_zero),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    Text(
                        text = stringResource(id = R.string.onboarding_weight_unit),
                        modifier = Modifier.padding(end = Dimens.spaceMedium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = Shape.pill,
                isError = uiState.weightError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            if (uiState.weightError != null) {
                Text(
                    text = stringResource(id = uiState.weightError),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = Dimens.spaceMedium, top = Dimens.spaceExtraSmall)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spaceLarge))

            // Height
            Text(
                text = stringResource(id = R.string.onboarding_height),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            OutlinedTextField(
                value = uiState.heightStr,
                onValueChange = { onAction(ProfileSetupAction.UpdateHeight(it)) },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.onboarding_placeholder_zero),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    Text(
                        text = stringResource(id = R.string.onboarding_height_unit),
                        modifier = Modifier.padding(end = Dimens.spaceMedium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = Shape.pill,
                isError = uiState.heightError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            if (uiState.heightError != null) {
                Text(
                    text = stringResource(id = uiState.heightError),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = Dimens.spaceMedium, top = Dimens.spaceExtraSmall)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            HealthPrimaryButton(
                onClick = { onAction(ProfileSetupAction.SubmitStep2) },
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

