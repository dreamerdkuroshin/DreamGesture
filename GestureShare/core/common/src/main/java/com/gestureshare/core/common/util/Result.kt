package com.gestureshare.core.common.util

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Failure(val error: AppError) : AppResult<Nothing>()
    object Loading : AppResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
        is Loading -> this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun errorOrNull(): AppError? = when (this) {
        is Failure -> error
        else -> null
    }
}

sealed class AppError(val message: String) {
    data class NetworkError(val msg: String) : AppError(msg)
    data class SecurityError(val msg: String) : AppError(msg)
    data class PermissionError(val msg: String) : AppError(msg)
    data class UnknownError(val msg: String) : AppError(msg)
}
