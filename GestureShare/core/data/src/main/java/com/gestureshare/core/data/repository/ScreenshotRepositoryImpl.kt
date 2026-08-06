package com.gestureshare.core.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.gestureshare.core.domain.model.Screenshot
import com.gestureshare.core.domain.model.ScreenshotEvent
import com.gestureshare.core.domain.repository.ScreenshotRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ScreenshotRepository {

    private val contentResolver: ContentResolver = context.contentResolver
    private val _latestScreenshot = MutableStateFlow<Screenshot?>(null)
    private val latestScreenshot: StateFlow<Screenshot?> = _latestScreenshot.asStateFlow()
    private var lastProcessedId: Long = 0L

    override fun observeScreenshots(): Flow<ScreenshotEvent> = callbackFlow {
        val handler = Handler(Looper.getMainLooper())
        val observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                if (uri != null) {
                    val screenshot = extractScreenshotFromUri(uri)
                    if (screenshot != null) {
                        _latestScreenshot.value = screenshot
                        trySend(ScreenshotEvent.Captured(screenshot))
                    }
                }
            }
        }

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )

        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }

    override suspend fun getLatestScreenshot(): Screenshot? {
        return latestScreenshot.value ?: queryLatestScreenshot()
    }

    override suspend fun deleteScreenshot(screenshot: Screenshot) {
        try {
            val file = File(screenshot.uri.removePrefix("file://"))
            if (file.exists()) {
                file.delete()
            }
            _latestScreenshot.value = null
        } catch (e: Exception) {
            // Silent - security requirement: no persistent state
        }
    }

    override suspend fun markProcessed(screenshot: Screenshot) {
        _latestScreenshot.value = screenshot.copy(isProcessed = true)
    }

    private fun extractScreenshotFromUri(uri: Uri): Screenshot? {
        return try {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED
            )

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val widthIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                    val heightIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                    val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

                    val id = cursor.getLong(idIndex)
                    if (id == lastProcessedId) return null
                    lastProcessedId = id

                    val name = cursor.getString(nameIndex) ?: return null
                    if (!isScreenshot(name)) return null

                    Screenshot(
                        id = id.toString(),
                        uri = uri.toString(),
                        timestamp = System.currentTimeMillis(),
                        width = cursor.getInt(widthIndex),
                        height = cursor.getInt(heightIndex),
                        fileSizeBytes = cursor.getLong(sizeIndex)
                    )
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun queryLatestScreenshot(): Screenshot? {
        return try {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT 1"

            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    )
                    if (isScreenshot(name)) {
                        val id = cursor.getLong(
                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                        )
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        Screenshot(
                            id = id.toString(),
                            uri = contentUri.toString(),
                            timestamp = System.currentTimeMillis(),
                            width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)),
                            height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)),
                            fileSizeBytes = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                        )
                    } else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isScreenshot(fileName: String): Boolean {
        val lower = fileName.lowercase()
        return (lower.contains("screenshot") || lower.contains("screen_shot") ||
                lower.contains("screen-shot") || lower.contains("screencap")) &&
                (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".webp")) &&
                !lower.contains("record") && !lower.contains("video")
    }
}
