package com.minhabateria.app.history

import com.minhabateria.app.session.SessionFormatter
import java.util.Locale
import kotlin.math.abs

object HistoryComparisonFormatter {
    data class Row(
        val label: String,
        val first: String,
        val second: String,
        val difference: String
    )

    fun rows(first: HistoryEntry, second: HistoryEntry): List<Row> = listOf(
        durationRow("Duração da sessão", first.elapsedMs, second.elapsedMs),
        durationRow("Tempo efetivamente carregando", first.chargingTimeMs, second.chargingTimeMs),
        doubleRow("Energia estimada", first.energyWh, second.energyWh, "Wh", 2),
        doubleRow("Carga estimada", first.chargeMah, second.chargeMah, "mAh", 0),
        doubleRow("Potência média", first.averagePowerW, second.averagePowerW, "W", 2),
        doubleRow("Pico de potência", first.maxPowerW, second.maxPowerW, "W", 2),
        doubleRow("Corrente média", first.averageCurrentMa, second.averageCurrentMa, "mA", 0),
        doubleRow("Tensão média", first.averageVoltageV, second.averageVoltageV, "V", 2),
        doubleRow("Temperatura máxima", first.maxTemperatureC, second.maxTemperatureC, "°C", 1),
        intRow("Interrupções", first.interruptions, second.interruptions, ""),
        nullableIntRow("Ganho da bateria", first.gainPercent, second.gainPercent, "%"),
        percentOfReferenceRow("Potência média / referência", first, second),
        percentOfReferencePeakRow("Pico / referência", first, second),
        stabilityRow(first, second)
    )

    fun sessionTitle(entry: HistoryEntry): String = HistoryFormatter.title(entry)

    fun sessionSubtitle(entry: HistoryEntry): String =
        "${HistoryFormatter.dateTime(entry.endedAtMs)} • ${HistoryFormatter.source(entry)}"

    private fun durationRow(label: String, first: Long?, second: Long?): Row {
        val a = SessionFormatter.duration(first)
        val b = SessionFormatter.duration(second)
        val delta = if (first == null || second == null) "Diferença indisponível" else {
            val diff = second - first
            "B − A: ${signedDuration(diff)}"
        }
        return Row(label, a, b, delta)
    }

    private fun doubleRow(
        label: String,
        first: Double?,
        second: Double?,
        unit: String,
        decimals: Int
    ): Row {
        val a = formatNumber(first, unit, decimals)
        val b = formatNumber(second, unit, decimals)
        val delta = if (first == null || second == null) "Diferença indisponível" else {
            "B − A: ${formatSigned(second - first, unit, decimals)}"
        }
        return Row(label, a, b, delta)
    }

    private fun intRow(label: String, first: Int, second: Int, unit: String): Row = Row(
        label,
        "$first$unit",
        "$second$unit",
        "B − A: ${signedInt(second - first, unit)}"
    )

    private fun nullableIntRow(label: String, first: Int?, second: Int?, unit: String): Row {
        val a = first?.let { "$it$unit" } ?: "Indisponível"
        val b = second?.let { "$it$unit" } ?: "Indisponível"
        val delta = if (first == null || second == null) "Diferença indisponível" else {
            "B − A: ${signedInt(second - first, unit)}"
        }
        return Row(label, a, b, delta)
    }

    private fun percentOfReferenceRow(label: String, first: HistoryEntry, second: HistoryEntry): Row =
        referenceRow(label, first.averagePowerW, first.nominalPowerW, second.averagePowerW, second.nominalPowerW)

    private fun percentOfReferencePeakRow(label: String, first: HistoryEntry, second: HistoryEntry): Row =
        referenceRow(label, first.maxPowerW, first.nominalPowerW, second.maxPowerW, second.nominalPowerW)

    private fun referenceRow(
        label: String,
        firstPower: Double?,
        firstReference: Double?,
        secondPower: Double?,
        secondReference: Double?
    ): Row {
        val a = percentage(firstPower, firstReference)
        val b = percentage(secondPower, secondReference)
        val delta = if (a == null || b == null) "Diferença indisponível" else {
            "B − A: ${signedInt(b - a, " p.p.")}"
        }
        return Row(label, a?.let { "$it%" } ?: "Indisponível", b?.let { "$it%" } ?: "Indisponível", delta)
    }

    private fun stabilityRow(first: HistoryEntry, second: HistoryEntry): Row = Row(
        label = "Estabilidade da potência",
        first = HistoryFormatter.stability(first.powerVariationRatio),
        second = HistoryFormatter.stability(second.powerVariationRatio),
        difference = "Indicador descritivo; não define uma sessão vencedora."
    )

    private fun percentage(power: Double?, reference: Double?): Int? {
        if (power == null || reference == null || reference <= 0.0) return null
        return ((power / reference) * 100.0).toInt()
    }

    private fun formatNumber(value: Double?, unit: String, decimals: Int): String {
        if (value == null || !value.isFinite()) return "Indisponível"
        val pattern = "%.${decimals}f"
        return String.format(Locale.getDefault(), "$pattern %s", value, unit).trim()
    }

    private fun formatSigned(value: Double, unit: String, decimals: Int): String {
        val prefix = if (value > 0) "+" else if (value < 0) "−" else ""
        val absolute = abs(value)
        val pattern = "%.${decimals}f"
        return String.format(Locale.getDefault(), "$prefix$pattern %s", absolute, unit).trim()
    }

    private fun signedInt(value: Int, unit: String): String {
        val prefix = if (value > 0) "+" else if (value < 0) "−" else ""
        return "$prefix${abs(value)}$unit"
    }

    private fun signedDuration(valueMs: Long): String {
        val prefix = if (valueMs > 0) "+" else if (valueMs < 0) "−" else ""
        return "$prefix${SessionFormatter.duration(abs(valueMs))}"
    }
}
