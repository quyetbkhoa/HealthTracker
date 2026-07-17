package com.quyetbkhoa.healthtracker.domain.model

data class ReminderTime(
    val hour: Int,
    val minute: Int
) {
    init {
        require(hour in 0..23)
        require(minute in 0..59)
    }
}

data class ReminderSettings(
    val isEnabled: Boolean = true,
    val breakfast: ReminderTime = ReminderTime(hour = 9, minute = 0),
    val lunch: ReminderTime = ReminderTime(hour = 13, minute = 0),
    val dinner: ReminderTime = ReminderTime(hour = 19, minute = 0),
    val activity: ReminderTime = ReminderTime(hour = 21, minute = 0)
) {
    fun timeFor(type: ReminderType): ReminderTime = when (type) {
        ReminderType.BREAKFAST -> breakfast
        ReminderType.LUNCH -> lunch
        ReminderType.DINNER -> dinner
        ReminderType.ACTIVITY -> activity
    }

    fun withTime(type: ReminderType, time: ReminderTime): ReminderSettings = when (type) {
        ReminderType.BREAKFAST -> copy(breakfast = time)
        ReminderType.LUNCH -> copy(lunch = time)
        ReminderType.DINNER -> copy(dinner = time)
        ReminderType.ACTIVITY -> copy(activity = time)
    }
}
