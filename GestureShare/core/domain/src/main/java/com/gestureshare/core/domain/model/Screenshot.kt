package com.gestureshare.core.domain.model

data class Screenshot(
    val id: String,
    val uri: String,
    val timestamp: Long,
    val width: Int,
    val height: Int,
    val fileSizeBytes: Long,
    val isProcessed: Boolean = false
)

sealed class ScreenshotEvent {
    data class Captured(val screenshot: Screenshot) : ScreenshotEvent()
    object Processing : ScreenshotEvent()
    data class Ready(val screenshot: Screenshot) : ScreenshotEvent()
    data class Error(val message: String) : ScreenshotEvent()
}
