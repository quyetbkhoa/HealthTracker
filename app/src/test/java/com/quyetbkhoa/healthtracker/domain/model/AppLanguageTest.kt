package com.quyetbkhoa.healthtracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun `returns Vietnamese for Vietnamese tag`() {
        assertEquals(AppLanguage.VIETNAMESE, AppLanguage.fromLanguageTag("vi"))
    }

    @Test
    fun `returns English for English tag`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLanguageTag("en"))
    }

    @Test
    fun `returns Vietnamese for missing or unsupported tag`() {
        assertEquals(AppLanguage.VIETNAMESE, AppLanguage.fromLanguageTag(null))
        assertEquals(AppLanguage.VIETNAMESE, AppLanguage.fromLanguageTag("fr"))
    }
}
