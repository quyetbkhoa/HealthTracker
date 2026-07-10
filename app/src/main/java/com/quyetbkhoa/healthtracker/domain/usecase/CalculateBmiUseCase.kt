package com.quyetbkhoa.healthtracker.domain.usecase

import javax.inject.Inject

enum class BmiCategory {
    UNDERWEIGHT,
    NORMAL,
    OVERWEIGHT,
    OBESE
}

data class BmiResult(val value: Float, val category: BmiCategory)

class CalculateBmiUseCase @Inject constructor() {
    operator fun invoke(weightKg: Float, heightCm: Float): BmiResult? {
        if (weightKg <= 0f || heightCm <= 0f) return null
        val heightMeters = heightCm / 100f
        val bmi = weightKg / (heightMeters * heightMeters)
        val category = when {
            bmi < 18.5f -> BmiCategory.UNDERWEIGHT
            bmi < 25f -> BmiCategory.NORMAL
            bmi < 30f -> BmiCategory.OVERWEIGHT
            else -> BmiCategory.OBESE
        }
        return BmiResult(bmi, category)
    }
}
