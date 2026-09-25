package com.minhabateria.app.history

import com.minhabateria.app.session.SessionFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryFormatter {
    fun dateTime(timestampMs: Long): String =
        SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(Date(timestampMs))

    fun title(entry: HistoryEntry): String =
        entry.profileName ?: entry.detectedSource ?: "Sessão de carregamento"

    fun source(entry: HistoryEntry): String {
        val parts = listOfNotNull(entry.profileType, entry.detectedSource)
        return parts.joinToString(" • ").ifBlank { "Fonte não identificada" }
    }

    fun primary(entry: HistoryEntry): String = listOf(
        SessionFormatter.duration(entry.elapsedMs),
        SessionFormatter.energy(entry.energyWh),
        SessionFormatter.charge(entry.chargeMah)
    ).joinToString(" • ")

    fun power(entry: HistoryEntry): String =
        "Média ${SessionFormatter.power(entry.averagePowerW)} • Pico ${SessionFormatter.power(entry.maxPowerW)}"

    fun battery(entry: HistoryEntry): String =
        "Bateria ${SessionFormatter.batteryRange(entry.startPercent, entry.endPercent)} • ${SessionFormatter.gain(entry.gainPercent)}"

    fun temperature(entry: HistoryEntry): String =
        "Máx. ${SessionFormatter.temperature(entry.maxTemperatureC)} • ${stability(entry.powerVariationRatio)} • ${entry.interruptions} interrupções"

    fun stability(ratio: Double?): String = when {
        ratio == null -> "Estabilidade indisponível"
        ratio <= 0.10 -> "Muito estável"
        ratio <= 0.20 -> "Estável"
        ratio <= 0.35 -> "Oscilando"
        else -> "Muito variável"
    }
}
