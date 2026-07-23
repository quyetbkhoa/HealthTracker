package com.quyetbkhoa.healthtracker.presentation.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.quyetbkhoa.healthtracker.core.navigation.AppRoute
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange
import com.quyetbkhoa.healthtracker.domain.statistics.CalculateStatisticsUseCase
import com.quyetbkhoa.healthtracker.domain.statistics.ResolveStatisticsPeriodUseCase
import com.quyetbkhoa.healthtracker.domain.statistics.StatisticsCalculationInput
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
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeStatisticsData: ObserveStatisticsDataUseCase,
    private val calculateStatistics: CalculateStatisticsUseCase,
    private val resolveStatisticsPeriod: ResolveStatisticsPeriodUseCase
) : ViewModel() {
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
        val period = resolveStatisticsPeriod(range)
        return observeStatisticsData(
            period.startMillis,
            period.endMillis,
            languageTag
        ).map { data ->
            calculateStatistics(
                StatisticsCalculationInput(
                    range = range,
                    requestedStartDate = period.startDate,
                    dailyTarget = data.profile?.dailyCalorieTarget ?: 0,
                    dailyBasalCalories = data.profile?.bmrCalories ?: 0,
                    goal = data.profile?.goal ?: Goal.MAINTAIN,
                    meals = data.meals,
                    activities = data.activities
                )
            ).toUiState()
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val ENGLISH = "en"
        const val VIETNAMESE = "vi"
    }
}
