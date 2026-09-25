package com.minhabateria.app.source

import kotlin.math.roundToInt

object NominalPowerComparison {
    fun compact(currentPowerW: Double?, peakPowerW: Double?, nominalPowerW: Double?): String? {
        if (nominalPowerW == null || nominalPowerW <= 0.0) return null
        val current = percent(currentPowerW, nominalPowerW)
        val peak = percent(peakPowerW, nominalPowerW)
        return when {
            current != null -> "$current% DA REF. • OBSERVADO"
            peak != null -> "PICO $peak% DA REF."
            else -> null
        }
    }

    fun detailed(powerW: Double?, nominalPowerW: Double?): String? {
        val pct = percent(powerW, nominalPowerW) ?: return null
        return "$pct% da potência nominal observada no aparelho"
    }

    private fun percent(value: Double?, nominal: Double?): Int? {
        if (value == null || nominal == null || value < 0.0 || nominal <= 0.0) return null
        return ((value / nominal) * 100.0).roundToInt()
    }
}
