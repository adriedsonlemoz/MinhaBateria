package com.minhabateria.app.session

import com.minhabateria.app.utils.TimeFormatter
import java.util.Locale

object SessionFormatter {
    fun duration(ms: Long?): String = TimeFormatter.elapsed(ms)

    fun energy(wh: Double?): String = wh?.let {
        val pattern = if (it < 0.1) "%.3f Wh" else "%.2f Wh"
        String.format(Locale.getDefault(), pattern, it)
    } ?: "Indisponível"

    fun charge(mah: Double?): String = mah?.let {
        String.format(Locale.getDefault(), "%.0f mAh", it)
    } ?: "Indisponível"

    fun power(value: Double?): String = value?.let {
        String.format(Locale.getDefault(), "%.2f W", it)
    } ?: "Indisponível"

    fun current(value: Double?): String = value?.let {
        String.format(Locale.getDefault(), "%.0f mA", it)
    } ?: "Indisponível"

    fun voltage(value: Double?): String = value?.let {
        String.format(Locale.getDefault(), "%.2f V", it)
    } ?: "Indisponível"

    fun temperature(value: Double?): String = value?.let {
        String.format(Locale.getDefault(), "%.1f °C", it)
    } ?: "Indisponível"

    fun currentRange(min: Double?, max: Double?): String = range(min, max, "%.0f", "mA")
    fun voltageRange(min: Double?, max: Double?): String = range(min, max, "%.2f", "V")

    fun batteryRange(start: Int?, current: Int?): String =
        if (start != null && current != null) "$start% → $current%" else "Indisponível"

    fun gain(value: Int?): String = value?.let { if (it >= 0) "+$it%" else "$it%" } ?: "Indisponível"

    private fun range(min: Double?, max: Double?, pattern: String, unit: String): String {
        if (min == null || max == null) return "Indisponível"
        val start = String.format(Locale.getDefault(), pattern, min)
        val end = String.format(Locale.getDefault(), pattern, max)
        return "$start – $end $unit"
    }
}
