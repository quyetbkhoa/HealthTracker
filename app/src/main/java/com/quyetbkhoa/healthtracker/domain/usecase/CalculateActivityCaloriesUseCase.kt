package com.quyetbkhoa.healthtracker.domain.usecase

import javax.inject.Inject

class CalculateActivityCaloriesUseCase @Inject constructor() {
    operator fun invoke(met: Double, weightKg: Double, durationMinutes: Int): Double {
        if (!met.isFinite() || !weightKg.isFinite()) return 0.0
        if (met <= 0.0 || weightKg <= 0.0 || durationMinutes !in 1..600) return 0.0
        return (met * weightKg * durationMinutes / 60.0).takeIf(Double::isFinite) ?: 0.0
    }
}
