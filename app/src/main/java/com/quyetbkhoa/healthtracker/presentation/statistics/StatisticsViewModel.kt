package com.quyetbkhoa.healthtracker.presentation.statistics

import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.quyetbkhoa.healthtracker.core.navigation.AppRoute
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.usecase.ObserveStatisticsDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
    private val observeStatisticsData: ObserveStatisticsDataUseCase,
    private val calculator: StatisticsCalculator,
    clock: Clock
) : ViewModel() {
    private val zone: ZoneId = clock.zone
    private val today: LocalDate = LocalDate.now(clock)
    private val languageTag = Locale.getDefault().language.takeIf { it == ENGLISH } ?: VIETNAMESE
    private val initialRange = savedStateHandle.toRoute<AppRoute.StatisticsCharts>().range
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
        return observeStatisticsData(
            period.startMillis,
            period.endMillis,
            languageTag
        ).map { data ->
            calculator.calculate(
                StatisticsInput(
                    range = range,
                    requestedStartDate = period.startDate,
                    dailyTarget = data.profile?.dailyCalorieTarget ?: 0,
                    goal = data.profile?.goal ?: Goal.MAINTAIN,
                    meals = data.meals,
                    activities = data.activities
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
