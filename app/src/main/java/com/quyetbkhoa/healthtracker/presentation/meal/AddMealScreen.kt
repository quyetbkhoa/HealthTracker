package com.quyetbkhoa.healthtracker.presentation.meal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthNumericSlider
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthOutlinedCard
import com.quyetbkhoa.healthtracker.core.designsystem.healthColors
import com.quyetbkhoa.healthtracker.core.designsystem.mealTypeColorPalette
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
    val favoriteFailedMessage = stringResource(R.string.add_meal_favorite_failed)

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                AddMealUiEvent.Saved -> onSaved()
                AddMealUiEvent.SaveFailed -> snackbar.showSnackbar(failedMessage)
                AddMealUiEvent.FavoriteFailed -> snackbar.showSnackbar(favoriteFailedMessage)
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(horizontal = Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
        ) {
            AddMealHeader(onNavigateBack)
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                state.isFoodPickerExpanded -> {
                    FoodPickerHeader()
                    FoodSearchField(state.query, onAction)
                    FoodGrid(state.foods, onAction)
                }
                else -> MealDetailsForm(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                )
            }
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
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_back))
        }
        Column {
            Text(
                text = stringResource(R.string.add_meal_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.add_meal_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FoodPickerHeader() {
    var isInfoVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.add_meal_choose_food),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { isInfoVisible = true }) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.add_meal_info),
                tint = MaterialTheme.healthColors.meal
            )
        }
    }

    if (isInfoVisible) {
        AlertDialog(
            onDismissRequest = { isInfoVisible = false },
            icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
            title = { Text(stringResource(R.string.add_meal_info_title)) },
            text = { Text(stringResource(R.string.add_meal_info_message)) },
            confirmButton = {
                TextButton(onClick = { isInfoVisible = false }) {
                    Text(stringResource(R.string.add_meal_info_confirm))
                }
            }
        )
    }
}

@Composable
private fun FoodSearchField(query: String, onAction: (AddMealAction) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = { onAction(AddMealAction.UpdateQuery(it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.food_search_hint)) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.healthColors.meal
            )
        },
        singleLine = true,
        shape = Shape.large
    )
}

