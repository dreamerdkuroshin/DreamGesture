package com.gestureshare.core.domain.usecase

import com.gestureshare.core.domain.model.ScreenshotEvent
import com.gestureshare.core.domain.repository.ScreenshotRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveScreenshotsUseCase @Inject constructor(
    private val screenshotRepository: ScreenshotRepository
) {
    operator fun invoke(): Flow<ScreenshotEvent> {
        return screenshotRepository.observeScreenshots()
    }
}
