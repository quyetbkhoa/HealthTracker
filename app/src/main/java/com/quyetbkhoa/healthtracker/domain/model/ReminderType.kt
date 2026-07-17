package com.quyetbkhoa.healthtracker.domain.model

enum class ReminderType(
    val requestCode: Int,
    val notificationId: Int
) {
    BREAKFAST(requestCode = 1001, notificationId = 2001),
    LUNCH(requestCode = 1002, notificationId = 2002),
    DINNER(requestCode = 1003, notificationId = 2003),
    ACTIVITY(requestCode = 1004, notificationId = 2004);

    companion object {
        fun fromName(value: String?): ReminderType? =
            entries.firstOrNull { it.name == value }
    }
}
