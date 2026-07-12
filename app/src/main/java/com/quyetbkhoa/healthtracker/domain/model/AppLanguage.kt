package com.quyetbkhoa.healthtracker.domain.model

enum class AppLanguage(val languageTag: String) {
    VIETNAMESE("vi"),
    ENGLISH("en");

    companion object {
        fun fromLanguageTag(languageTag: String?): AppLanguage =
            entries.firstOrNull { it.languageTag == languageTag.orEmpty() }
                ?: VIETNAMESE
    }
}
