package com.quyetbkhoa.healthtracker.presentation.meal

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.quyetbkhoa.healthtracker.navigation.AppRoute
import com.quyetbkhoa.healthtracker.domain.model.Food
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.repository.FoodRepository
import com.quyetbkhoa.healthtracker.domain.usecase.AddMealResult
import com.quyetbkhoa.healthtracker.domain.usecase.AddMealUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.AddMealValidationError
import com.quyetbkhoa.healthtracker.domain.usecase.CalculateMealCaloriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.Clock
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class AddMealUiState(
    val epochDay: Long = LocalDate.now().toEpochDay(),
    val mealType: MealType = MealType.BREAKFAST,
    val query: String = "",
    val foods: List<Food> = emptyList(),
    val selectedFood: Food? = null,
    val isCustom: Boolean = false,
    val customName: String = "",
    val consumedGrams: String = "",
    val caloriesPer100Grams: String = "",
    val estimatedCalories: Int? = null,
    val isFoodPickerExpanded: Boolean = true,
    val isLoading: Boolean = true,
    val validationError: AddMealValidationError? = null,
    val isSaving: Boolean = false
) {
    val canSave: Boolean
        get() = !isFoodPickerExpanded &&
            estimatedCalories != null &&
            (selectedFood != null || (isCustom && customName.isNotBlank())) &&
            !isSaving
}

sealed interface AddMealAction {
    data class UpdateQuery(val value: String) : AddMealAction
    data class SelectFood(val food: Food) : AddMealAction
    data class ToggleFavorite(val foodId: Long) : AddMealAction
    data object SelectOther : AddMealAction
    data object ReselectFood : AddMealAction
    data class UpdateCustomName(val value: String) : AddMealAction
    data class UpdateConsumedGrams(val value: String) : AddMealAction
    data class UpdateCaloriesPer100Grams(val value: String) : AddMealAction
    data class SelectMealType(val value: MealType) : AddMealAction
    data object Save : AddMealAction
}

