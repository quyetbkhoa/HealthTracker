package com.quyetbkhoa.healthtracker.presentation.meal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.domain.model.Food
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.usecase.AddMealValidationError
import java.text.NumberFormat

@Composable
fun AddMealScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddMealViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val failedMessage = stringResource(R.string.add_meal_save_failed)
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                AddMealUiEvent.Saved -> onSaved()
                AddMealUiEvent.SaveFailed -> snackbar.showSnackbar(failedMessage)
            }
        }
    }
    AddMealContent(state, snackbar, viewModel::onAction, onNavigateBack)
}

@Composable
private fun AddMealContent(
    state: AddMealUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (AddMealAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
        ) {
            item { ScreenHeader(onNavigateBack) }
            item { MealTypeSelector(state.mealType, onAction) }
            item { ModeSelector(state.isCustom, onAction) }

            if (!state.isCustom && state.selectedFood == null) {
                item {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { onAction(AddMealAction.UpdateQuery(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.food_search_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        shape = Shape.large
                    )
                }
                if (state.foods.isEmpty()) {
                    item { EmptyFoodCard { onAction(AddMealAction.SetCustomMode(true)) } }
                } else {
                    items(state.foods, key = Food::id) { food ->
                        FoodRow(food) { onAction(AddMealAction.SelectFood(food)) }
                    }
                }
            } else {
                item { MealDetailsForm(state, onAction) }
                item {
                    HealthPrimaryButton(
                        onClick = { onAction(AddMealAction.Save) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.buttonHeightMedium),
                        enabled = !state.isSaving && state.estimatedCalories != null
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimens.iconSizeMedium),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.add_meal_save))
                        }
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.food_nutrition_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { Spacer(Modifier.height(Dimens.spaceLarge)) }
        }
    }
}

@Composable
private fun ScreenHeader(onNavigateBack: () -> Unit) {
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_back))
            }
        }
        Column {
            Text(
                stringResource(R.string.add_meal_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.add_meal_catalog_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModeSelector(isCustom: Boolean, onAction: (AddMealAction) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
        ModeCard(
            label = stringResource(R.string.food_catalog_tab),
            selected = !isCustom,
            modifier = Modifier.weight(1f)
        ) { onAction(AddMealAction.SetCustomMode(false)) }
        ModeCard(
            label = stringResource(R.string.food_custom_tab),
            selected = isCustom,
            modifier = Modifier.weight(1f)
        ) { onAction(AddMealAction.SetCustomMode(true)) }
    }
}

@Composable
private fun ModeCard(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(Dimens.buttonHeightLarge)
            .clickable(onClick = onClick),
        shape = Shape.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.spaceSmall),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun FoodRow(food: Food, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Shape.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(Dimens.cardElevationMedium)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.food_default_grams, formatDecimal(food.defaultServingGrams)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                stringResource(
                    R.string.food_kcal_per_100g_value,
                    formatDecimal(food.caloriesPer100Grams)
                ),
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Default.Add,
                stringResource(R.string.food_select),
                modifier = Modifier.padding(start = Dimens.spaceSmall),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyFoodCard(onCustom: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.food_empty_search))
            TextButton(onClick = onCustom) { Text(stringResource(R.string.food_enter_custom)) }
        }
    }
}

@Composable
private fun MealDetailsForm(state: AddMealUiState, onAction: (AddMealAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)) {
        state.selectedFood?.let { food ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spaceMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(food.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(
                                R.string.food_kcal_per_100g_value,
                                formatDecimal(food.caloriesPer100Grams)
                            )
                        )
                    }
                    TextButton(
                        onClick = { onAction(AddMealAction.ChooseAgain) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.food_choose_again),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        if (state.isCustom) {
            OutlinedTextField(
                value = state.customName,
                onValueChange = { onAction(AddMealAction.UpdateCustomName(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.add_meal_name)) },
                singleLine = true,
                isError = state.validationError == AddMealValidationError.EMPTY_NAME
            )
            OutlinedTextField(
                value = state.caloriesPer100Grams,
                onValueChange = { onAction(AddMealAction.UpdateCaloriesPer100Grams(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.food_kcal_per_100g)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = state.validationError == AddMealValidationError.INVALID_CALORIES_PER_100_GRAMS
            )
        }
        OutlinedTextField(
            value = state.consumedGrams,
            onValueChange = { onAction(AddMealAction.UpdateConsumedGrams(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.food_consumed_grams)) },
            suffix = { Text(stringResource(R.string.unit_grams)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            isError = state.validationError == AddMealValidationError.INVALID_GRAMS
        )
        state.validationError?.let { error ->
            Text(
                text = validationMessage(error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(Dimens.spaceLarge)) {
                Text(stringResource(R.string.food_estimated_energy))
                Text(
                    stringResource(R.string.food_estimated_kcal, state.estimatedCalories ?: 0),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun MealTypeSelector(selected: MealType, onAction: (AddMealAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
        Text(stringResource(R.string.add_meal_choose_type), fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
            MealType.entries.forEach { type ->
                ModeCard(
                    label = mealTypeLabel(type),
                    selected = selected == type,
                    modifier = Modifier.weight(1f)
                ) { onAction(AddMealAction.SelectMealType(type)) }
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
private fun validationMessage(error: AddMealValidationError): String = stringResource(
    when (error) {
        AddMealValidationError.EMPTY_NAME -> R.string.add_meal_error_empty_name
        AddMealValidationError.INVALID_GRAMS -> R.string.add_meal_error_invalid_grams
        AddMealValidationError.INVALID_CALORIES_PER_100_GRAMS ->
            R.string.add_meal_error_invalid_per_100g
        AddMealValidationError.CALORIES_TOO_HIGH -> R.string.add_meal_error_calories_too_high
    }
)

private fun formatDecimal(value: Double): String =
    NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }.format(value)

@Preview
@Composable
private fun PreviewAddMealScreen() {
    HealthTrackerTheme {
        AddMealContent(
            state = AddMealUiState(
                foods = listOf(Food(1L, "Cơm trắng", 130.0, 100.0, 1))
            ),
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
