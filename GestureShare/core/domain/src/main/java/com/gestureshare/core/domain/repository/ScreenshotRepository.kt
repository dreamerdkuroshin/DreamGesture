package com.gestureshare.core.domain.repository

import com.gestureshare.core.domain.model.Screenshot
import com.gestureshare.core.domain.model.ScreenshotEvent
import kotlinx.coroutines.flow.Flow

interface ScreenshotRepository {
    fun observeScreenshots(): Flow<ScreenshotEvent>
    suspend fun getLatestScreenshot(): Screenshot?
    suspend fun deleteScreenshot(screenshot: Screenshot)
    suspend fun markProcessed(screenshot: Screenshot)
}
