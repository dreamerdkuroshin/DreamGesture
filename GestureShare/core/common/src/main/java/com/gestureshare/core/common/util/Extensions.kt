package com.gestureshare.core.common.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

fun <T> Flow<T>.asAppResult(): Flow<AppResult<T>> = this
    .map<T, AppResult<T>> { AppResult.Success(it) }
    .onStart { emit(AppResult.Loading) }
    .catch { emit(AppResult.Failure(AppResult.UnknownError(it.message ?: "Unknown error")))) }

fun <T, R> AppResult<T>.flatMap(transform: (T) -> AppResult<R>): AppResult<R> = when (this) {
    is AppResult.Success -> transform(data)
    is AppResult.Failure -> this
    is AppResult.Loading -> this
}
