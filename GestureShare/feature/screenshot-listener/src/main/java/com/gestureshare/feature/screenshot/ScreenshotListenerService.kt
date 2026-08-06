package com.gestureshare.feature.screenshot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScreenshotListenerService : Service() {

    companion object {
        private const val CHANNEL_ID = "screenshot_listener_channel"
        private const val NOTIFICATION_ID = 1001
        private const val SERVICE_NAME = "GestureShare Screenshot Listener"
    }

    @Inject
    lateinit var screenshotDetector: ScreenshotDetector

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var contentObserver: ContentObserver? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        registerContentObserver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterContentObserver()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerContentObserver() {
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                if (uri != null && !selfChange) {
                    serviceScope.launch {
                        screenshotDetector.onMediaChange(uri)
                    }
                }
            }
        }

        contentObserver?.let {
            contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                it
            )
        }
    }

    private fun unregisterContentObserver() {
        contentObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            SERVICE_NAME,
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Monitors for screenshots to enable gesture sharing"
            setShowBadge(false)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GestureShare")
            .setContentText("Listening for screenshots...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
