package com.minhabateria.app.discharge

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DischargeFormatter {
    private val locale = Locale("pt", "BR")

    fun rate(value: Double?): String = value?.let { String.format(locale, "%.2f %%/h", it) } ?: "Calculando…"

    fun duration(ms: Long): String {
        val totalMinutes = ms.coerceAtLeast(0L) / 60_000L
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}min" else "${minutes} min"
    }

    fun remaining(ms: Long?): String = ms?.let(::duration) ?: "Aguardando dados"

    fun date(ms: Long): String = SimpleDateFormat("dd/MM • HH:mm", locale).format(Date(ms))
}
