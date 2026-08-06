package com.gestureshare.feature.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class PhysicsAnimation(
    private val scope: CoroutineScope
) {
    val position = Animatable(Offset.Zero, Offset.VectorConverter)
    val velocity = Animatable(Offset.Zero, Offset.VectorConverter)
    val scale = Animatable(1f)

    private val gravity = 0.5f
    private val friction = 0.98f
    private val bounce = 0.7f

    fun applyForce(force: Offset) {
        scope.launch {
            val currentVel = velocity.value
            velocity.snapTo(
                Offset(
                    currentVel.x + force.x,
                    currentVel.y + force.y
                )
            )
        }
    }

    fun simulateFrame() {
        scope.launch {
            val currentVel = velocity.value
            val currentPos = position.value

            val newVelY = (currentVel.y + gravity) * friction
            val newVelX = currentVel.x * friction

            var newPosX = currentPos.x + newVelX
            var newPosY = currentPos.y + newVelY

            if (newPosY > 800f) {
                newPosY = 800f
                velocity.snapTo(Offset(newVelX, -newVelY * bounce))
            }

            position.snapTo(Offset(newPosX, newPosY))
        }
    }

    fun animateThrow(target: Offset) {
        scope.launch {
            val distance = target - position.value
            val throwVelocity = distance / 30f

            velocity.snapTo(throwVelocity)

            for (i in 0 until 60) {
                simulateFrame()
                kotlinx.coroutines.delay(16)
            }
        }
    }

    fun reset() {
        scope.launch {
            position.snapTo(Offset.Zero)
            velocity.snapTo(Offset.Zero)
            scale.snapTo(1f)
        }
    }
}
