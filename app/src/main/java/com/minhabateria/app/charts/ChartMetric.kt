package com.minhabateria.app.charts

import java.util.Locale

enum class ChartMetric(val title: String) {
    POWER("Potência"),
    CURRENT("Corrente"),
    TEMPERATURE("Temperatura"),
    BATTERY("Bateria");

    fun value(sample: ChartSample): Double? = when (this) {
        POWER -> sample.powerW
        CURRENT -> sample.currentMa
        TEMPERATURE -> sample.temperatureC
        BATTERY -> sample.batteryPercent?.toDouble()
    }

    fun format(value: Double?): String {
        if (value == null || !value.isFinite()) return "—"
        return when (this) {
            POWER -> String.format(Locale.getDefault(), "%.2f W", value)
            CURRENT -> String.format(Locale.getDefault(), "%.0f mA", value)
            TEMPERATURE -> String.format(Locale.getDefault(), "%.1f °C", value)
            BATTERY -> String.format(Locale.getDefault(), "%.0f%%", value)
        }
    }
}
