package com.quyetbkhoa.healthtracker.presentation.mealjournal

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.usecase.DeleteMealUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.ObserveMealJournalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.Clock
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class MealJournalUiState(
    val isLoading: Boolean = true,
    val selectedEpochDay: Long = LocalDate.now().toEpochDay(),
    val targetCalories: Int = 0,
    val meals: List<MealEntry> = emptyList(),
    val pendingDeleteId: Long? = null
) {
    val totalCalories: Int get() = meals.sumOf(MealEntry::calories)
    fun mealsFor(type: MealType): List<MealEntry> = meals.filter { it.mealType == type }
    fun caloriesFor(type: MealType): Int = mealsFor(type).sumOf(MealEntry::calories)
}

sealed interface MealJournalAction {
    data object PreviousDay : MealJournalAction
    data object NextDay : MealJournalAction
    data class SelectDay(val epochDay: Long) : MealJournalAction
    data class RequestDelete(val id: Long) : MealJournalAction
    data object DismissDelete : MealJournalAction
    data object ConfirmDelete : MealJournalAction
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MealJournalViewModel @Inject constructor(
    observeMealJournal: ObserveMealJournalUseCase,
    private val deleteMeal: DeleteMealUseCase,
    clock: Clock
) : ViewModel() {
    private val selectedDay = MutableStateFlow(LocalDate.now(clock).toEpochDay())
    private val pendingDeleteId = MutableStateFlow<Long?>(null)
    private val languageTag = Locale.getDefault().language.takeIf { it == "en" } ?: "vi"

    private val journalData = selectedDay.flatMapLatest { day ->
        observeMealJournal(day, languageTag)
    }

    val uiState: StateFlow<MealJournalUiState> = combine(
        selectedDay,
        journalData,
        pendingDeleteId
    ) { day, data, deleteId ->
        MealJournalUiState(
            isLoading = false,
            selectedEpochDay = day,
            targetCalories = data.targetCalories,
            meals = data.meals,
            pendingDeleteId = deleteId
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        MealJournalUiState()
    )

    fun onAction(action: MealJournalAction) {
        when (action) {
            MealJournalAction.PreviousDay -> selectedDay.update { it - 1L }
            MealJournalAction.NextDay -> selectedDay.update { it + 1L }
            is MealJournalAction.SelectDay -> selectedDay.value = action.epochDay
            is MealJournalAction.RequestDelete -> pendingDeleteId.value = action.id
            MealJournalAction.DismissDelete -> pendingDeleteId.value = null
            MealJournalAction.ConfirmDelete -> {
                val id = pendingDeleteId.value ?: return
                pendingDeleteId.value = null
                viewModelScope.launch { deleteMeal(id) }
            }
        }
    }
}
