package com.minhabateria.app.discharge

data class ActiveDischarge(
    val startedAtMs: Long,
    val startPercent: Int,
    val lastObservedAtMs: Long,
    val currentPercent: Int
) {
    val durationMs: Long get() = (lastObservedAtMs - startedAtMs).coerceAtLeast(0L)
    val dropPercent: Int get() = (startPercent - currentPercent).coerceAtLeast(0)
    val rawRatePercentPerHour: Double?
        get() {
            if (dropPercent <= 0 || durationMs <= 0L) return null
            val hours = durationMs / 3_600_000.0
            return if (hours > 0.0) dropPercent / hours else null
        }
}

data class CompletedDischarge(
    val id: String,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val startPercent: Int,
    val endPercent: Int
) {
    val durationMs: Long get() = (endedAtMs - startedAtMs).coerceAtLeast(0L)
    val dropPercent: Int get() = (startPercent - endPercent).coerceAtLeast(0)
    val ratePercentPerHour: Double?
        get() {
            if (dropPercent <= 0 || durationMs <= 0L) return null
            return dropPercent / (durationMs / 3_600_000.0)
        }
}
