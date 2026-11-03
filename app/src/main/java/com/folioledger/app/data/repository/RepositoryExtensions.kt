// File: app/src/main/java/com/folioledger/app/data/repository/RepositoryExtensions.kt
package com.folioledger.app.data.repository

import kotlinx.coroutines.CancellationException

/**
 * Runs the given [block] and returns its result encapsulated in a [Result].
 *
 * Unlike standard [kotlin.runCatching], this implementation re-throws any [CancellationException]
 * so that Kotlin Coroutines cooperative cancellation and structured concurrency are strictly preserved.
 */
inline fun <R> runCatchingNonCancellation(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
