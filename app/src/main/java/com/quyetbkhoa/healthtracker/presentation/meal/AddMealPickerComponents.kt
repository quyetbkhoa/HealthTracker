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
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.presentation.designsystem.Dimens
import com.quyetbkhoa.healthtracker.presentation.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.presentation.designsystem.Shape
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthNumericSlider
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthOutlinedCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.healthColors
import com.quyetbkhoa.healthtracker.presentation.designsystem.mealTypeColorPalette
import com.quyetbkhoa.healthtracker.domain.model.Food
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.usecase.AddMealValidationError
import java.text.NumberFormat

@Composable
internal fun AddMealHeader(onNavigateBack: () -> Unit) {
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
internal fun FoodPickerHeader() {
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
internal fun FoodSearchField(query: String, onAction: (AddMealAction) -> Unit) {
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
internal fun FoodGrid(foods: List<Food>, onAction: (AddMealAction) -> Unit) {
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
internal fun FoodChoiceCard(
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
internal fun OtherFoodCard(onClick: () -> Unit) {
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
internal fun EmptySearchCard() {
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
