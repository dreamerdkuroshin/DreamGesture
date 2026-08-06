package com.gestureshare.core.domain.usecase

import com.gestureshare.core.domain.model.Screenshot
import com.gestureshare.core.domain.repository.ScreenshotRepository
import javax.inject.Inject

class ProcessScreenshotUseCase @Inject constructor(
    private val screenshotRepository: ScreenshotRepository
) {
    suspend operator fun invoke(): Screenshot? {
        return screenshotRepository.getLatestScreenshot()
    }

    suspend fun delete(screenshot: Screenshot) {
        screenshotRepository.deleteScreenshot(screenshot)
    }
}
