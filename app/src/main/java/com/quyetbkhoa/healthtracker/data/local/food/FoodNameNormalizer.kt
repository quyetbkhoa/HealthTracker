package com.quyetbkhoa.healthtracker.data.local.food

import java.text.Normalizer
import java.util.Locale

object FoodNameNormalizer {

    private val combiningMarksRegex = Regex("\\p{Mn}+")
    private val whitespaceRegex = Regex("\\s+")

    fun normalize(value: String): String {
        val lowercase = value
            .trim()
            .lowercase(Locale.ROOT)
            .replace('đ', 'd')

        return Normalizer.normalize(lowercase, Normalizer.Form.NFD)
            .replace(combiningMarksRegex, "")
            .replace(whitespaceRegex, " ")
            .trim()
    }
}