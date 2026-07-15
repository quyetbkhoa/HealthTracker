package com.quyetbkhoa.healthtracker.presentation.tdee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.domain.model.UserProfile

@Composable
fun TdeeResultScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: TdeeResultViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            if (event == TdeeResultEvent.NavigateToDashboard) onNavigateToDashboard()
        }
    }
    TdeeResultContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun TdeeResultContent(state: TdeeResultUiState, onAction: (TdeeResultAction) -> Unit) {
    when (state) {
        TdeeResultUiState.Loading -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) { CircularProgressIndicator() }

        is TdeeResultUiState.Success -> Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.spaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)
        ) {
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))
            Text(
                text = stringResource(R.string.tdee_result_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.tdee_result_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ResultMetricCard(
                label = stringResource(R.string.tdee_bmr),
                description = stringResource(R.string.tdee_bmr_description),
                value = stringResource(R.string.tdee_kcal_value, state.bmrCalories)
            )
            ResultMetricCard(
                label = stringResource(R.string.tdee_value),
                description = stringResource(R.string.tdee_description),
                value = stringResource(R.string.tdee_kcal_value, state.tdeeCalories)
            )
            HealthCard(
                shape = Shape.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.spaceLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                ) {
                    Text(
                        text = stringResource(R.string.tdee_target),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    OutlinedTextField(
                        value = state.targetInput,
                        onValueChange = { onAction(TdeeResultAction.UpdateTarget(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text(text = stringResource(R.string.unit_kcal)) },
                        isError = state.targetError,
                        supportingText = {
                            Text(
                                text = stringResource(
                                    if (state.targetError) R.string.tdee_invalid_target else R.string.tdee_target_hint
                                )
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            HealthPrimaryButton(
                onClick = { onAction(TdeeResultAction.Save) },
                modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeightMedium)
            ) {
                Text(text = stringResource(R.string.tdee_save))
            }
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))
        }
    }
}

@Composable
private fun ResultMetricCard(label: String, description: String, value: String) {
    HealthElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.extraLarge
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimens.spaceLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview
@Composable
private fun PreviewTdeeResultScreen() {
    HealthTrackerTheme {
        TdeeResultContent(
            state = TdeeResultUiState.Success(
                profile = UserProfile(),
                bmrCalories = 1_400,
                tdeeCalories = 1_900,
                targetInput = "1700"
            ),
            onAction = {}
        )
    }
}