@Composable
private fun FoodGrid(foods: List<Food>, onAction: (AddMealAction) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        if (foods.isEmpty()) {
            item(key = EMPTY_RESULT_KEY, span = { GridItemSpan(maxLineSpan) }) {
                EmptySearchCard()
            }
        }
        items(foods, key = Food::id) { food ->
            FoodChoiceCard(
                food = food,
                onClick = { onAction(AddMealAction.SelectFood(food)) },
                onToggleFavorite = { onAction(AddMealAction.ToggleFavorite(food.id)) }
            )
        }
        item(key = OTHER_FOOD_KEY) {
            OtherFoodCard { onAction(AddMealAction.SelectOther) }
        }
        item(key = BOTTOM_SPACER_KEY, span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(Dimens.spaceMedium))
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun FoodChoiceCard(
    food: Food,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    HealthOutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.activityChoiceCardHeight)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleFavorite()
                }
            ),
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.healthColors.mealContainer,
            contentColor = MaterialTheme.healthColors.onMealContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.spaceSmall)
        ) {
            Icon(
                imageVector = if (food.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = stringResource(
                    if (food.isFavorite) R.string.add_meal_favorite
                    else R.string.add_meal_not_favorite
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(Dimens.iconSizeMedium),
                tint = if (food.isFavorite) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.spaceSmall, vertical = Dimens.spaceMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    Dimens.spaceSmall,
                    Alignment.CenterVertically
                )
            ) {
                Text(
                    text = food.name,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.healthColors.onMealContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.food_kcal_per_100g_value,
                        formatDecimal(food.caloriesPer100Grams)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.healthColors.onMealContainer.copy(alpha = 0.76f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.food_default_grams_short,
                        formatDecimal(food.defaultServingGrams)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.healthColors.meal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OtherFoodCard(onClick: () -> Unit) {
    HealthOutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.activityChoiceCardHeight)
            .clickable(onClick = onClick),
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.healthColors.mealContainer,
            contentColor = MaterialTheme.healthColors.onMealContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.spaceMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                Dimens.spaceSmall,
                Alignment.CenterVertically
            )
        ) {
            Text(
                text = stringResource(R.string.common_other),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.food_other_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.healthColors.onMealContainer.copy(alpha = 0.76f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptySearchCard() {
    HealthCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.food_empty_search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMedium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MealDetailsForm(
    state: AddMealUiState,
    onAction: (AddMealAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
    ) {
        SelectedFoodCard(state, onAction)
        MealTypeSelector(state.mealType, onAction)
        if (state.isCustom) {
            CustomFoodFields(state, onAction)
        }
        Text(
            text = stringResource(R.string.add_meal_amount_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        HealthNumericSlider(
            label = stringResource(R.string.food_consumed_grams),
            value = state.consumedGrams,
            onValueChange = { onAction(AddMealAction.UpdateConsumedGrams(it)) },
            valueRange = 5f..1_000f,
            unit = stringResource(R.string.unit_grams),
            step = 5f,
            errorText = if (state.validationError == AddMealValidationError.INVALID_GRAMS) {
                validationMessage(state.validationError)
            } else {
                null
            },
            accentColor = MaterialTheme.healthColors.meal,
            accentContainerColor = MaterialTheme.healthColors.mealContainer,
            onAccentContainerColor = MaterialTheme.healthColors.onMealContainer
        )
        state.validationError?.takeIf {
            it != AddMealValidationError.INVALID_GRAMS &&
                it != AddMealValidationError.INVALID_CALORIES_PER_100_GRAMS
        }?.let { error ->
            Text(
                text = validationMessage(error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        CaloriesEstimate(state.estimatedCalories ?: 0)
        Text(
            text = stringResource(R.string.food_nutrition_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HealthPrimaryButton(
            onClick = { onAction(AddMealAction.Save) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.buttonHeightMedium),
            enabled = state.canSave,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.healthColors.meal,
                contentColor = MaterialTheme.healthColors.onMeal
            )
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.iconSizeMedium),
                    color = MaterialTheme.healthColors.onMeal
                )
            } else {
                Text(stringResource(R.string.add_meal_save))
            }
        }
        Spacer(Modifier.height(Dimens.spaceMedium))
    }
}

@Composable
private fun SelectedFoodCard(state: AddMealUiState, onAction: (AddMealAction) -> Unit) {
    val food = state.selectedFood
    HealthOutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food?.name ?: stringResource(R.string.common_other),
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = food?.let {
                        stringResource(
                            R.string.food_kcal_per_100g_value,
                            formatDecimal(it.caloriesPer100Grams)
                        )
                    } ?: stringResource(R.string.food_other_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = { onAction(AddMealAction.ReselectFood) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.healthColors.meal
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

@Composable
private fun CustomFoodFields(state: AddMealUiState, onAction: (AddMealAction) -> Unit) {
    OutlinedTextField(
        value = state.customName,
        onValueChange = { onAction(AddMealAction.UpdateCustomName(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.add_meal_name)) },
        placeholder = { Text(stringResource(R.string.add_meal_name_hint)) },
        singleLine = true,
        isError = state.validationError == AddMealValidationError.EMPTY_NAME
    )
    HealthNumericSlider(
        label = stringResource(R.string.food_kcal_per_100g),
        value = state.caloriesPer100Grams,
        onValueChange = { onAction(AddMealAction.UpdateCaloriesPer100Grams(it)) },
        valueRange = 1f..2_000f,
        unit = stringResource(R.string.unit_kcal),
        step = 5f,
        errorText = if (
            state.validationError == AddMealValidationError.INVALID_CALORIES_PER_100_GRAMS
        ) {
            validationMessage(state.validationError)
        } else {
            null
        },
        accentColor = MaterialTheme.healthColors.meal,
        accentContainerColor = MaterialTheme.healthColors.mealContainer,
        onAccentContainerColor = MaterialTheme.healthColors.onMealContainer
    )
}

@Composable
private fun CaloriesEstimate(calories: Int) {
    HealthCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.food_estimated_energy))
            Text(
                text = stringResource(R.string.food_estimated_kcal, calories),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun MealTypeSelector(selected: MealType, onAction: (AddMealAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
        Text(stringResource(R.string.add_meal_choose_type), fontWeight = FontWeight.Bold)
        MealType.entries.chunked(MEAL_TYPE_COLUMNS).forEach { types ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
            ) {
                types.forEach { type ->
                    MealTypeCard(
                        type = type,
                        label = mealTypeLabel(type),
                        selected = selected == type,
                        modifier = Modifier.weight(1f)
                    ) { onAction(AddMealAction.SelectMealType(type)) }
                }
            }
        }
    }
}

@Composable
private fun MealTypeCard(
    type: MealType,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = mealTypeColorPalette(type)
    HealthOutlinedCard(
        modifier = modifier
            .height(Dimens.buttonHeightMedium)
            .clickable(onClick = onClick),
        shape = Shape.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = colors.accent
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
                color = colors.accent,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
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

private const val MEAL_TYPE_COLUMNS = 2
private const val EMPTY_RESULT_KEY = "empty-result"
private const val OTHER_FOOD_KEY = "other-food"
private const val BOTTOM_SPACER_KEY = "bottom-spacer"

@Preview
@Composable
private fun PreviewAddMealPicker() {
    HealthTrackerTheme {
        AddMealContent(
            state = AddMealUiState(
                foods = listOf(Food(1L, "Cơm trắng", 130.0, 150.0, 1)),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun PreviewAddMealDetails() {
    HealthTrackerTheme {
        AddMealContent(
            state = AddMealUiState(
                selectedFood = Food(1L, "Cơm trắng", 130.0, 150.0, 1),
                consumedGrams = "150",
                caloriesPer100Grams = "130",
                estimatedCalories = 195,
                isFoodPickerExpanded = false,
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
