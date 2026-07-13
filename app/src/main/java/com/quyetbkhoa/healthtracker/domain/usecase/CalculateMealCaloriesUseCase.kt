package com.quyetbkhoa.healthtracker.domain.usecase

import kotlin.math.roundToInt
import javax.inject.Inject

class CalculateMealCaloriesUseCase @Inject constructor() {
    operator fun invoke(
        caloriesPer100Grams: Double,
        consumedGrams: Double
    ): Int? {
        if (!caloriesPer100Grams.isFinite() || caloriesPer100Grams <= 0.0) return null
        if (!consumedGrams.isFinite() || consumedGrams <= 0.0) return null
        val result = caloriesPer100Grams * consumedGrams / 100.0
        if (!result.isFinite() || result <= 0.0) return null
        return result.roundToInt().coerceAtLeast(1)
    }
}
