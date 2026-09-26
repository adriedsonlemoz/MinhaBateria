package com.minhabateria.app.charging

import java.util.Locale

object ChargeTimeFormatter {
    fun mainLabel(estimate: ChargeTimeEstimator.Estimate?): String? {
        estimate ?: return null
        if (estimate.remainingMs == 0L) return "100% • carga completa"
        return "100% em aproximadamente ${compact(estimate.remainingMs)}"
    }

    fun sourceLabel(estimate: ChargeTimeEstimator.Estimate?): String? = when (estimate?.source) {
        ChargeTimeEstimator.Source.SYSTEM -> "Previsão calculada pelo Android"
        ChargeTimeEstimator.Source.SESSION -> "Previsão pelo ritmo desta carga"
        null -> null
    }

    fun compact(milliseconds: Long): String {
        val totalMinutes = ((milliseconds + 59_999L) / 60_000L).coerceAtLeast(1L)
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return when {
            hours == 0L -> String.format(Locale.getDefault(), "%d min", minutes)
            minutes == 0L -> String.format(Locale.getDefault(), "%d h", hours)
            else -> String.format(Locale.getDefault(), "%d h %02d min", hours, minutes)
        }
    }
}
