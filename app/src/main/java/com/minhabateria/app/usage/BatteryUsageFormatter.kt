package com.minhabateria.app.usage

import java.util.Locale

object BatteryUsageFormatter {
    fun duration(ms: Long): String {
        val totalMinutes = (ms / 60_000L).coerceAtLeast(1L)
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return when {
            hours == 0L -> "$minutes min"
            minutes == 0L -> "$hours h"
            else -> "$hours h ${minutes} min"
        }
    }

    fun rate(value: Double?): String = value?.let {
        String.format(Locale("pt", "BR"), "%.2f %%/h", it)
    } ?: "Calculando"

    fun current(value: Double?): String = value?.let {
        String.format(Locale("pt", "BR"), "%.0f mA", it)
    } ?: "Indisponível"

    fun impactLabel(sharePercent: Int): String = when {
        sharePercent >= 40 -> "Maior atividade"
        sharePercent >= 20 -> "Uso elevado"
        else -> "Uso frequente"
    }
}
