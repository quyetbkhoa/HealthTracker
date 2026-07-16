package com.quyetbkhoa.healthtracker.presentation.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val mealRepository: MealRepository,
    private val activityRepository: ActivityRepository,
    private val calculator: StatisticsCalculator,
    clock: Clock
) : ViewModel() {
    private val zone: ZoneId = clock.zone
    private val today: LocalDate = LocalDate.now(clock)
    private val languageTag = Locale.getDefault().language.takeIf { it == ENGLISH } ?: VIETNAMESE
    private val initialRange = savedStateHandle.initialStatisticsRange()
    private val selectedRange = MutableStateFlow(initialRange)

    val uiState: StateFlow<StatisticsUiState> = selectedRange
        .flatMapLatest(::observeStatistics)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = StatisticsUiState(selectedRange = initialRange)
        )

    fun onAction(action: StatisticsAction) {
        when (action) {
            is StatisticsAction.SelectRange -> selectedRange.value = action.range
        }
    }

    private fun observeStatistics(range: StatisticsRange): Flow<StatisticsUiState> {
        val period = range.toQueryPeriod(today, zone)
        return combine(
            profileRepository.userProfile,
            mealRepository.observeMealsBetween(period.startMillis, period.endMillis, languageTag),
            activityRepository.observeRecordsBetween(period.startMillis, period.endMillis)
        ) { profile, meals, activities ->
            calculator.calculate(
                StatisticsInput(
                    range = range,
                    requestedStartDate = period.startDate,
                    dailyTarget = profile?.dailyCalorieTarget ?: 0,
                    goal = profile?.goal ?: Goal.MAINTAIN,
                    meals = meals,
                    activities = activities
                )
            )
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val ENGLISH = "en"
        const val VIETNAMESE = "vi"
    }
}

private data class StatisticsQueryPeriod(
    val startDate: LocalDate,
    val startMillis: Long,
    val endMillis: Long
)

private fun StatisticsRange.toQueryPeriod(today: LocalDate, zone: ZoneId): StatisticsQueryPeriod {
    val startDate = when (this) {
        StatisticsRange.TODAY -> today
        StatisticsRange.LAST_7_DAYS -> today.minusDays(6)
        StatisticsRange.LAST_30_DAYS -> today.minusDays(29)
        StatisticsRange.ALL -> LocalDate.ofEpochDay(0)
    }
    return StatisticsQueryPeriod(
        startDate = startDate,
        startMillis = startDate.atStartOfDay(zone).toInstant().toEpochMilli(),
        endMillis = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    )
}

private fun SavedStateHandle.initialStatisticsRange(): StatisticsRange =
    get<String>("range")
        ?.let { value -> runCatching { StatisticsRange.valueOf(value) }.getOrNull() }
        ?: StatisticsRange.LAST_7_DAYS
