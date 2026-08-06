package com.gestureshare.feature.screenshot

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.gestureshare.core.domain.model.Screenshot
import com.gestureshare.core.domain.model.ScreenshotEvent
import com.gestureshare.core.domain.repository.ScreenshotRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenshotRepository: ScreenshotRepository
) {
    private val contentResolver: ContentResolver = context.contentResolver

    private val _events = MutableSharedFlow<ScreenshotEvent>(
        replay = 1,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ScreenshotEvent> = _events.asSharedFlow()

    private val processedIds = ConcurrentHashMap.newKeySet<Long>()
    private var lastCheckTimestamp: Long = 0

    suspend fun onMediaChange(uri: Uri) {
        val screenshot = resolveScreenshot(uri) ?: return
        if (processedIds.contains(screenshot.id.toLong())) return

        processedIds.add(screenshot.id.toLong())
        if (processedIds.size > 100) {
            processedIds.clear()
        }

        _events.emit(ScreenshotEvent.Captured(screenshot))
        _events.emit(ScreenshotEvent.Ready(screenshot))
    }

    private fun resolveScreenshot(uri: Uri): Screenshot? {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED
        )

        return try {
            contentResolver.query(uri, projection, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    ) ?: return null

                    if (!isScreenshotFile(name)) return null

                    val id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    )

                    Screenshot(
                        id = id.toString(),
                        uri = uri.toString(),
                        timestamp = System.currentTimeMillis(),
                        width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)),
                        height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)),
                        fileSizeBytes = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                    )
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isScreenshotFile(fileName: String): Boolean {
        val lower = fileName.lowercase()
        val screenshotKeywords = listOf("screenshot", "screen_shot", "screen-shot", "screencap")
        val imageExtensions = listOf(".png", ".jpg", ".jpeg", ".webp")
        val excludeKeywords = listOf("record", "video", "screenrecord")

        return screenshotKeywords.any { lower.contains(it) } &&
                imageExtensions.any { lower.endsWith(it) } &&
                excludeKeywords.none { lower.contains(it) }
    }
}
