package com.gestureshare.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestureshare.core.domain.model.Gesture
import com.gestureshare.core.domain.model.GestureEvent
import com.gestureshare.core.domain.model.NearbyDevice
import com.gestureshare.core.domain.model.Screenshot
import com.gestureshare.core.domain.model.ScreenshotEvent
import com.gestureshare.core.domain.model.TransferState
import com.gestureshare.core.domain.usecase.DetectGestureUseCase
import com.gestureshare.core.domain.usecase.ProcessScreenshotUseCase
import com.gestureshare.core.domain.usecase.TransferScreenshotUseCase
import com.gestureshare.feature.gesture.GestureEngine
import com.gestureshare.feature.nearby.DeviceDiscoveryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isServiceRunning: Boolean = false,
    val currentScreenshot: Screenshot? = null,
    val lastGesture: Gesture? = null,
    val nearbyDevices: List<NearbyDevice> = emptyList(),
    val transferState: TransferState = TransferState.Idle,
    val selectedDevice: NearbyDevice? = null,
    val cameraActive: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val processScreenshotUseCase: ProcessScreenshotUseCase,
    private val transferScreenshotUseCase: TransferScreenshotUseCase,
    private val gestureEngine: GestureEngine,
    private val deviceDiscoveryManager: DeviceDiscoveryManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeGestures()
        observeDevices()
        observeTransferState()
    }

    fun startGestureShareService() {
        _uiState.value = _uiState.value.copy(isServiceRunning = true, cameraActive = true)
    }

    fun stopGestureShareService() {
        _uiState.value = _uiState.value.copy(isServiceRunning = false, cameraActive = false)
    }

    private fun observeGestures() {
        viewModelScope.launch {
            gestureEngine.events.collect { event ->
                when (event) {
                    is GestureEvent.Detected -> {
                        _uiState.value = _uiState.value.copy(lastGesture = event.gesture)
                        handleDetectedGesture(event.gesture)
                    }
                    is GestureEvent.SequenceCompleted -> {
                        _uiState.value = _uiState.value.copy(lastGesture = event.sequence.gestures.last())
                    }
                    GestureEvent.Idle -> {}
                }
            }
        }
    }

    private fun observeDevices() {
        viewModelScope.launch {
            deviceDiscoveryManager.devices.collect { devices ->
                _uiState.value = _uiState.value.copy(nearbyDevices = devices)
            }
        }
    }

    private fun observeTransferState() {
        viewModelScope.launch {
            transferScreenshotUseCase.observeTransferState().collect { state ->
                _uiState.value = _uiState.value.copy(transferState = state)
            }
        }
    }

    private fun handleDetectedGesture(gesture: Gesture) {
        viewModelScope.launch {
            val screenshot = processScreenshotUseCase()
            if (screenshot != null && _uiState.value.nearbyDevices.isNotEmpty()) {
                val target = transferScreenshotUseCase.selectTarget(gesture, _uiState.value.nearbyDevices)
                if (target != null) {
                    _uiState.value = _uiState.value.copy(selectedDevice = target)
                    transferScreenshotUseCase.transfer(target, screenshot.id)
                }
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
