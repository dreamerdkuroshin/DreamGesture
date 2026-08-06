package com.gestureshare.core.domain.repository

import com.gestureshare.core.domain.model.Gesture
import com.gestureshare.core.domain.model.GestureEvent
import com.gestureshare.core.domain.model.GestureSequence
import kotlinx.coroutines.flow.Flow

interface GestureRepository {
    fun observeGestures(): Flow<GestureEvent>
    suspend fun detectGesture(byteArray: ByteArray, width: Int, height: Int): Gesture?
    fun observeGestureSequences(): Flow<GestureSequence>
    suspend fun registerCustomGesture(name: String, samples: List<Gesture>)
}
