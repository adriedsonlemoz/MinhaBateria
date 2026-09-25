package com.minhabateria.app.charts

data class ChartSample(
    val timestampMs: Long,
    val powerW: Double?,
    val currentMa: Double?,
    val temperatureC: Double?,
    val batteryPercent: Int?
)
