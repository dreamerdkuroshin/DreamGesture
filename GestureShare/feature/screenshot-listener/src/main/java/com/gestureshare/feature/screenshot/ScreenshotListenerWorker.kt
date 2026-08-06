package com.gestureshare.feature.screenshot

import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ScreenshotListenerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val intent = Intent(applicationContext, ScreenshotListenerService::class.java)
        applicationContext.startForegroundService(intent)
        return Result.success()
    }
}