sealed interface AddMealUiEvent {
    data object Saved : AddMealUiEvent
    data object SaveFailed : AddMealUiEvent
    data object FavoriteFailed : AddMealUiEvent
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AddMealViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val foodRepository: FoodRepository,
    private val addMealUseCase: AddMealUseCase,
    private val calculateMealCalories: CalculateMealCaloriesUseCase,
    private val clock: Clock
) : ViewModel() {
    private val languageTag = Locale.getDefault().language
        .takeIf { it == "en" }
        ?: "vi"
    private val query = MutableStateFlow("")
    private val initialRoute = savedStateHandle.toRoute<AppRoute.AddMeal>()
    private val initialEpochDay = initialRoute.epochDay
    private val initialMealType = initialRoute.mealType

    private val _uiState = MutableStateFlow(
        AddMealUiState(epochDay = initialEpochDay, mealType = initialMealType)
    )
    val uiState: StateFlow<AddMealUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<AddMealUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()
    private var isSaveInProgress = false
    private var hasSaved = false

    init {
        viewModelScope.launch {
            query.flatMapLatest { foodRepository.observeFoods(it, languageTag) }
                .collect { foods ->
                    _uiState.update { it.copy(foods = foods, isLoading = false) }
                }
        }
    }

    fun onAction(action: AddMealAction) {
        when (action) {
            is AddMealAction.UpdateQuery -> {
                query.value = action.value
                _uiState.update { it.copy(query = action.value) }
            }
            is AddMealAction.SelectFood -> selectFood(action.food)
            is AddMealAction.ToggleFavorite -> toggleFavorite(action.foodId)
            AddMealAction.SelectOther -> _uiState.update {
                it.copy(
                    isCustom = true,
                    selectedFood = null,
                    customName = "",
                    consumedGrams = DEFAULT_CUSTOM_GRAMS,
                    caloriesPer100Grams = DEFAULT_CUSTOM_CALORIES,
                    estimatedCalories = calculateMealCalories(
                        DEFAULT_CUSTOM_CALORIES.toDouble(),
                        DEFAULT_CUSTOM_GRAMS.toDouble()
                    ),
                    isFoodPickerExpanded = false,
                    validationError = null
                )
            }
            AddMealAction.ReselectFood -> _uiState.update {
                it.copy(
                    selectedFood = null,
                    isCustom = false,
                    customName = "",
                    consumedGrams = "",
                    caloriesPer100Grams = "",
                    estimatedCalories = null,
                    isFoodPickerExpanded = true,
                    validationError = null
                )
            }
            is AddMealAction.UpdateCustomName -> _uiState.update {
                it.copy(customName = action.value, validationError = null)
            }
            is AddMealAction.UpdateConsumedGrams -> updateNutrition(consumedGrams = action.value)
            is AddMealAction.UpdateCaloriesPer100Grams ->
                updateNutrition(caloriesPer100Grams = action.value)
            is AddMealAction.SelectMealType -> _uiState.update { it.copy(mealType = action.value) }
            AddMealAction.Save -> saveMeal()
        }
    }

    private fun selectFood(food: Food) {
        _uiState.update {
            it.copy(
                selectedFood = food,
                isCustom = false,
                consumedGrams = food.defaultServingGrams.toInputText(),
                caloriesPer100Grams = food.caloriesPer100Grams.toInputText(),
                estimatedCalories = calculateMealCalories(
                    food.caloriesPer100Grams,
                    food.defaultServingGrams
                ),
                isFoodPickerExpanded = false,
                validationError = null
            )
        }
    }

    private fun toggleFavorite(foodId: Long) {
        val food = _uiState.value.foods.firstOrNull { it.id == foodId } ?: return
        viewModelScope.launch {
            runCatching { foodRepository.setFavorite(foodId, !food.isFavorite) }
                .onFailure { _uiEvent.send(AddMealUiEvent.FavoriteFailed) }
        }
    }

    private fun updateNutrition(
        consumedGrams: String? = null,
        caloriesPer100Grams: String? = null
    ) {
        _uiState.update { state ->
            val gramsText = consumedGrams?.decimalInput() ?: state.consumedGrams
            val caloriesText = caloriesPer100Grams?.decimalInput() ?: state.caloriesPer100Grams
            state.copy(
                consumedGrams = gramsText,
                caloriesPer100Grams = caloriesText,
                estimatedCalories = calculateMealCalories(
                    caloriesText.toDoubleOrNull() ?: Double.NaN,
                    gramsText.toDoubleOrNull() ?: Double.NaN
                ),
                validationError = null
            )
        }
    }

    private fun saveMeal() {
        val state = _uiState.value
        if (isSaveInProgress || hasSaved) return
        val food = state.selectedFood
        val name = food?.name ?: state.customName
        val grams = state.consumedGrams.toDoubleOrNull() ?: Double.NaN
        val per100Grams = food?.caloriesPer100Grams
            ?: state.caloriesPer100Grams.toDoubleOrNull()
            ?: Double.NaN
        val eatenAt = LocalDate.ofEpochDay(state.epochDay)
            .atTime(LocalTime.now(clock))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        isSaveInProgress = true
        _uiState.update { it.copy(isSaving = true, validationError = null) }
        viewModelScope.launch {
            try {
                val result = addMealUseCase(
                    MealEntry(
                        foodId = food?.id,
                        name = name,
                        nameSnapshot = name,
                        calories = state.estimatedCalories ?: 0,
                        mealType = state.mealType,
                        consumedGrams = grams,
                        caloriesPer100GramsSnapshot = per100Grams,
                        eatenAt = eatenAt
                    )
                )
                when (result) {
                    AddMealResult.Success -> {
                        hasSaved = true
                        _uiEvent.send(AddMealUiEvent.Saved)
                    }
                    is AddMealResult.Invalid -> _uiState.update {
                        it.copy(validationError = result.error)
                    }
                }
            } catch (_: Exception) {
                _uiEvent.send(AddMealUiEvent.SaveFailed)
            } finally {
                isSaveInProgress = false
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private companion object {
        const val DEFAULT_CUSTOM_GRAMS = "100"
        const val DEFAULT_CUSTOM_CALORIES = "100"
    }
}

private fun String.decimalInput(): String {
    val normalized = replace(',', '.')
    var hasDecimal = false
    return normalized.filter { char ->
        when {
            char.isDigit() -> true
            char == '.' && !hasDecimal -> {
                hasDecimal = true
                true
            }
            else -> false
        }
    }
}

private fun Double.toInputText(): String =
    if (this % 1.0 == 0.0) toInt().toString() else toString()
