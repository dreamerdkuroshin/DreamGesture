package com.gestureshare.core.domain.usecase

import com.gestureshare.core.domain.model.GestureEvent
import com.gestureshare.core.domain.repository.GestureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGesturesUseCase @Inject constructor(
    private val gestureRepository: GestureRepository
) {
    operator fun invoke(): Flow<GestureEvent> {
        return gestureRepository.observeGestures()
    }
}
