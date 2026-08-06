package com.gestureshare.feature.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AnimationPhase {
    IDLE,
    FLOATING,
    TRACKING,
    FLYING,
    DISAPPEARING,
    COMPLETE
}

class GestureAnimationState {
    val position = Animatable(Offset.Zero, Offset.VectorConverter)
    val scale = Animatable(1f)
    val rotation = Animatable(0f)
    val alpha = Animatable(1f)
    var phase by mutableStateOf(AnimationPhase.IDLE)
        private set

    suspend fun startFloating(initialPosition: Offset) {
        phase = AnimationPhase.FLOATING
        position.snapTo(initialPosition)
        scale.snapTo(0.8f)
        alpha.snapTo(1f)
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
    }

    suspend fun trackHand(handPosition: Offset) {
        phase = AnimationPhase.TRACKING
        position.animateTo(
            targetValue = handPosition,
            animationSpec = tween(durationMillis = 50)
        )
    }

    suspend fun flyTo(target: Offset, durationMs: Long = 600) {
        phase = AnimationPhase.FLYING
        launch {
            scale.animateTo(0.3f, tween(durationMs.toInt()))
        }
        launch {
            rotation.animateTo(720f, tween(durationMs.toInt()))
        }
        position.animateTo(
            targetValue = target,
            animationSpec = tween(durationMs.toInt())
        )
    }

    suspend fun disappear() {
        phase = AnimationPhase.DISAPPEARING
        alpha.animateTo(0f, tween(200))
        phase = AnimationPhase.COMPLETE
    }

    suspend fun reset() {
        phase = AnimationPhase.IDLE
        position.snapTo(Offset.Zero)
        scale.snapTo(1f)
        rotation.snapTo(0f)
        alpha.snapTo(1f)
    }
}

@Composable
fun rememberGestureAnimationState(): GestureAnimationState {
    return remember { GestureAnimationState() }
}
