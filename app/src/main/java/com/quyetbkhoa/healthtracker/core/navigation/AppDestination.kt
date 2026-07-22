package com.quyetbkhoa.healthtracker.core.navigation

enum class AppDestination {
    DASHBOARD,
    ADD_MEAL,
    ADD_LUNCH,
    ADD_DINNER,
    ADD_ACTIVITY;

    companion object {
        const val EXTRA_APP_DESTINATION = "app_destination"

        fun fromName(name: String?): AppDestination? =
            entries.firstOrNull { it.name == name }
    }
}
