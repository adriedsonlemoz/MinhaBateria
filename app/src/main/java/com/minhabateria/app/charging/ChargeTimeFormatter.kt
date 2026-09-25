package com.minhabateria.app.charging

import java.util.Locale

object ChargeTimeFormatter {
    fun mainLabel(estimate: ChargeTimeEstimator.Estimate?): String? {
        estimate ?: return null
        if (estimate.remainingMs == 0L) return "Carga completa"
        return "≈ ${compact(estimate.remainingMs)} até 100%"
    }

    fun sourceLabel(estimate: ChargeTimeEstimator.Estimate?): String? = when (estimate?.source) {
        ChargeTimeEstimator.Source.SYSTEM -> "Estimativa do Android"
        ChargeTimeEstimator.Source.SESSION -> "Estimado pelo ritmo desta sessão"
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
