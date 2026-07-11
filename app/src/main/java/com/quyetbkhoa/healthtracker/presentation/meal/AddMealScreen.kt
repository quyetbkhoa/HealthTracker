package com.quyetbkhoa.healthtracker.presentation.meal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.usecase.AddMealValidationError

@Composable
fun AddMealScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddMealViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailedMessage = stringResource(R.string.add_meal_save_failed)

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                AddMealUiEvent.Saved -> onSaved()
                AddMealUiEvent.SaveFailed -> snackbarHostState.showSnackbar(saveFailedMessage)
            }
        }
    }

    AddMealContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun AddMealContent(
    uiState: AddMealUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (AddMealAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)
        ) {
            AddMealHeader(onNavigateBack)
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                Text(
                    text = stringResource(R.string.add_meal_choose_type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                MealTypeSelector(uiState.mealType) {
                    onAction(AddMealAction.SelectMealType(it))
                }
            }
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { onAction(AddMealAction.UpdateName(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.add_meal_name)) },
                placeholder = { Text(stringResource(R.string.add_meal_name_hint)) },
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { error ->
                    { Text(addMealErrorText(error)) }
                },
                shape = Shape.medium
            )
            OutlinedTextField(
                value = uiState.calories,
                onValueChange = { onAction(AddMealAction.UpdateCalories(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.add_meal_calories)) },
                suffix = { Text(stringResource(R.string.add_meal_kcal)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.caloriesError != null,
                supportingText = uiState.caloriesError?.let { error ->
                    { Text(addMealErrorText(error)) }
                },
                shape = Shape.medium
            )
            Spacer(modifier = Modifier.weight(1f))
            HealthPrimaryButton(
                onClick = { onAction(AddMealAction.Save) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeightMedium),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimens.iconSizeMedium),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = Dimens.borderWidthThick
                    )
                } else {
                    Text(stringResource(R.string.add_meal_save))
                }
            }
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
        }
    }
}

@Composable
private fun AddMealHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.spaceMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
    ) {
        Card(
            modifier = Modifier
                .size(Dimens.buttonHeightMedium)
                .clickable(onClick = onNavigateBack),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevationMedium)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Column {
            Text(
                text = stringResource(R.string.add_meal_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.add_meal_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MealTypeSelector(selected: MealType, onSelected: (MealType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        MealType.entries.forEach { type ->
            val isSelected = selected == type
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(type) },
                shape = Shape.large,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) Dimens.cardElevationLarge else Dimens.cardElevationMedium
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.spaceMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                ) {
                    Text(text = mealTypeIcon(type), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = mealTypeLabel(type),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun mealTypeLabel(type: MealType): String = stringResource(
    when (type) {
        MealType.BREAKFAST -> R.string.add_meal_breakfast
        MealType.LUNCH -> R.string.add_meal_lunch
        MealType.DINNER -> R.string.add_meal_dinner
        MealType.SNACK -> R.string.add_meal_snack
    }
)

@Composable
private fun mealTypeIcon(type: MealType): String = stringResource(
    when (type) {
        MealType.BREAKFAST -> R.string.dashboard_icon_breakfast
        MealType.LUNCH -> R.string.dashboard_icon_lunch
        MealType.DINNER -> R.string.dashboard_icon_dinner
        MealType.SNACK -> R.string.dashboard_icon_snack
    }
)

@Composable
private fun addMealErrorText(error: AddMealValidationError): String = stringResource(
    when (error) {
        AddMealValidationError.EMPTY_NAME -> R.string.add_meal_error_empty_name
        AddMealValidationError.INVALID_CALORIES -> R.string.add_meal_error_invalid_calories
        AddMealValidationError.CALORIES_TOO_HIGH -> R.string.add_meal_error_calories_too_high
    }
)
