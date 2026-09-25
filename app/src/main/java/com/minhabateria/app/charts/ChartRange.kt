package com.minhabateria.app.charts

enum class ChartRange(val minutes: Int) {
    MINUTES_5(5),
    MINUTES_15(15),
    MINUTES_60(60);

    val durationMs: Long
        get() = minutes * 60_000L
}
