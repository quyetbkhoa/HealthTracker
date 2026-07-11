package com.quyetbkhoa.healthtracker.presentation.meal

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.usecase.AddMealResult
import com.quyetbkhoa.healthtracker.domain.usecase.AddMealUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.AddMealValidationError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@Immutable
data class AddMealUiState(
    val name: String = "",
    val calories: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val nameError: AddMealValidationError? = null,
    val caloriesError: AddMealValidationError? = null,
    val isSaving: Boolean = false
)

sealed interface AddMealAction {
    data class UpdateName(val value: String) : AddMealAction
    data class UpdateCalories(val value: String) : AddMealAction
    data class SelectMealType(val value: MealType) : AddMealAction
    data object Save : AddMealAction
}

sealed interface AddMealUiEvent {
    data object Saved : AddMealUiEvent
    data object SaveFailed : AddMealUiEvent
}

@HiltViewModel
class AddMealViewModel @Inject constructor(
    private val addMealUseCase: AddMealUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddMealUiState())
    val uiState: StateFlow<AddMealUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<AddMealUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onAction(action: AddMealAction) {
        when (action) {
            is AddMealAction.UpdateName -> _uiState.update {
                it.copy(name = action.value, nameError = null)
            }
            is AddMealAction.UpdateCalories -> {
                val digitsOnly = action.value.filter(Char::isDigit)
                _uiState.update { it.copy(calories = digitsOnly, caloriesError = null) }
            }
            is AddMealAction.SelectMealType -> _uiState.update {
                it.copy(mealType = action.value)
            }
            AddMealAction.Save -> saveMeal()
        }
    }

    private fun saveMeal() {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isSaving = true) }
            try {
                when (val result = addMealUseCase(
                    MealEntry(
                        name = state.name,
                        calories = state.calories.toIntOrNull() ?: 0,
                        mealType = state.mealType,
                        eatenAt = Instant.now().toEpochMilli()
                    )
                )) {
                    AddMealResult.Success -> _uiEvent.send(AddMealUiEvent.Saved)
                    is AddMealResult.Invalid -> showValidationError(result.error)
                }
            } catch (_: Exception) {
                _uiEvent.send(AddMealUiEvent.SaveFailed)
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun showValidationError(error: AddMealValidationError) {
        _uiState.update { state ->
            when (error) {
                AddMealValidationError.EMPTY_NAME -> state.copy(nameError = error)
                AddMealValidationError.INVALID_CALORIES,
                AddMealValidationError.CALORIES_TOO_HIGH -> state.copy(caloriesError = error)
            }
        }
    }
}
