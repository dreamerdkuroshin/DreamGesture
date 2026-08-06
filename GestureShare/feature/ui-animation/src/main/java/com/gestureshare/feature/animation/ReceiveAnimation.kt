package com.gestureshare.feature.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ReceiveAnimationState(
    private val scope: CoroutineScope
) {
    val position = Animatable(Offset(500f, -200f), Offset.VectorConverter)
    val scale = Animatable(0.1f)
    val rotation = Animatable(-45f)
    val alpha = Animatable(0f)

    suspend fun playEntryAnimation() {
        alpha.snapTo(1f)
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            rotation.animateTo(0f, spring())
        }
        position.animateTo(
            targetValue = Offset(0f, 0f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    suspend fun flyToGallery(galleryPosition: Offset) {
        launch {
            scale.animateTo(0.3f, androidx.compose.animation.core.tween(400))
        }
        launch {
            rotation.animateTo(90f, androidx.compose.animation.core.tween(400))
        }
        position.animateTo(galleryPosition, androidx.compose.animation.core.tween(400))
        alpha.animateTo(0f, androidx.compose.animation.core.tween(200))
    }

    fun launch(block: suspend () -> Unit) {
        scope.launch { block() }
    }
}
