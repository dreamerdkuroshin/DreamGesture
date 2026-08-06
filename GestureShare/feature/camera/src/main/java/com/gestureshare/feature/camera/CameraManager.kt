package com.gestureshare.feature.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CameraManager"
        private const val TARGET_FPS_IDLE = 5
        private const val TARGET_FPS_ACTIVE = 15
        private const val TARGET_FPS_MAX = 30
    }

    private val _frameFlow = MutableSharedFlow<CameraFrame>(
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val frameFlow: SharedFlow<CameraFrame> = _frameFlow.asSharedFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var isRunning = AtomicBoolean(false)
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var currentFps = TARGET_FPS_ACTIVE
    private var handDetectedFrames = 0
    private var noHandFrames = 0

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView? = null
    ) {
        if (isRunning.get()) return

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindUseCases(lifecycleOwner, previewView)
                isRunning.set(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start camera", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        isRunning.set(false)
        cameraProvider?.unbindAll()
        imageAnalysis?.clearAnalyzer()
    }

    fun setActiveMode(active: Boolean) {
        val newFps = if (active) TARGET_FPS_ACTIVE else TARGET_FPS_IDLE
        if (newFps != currentFps) {
            currentFps = newFps
            imageAnalysis?.clearAnalyzer()
            setupImageAnalysis()
        }
    }

    private fun bindUseCases(lifecycleOwner: LifecycleOwner, previewView: PreviewView?) {
        val provider = cameraProvider ?: return

        val preview = previewView?.let {
            Preview.Builder()
                .build()
                .also { p -> p.setSurfaceProvider(it.surfaceProvider) }
        }

        setupImageAnalysis()

        try {
            provider.unbindAll()
            if (preview != null) {
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalysis
                )
            } else {
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    imageAnalysis
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    private fun setupImageAnalysis() {
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalysis?.setAnalyzer(cameraExecutor) { imageProxy ->
            if (!isRunning.get()) {
                imageProxy.close()
                return@setAnalyzer
            }

            val frame = processFrame(imageProxy)
            frame?.let { _frameFlow.tryEmit(it) }
            imageProxy.close()
        }
    }

    private fun processFrame(imageProxy: ImageProxy): CameraFrame? {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        return CameraFrame(
            data = bytes,
            width = imageProxy.width,
            height = imageProxy.height,
            timestamp = imageProxy.imageInfo.timestamp,
            rotationDegrees = imageProxy.imageInfo.rotationDegrees
        )
    }

    fun release() {
        stopCamera()
        cameraExecutor.shutdown()
    }
}

data class CameraFrame(
    val data: ByteArray,
    val width: Int,
    val height: Int,
    val timestamp: Long,
    val rotationDegrees: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CameraFrame) return false
        return width == other.width && height == other.height && timestamp == other.timestamp
    }

    override fun hashCode(): Int = timestamp.hashCode()
}
