package com.gestureshare.feature.gesture

import com.gestureshare.core.domain.model.Gesture
import com.gestureshare.core.domain.model.GestureEvent
import com.gestureshare.core.domain.model.GestureType
import com.gestureshare.core.domain.model.HandLandmark
import com.gestureshare.core.domain.model.HandPose
import com.gestureshare.core.domain.model.HandSide
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class GestureEngineTest {

    private lateinit var engine: GestureEngine

    @Before
    fun setup() {
        engine = GestureEngine()
    }

    @Test
    fun `processGesture rejects low confidence gestures`() = runBlocking {
        val gesture = createGesture(GestureType.PALM, confidence = 0.5f)
        val result = engine.processGesture(gesture)
        assertThat(result).isFalse()
    }

    @Test
    fun `processGesture accepts high confidence gestures`() = runBlocking {
        val gesture = createGesture(GestureType.PALM, confidence = 0.97f)
        val result = engine.processGesture(gesture)
        assertThat(result).isTrue()
    }

    @Test
    fun `cooldown prevents rapid re-triggering`() = runBlocking {
        val gesture1 = createGesture(GestureType.PALM, confidence = 0.97f)
        val gesture2 = createGesture(GestureType.PALM, confidence = 0.97f)

        engine.processGesture(gesture1)
        val result = engine.processGesture(gesture2)
        assertThat(result).isFalse()
    }

    @Test
    fun `reset clears all state`() = runBlocking {
        val gesture = createGesture(GestureType.PALM, confidence = 0.97f)
        engine.processGesture(gesture)
        engine.reset()
        assertThat(engine.getMotionHistory()).isEmpty()
    }

    private fun createGesture(type: GestureType, confidence: Float): Gesture {
        return Gesture(
            type = type,
            confidence = confidence,
            handPose = HandPose(
                side = HandSide.LEFT,
                landmarks = List(21) { HandLandmark(0.5f, 0.5f, 0f) },
                confidence = confidence
            )
        )
    }
}
