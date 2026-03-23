package com.rpn.blockblaster.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

fun <T> Flow<T>.catchAndIgnore(): Flow<T> = catch { /* ignore */ }

fun Int.toFormattedScore(): String {
    return when {
        this >= 1_000_000 -> String.format("%.1fM", this / 1_000_000f)
        this >= 1_000     -> String.format("%.1fK", this / 1_000f)
        else              -> this.toString()
    }
}
