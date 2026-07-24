package com.quyetbkhoa.healthtracker.navigation

import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable
    data object Welcome : AppRoute

    @Serializable
    data object OnboardingGraph : AppRoute

    @Serializable
    data object ProfileStep1 : AppRoute

    @Serializable
    data object ProfileStep2 : AppRoute

    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Statistics : AppRoute

    @Serializable
    data class StatisticsCharts(
        val range: StatisticsRange = StatisticsRange.LAST_7_DAYS
    ) : AppRoute

    @Serializable
    data class AddMeal(
        val epochDay: Long,
        val mealType: MealType = MealType.BREAKFAST
    ) : AppRoute

    @Serializable
    data object MealJournal : AppRoute

    @Serializable
    data class AddActivity(
        val epochDay: Long
    ) : AppRoute

    @Serializable
    data object ActivityHistory : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object ProfileSettings : AppRoute
}
