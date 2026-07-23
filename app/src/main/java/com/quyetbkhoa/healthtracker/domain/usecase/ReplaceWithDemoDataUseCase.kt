package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.repository.DemoDataRepository
import javax.inject.Inject

class ReplaceWithDemoDataUseCase @Inject constructor(
    private val demoDataRepository: DemoDataRepository
) {
    suspend operator fun invoke() {
        demoDataRepository.replaceWithDemoData()
    }
}
