package com.quyetbkhoa.healthtracker.presentation.activityhistory

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import com.quyetbkhoa.healthtracker.domain.usecase.DeleteActivityRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ActivityHistoryItem(
    val id: Long,
    val name: String,
    val icon: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val performedAt: Long
)

@Immutable
data class ActivityHistoryUiState(
    val isLoading: Boolean = true,
    val selectedEpochDay: Long = LocalDate.now().toEpochDay(),
    val activities: List<ActivityHistoryItem> = emptyList(),
    val pendingDeleteId: Long? = null
) {
    val totalCalories: Int get() = activities.sumOf(ActivityHistoryItem::caloriesBurned)
    val totalDurationMinutes: Int get() = activities.sumOf(ActivityHistoryItem::durationMinutes)
}

sealed interface ActivityHistoryAction {
    data object PreviousDay : ActivityHistoryAction
    data object NextDay : ActivityHistoryAction
    data class SelectDay(val epochDay: Long) : ActivityHistoryAction
    data class RequestDelete(val id: Long) : ActivityHistoryAction
    data object DismissDelete : ActivityHistoryAction
    data object ConfirmDelete : ActivityHistoryAction
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ActivityHistoryViewModel @Inject constructor(
    activityRepository: ActivityRepository,
    private val deleteActivityRecord: DeleteActivityRecordUseCase,
    clock: Clock
) : ViewModel() {
    private val todayEpochDay = LocalDate.now(clock).toEpochDay()
    private val selectedDay = MutableStateFlow(todayEpochDay)
    private val pendingDeleteId = MutableStateFlow<Long?>(null)
    private val records = selectedDay.flatMapLatest(activityRepository::observeRecordsByDay)

    val uiState: StateFlow<ActivityHistoryUiState> = combine(
        selectedDay,
        records,
        activityRepository.observeActivityTypes(),
        pendingDeleteId
    ) { day, activityRecords, activityTypes, deleteId ->
        val typesById = activityTypes.associateBy { it.id }
        ActivityHistoryUiState(
            isLoading = false,
            selectedEpochDay = day,
            activities = activityRecords.map { record ->
                val type = typesById[record.activityTypeId]
                ActivityHistoryItem(
                    id = record.id,
                    name = type?.name.orEmpty(),
                    icon = type?.iconName ?: DEFAULT_ACTIVITY_ICON,
                    durationMinutes = record.durationMinutes,
                    caloriesBurned = record.caloriesBurned.roundToInt(),
                    performedAt = record.performedAt
                )
            },
            pendingDeleteId = deleteId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivityHistoryUiState(selectedEpochDay = todayEpochDay)
    )

    fun onAction(action: ActivityHistoryAction) {
        when (action) {
            ActivityHistoryAction.PreviousDay -> selectedDay.update { it - 1L }
            ActivityHistoryAction.NextDay -> selectedDay.update { it + 1L }
            is ActivityHistoryAction.SelectDay -> selectedDay.value = action.epochDay
            is ActivityHistoryAction.RequestDelete -> pendingDeleteId.value = action.id
            ActivityHistoryAction.DismissDelete -> pendingDeleteId.value = null
            ActivityHistoryAction.ConfirmDelete -> {
                val recordId = pendingDeleteId.value ?: return
                pendingDeleteId.value = null
                viewModelScope.launch { deleteActivityRecord(recordId) }
            }
        }
    }

    private companion object {
        const val DEFAULT_ACTIVITY_ICON = "✨"
    }
}
