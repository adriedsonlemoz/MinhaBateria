package com.minhabateria.app.history

import com.minhabateria.app.session.SessionFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

object HistoryComparisonFormatter {
    data class Row(
        val label: String,
        val first: String,
        val second: String,
        val difference: String,
        val firstRatio: Int = 0,
        val secondRatio: Int = 0
    )

    data class Summary(
        val title: String,
        val badge: String,
        val chargeValue: String,
        val chargeCaption: String,
        val energyValue: String,
        val energyCaption: String,
        val durationText: String,
        val powerText: String,
        val contextText: String,
        val explanation: String
    )

    fun quickRows(first: HistoryEntry, second: HistoryEntry): List<Row> = listOf(
        durationRow("Duração da sessão", first.elapsedMs, second.elapsedMs, "durou"),
        durationRow("Tempo carregando", first.chargingTimeMs, second.chargingTimeMs, "carregou por"),
        doubleRow("Energia estimada", first.energyWh, second.energyWh, "Wh", 2, "entregou"),
        doubleRow("Carga estimada", first.chargeMah, second.chargeMah, "mAh", 0, "acumulou"),
        doubleRow("Potência média", first.averagePowerW, second.averagePowerW, "W", 2, "ficou"),
        doubleRow("Pico de potência", first.maxPowerW, second.maxPowerW, "W", 2, "atingiu")
    )

    fun technicalRows(first: HistoryEntry, second: HistoryEntry): List<Row> = listOf(
        doubleRow("Corrente média", first.averageCurrentMa, second.averageCurrentMa, "mA", 0, "ficou"),
        doubleRow("Tensão média", first.averageVoltageV, second.averageVoltageV, "V", 2, "ficou"),
        doubleRow("Temperatura máxima", first.maxTemperatureC, second.maxTemperatureC, "°C", 1, "chegou"),
        intRow("Interrupções", first.interruptions, second.interruptions, ""),
        nullableIntRow("Ganho da bateria", first.gainPercent, second.gainPercent, "%"),
        percentOfReferenceRow("Potência média / referência", first, second),
        percentOfReferencePeakRow("Pico / referência", first, second),
        stabilityRow(first, second)
    )

    fun summary(first: HistoryEntry, second: HistoryEntry): Summary {
        val chargeDiff = difference(first.chargeMah, second.chargeMah)
        val energyDiff = difference(first.energyWh, second.energyWh)
        val durationDiff = difference(first.elapsedMs?.toDouble(), second.elapsedMs?.toDouble())
        val averagePowerDiff = difference(first.averagePowerW, second.averagePowerW)

        val primary = when {
            chargeDiff != null && abs(chargeDiff) >= 1.0 -> metricWinner(chargeDiff, "acumulou mais carga")
            energyDiff != null && abs(energyDiff) >= 0.005 -> metricWinner(energyDiff, "entregou mais energia ao aparelho")
            first.gainPercent != null && second.gainPercent != null && first.gainPercent != second.gainPercent ->
                metricWinner((first.gainPercent - second.gainPercent).toDouble(), "ganhou mais bateria")
            averagePowerDiff != null && abs(averagePowerDiff) >= 0.01 -> metricWinner(averagePowerDiff, "teve maior potência média")
            else -> "Resultados muito próximos"
        }

        val chargeHighlight = highlight(
            first.chargeMah,
            second.chargeMah,
            unit = "mAh",
            decimals = 0,
            caption = "mais carga acumulada"
        )
        val energyHighlight = highlight(
            first.energyWh,
            second.energyWh,
            unit = "Wh",
            decimals = 2,
            caption = "mais energia observada"
        )

        val durationText = compactDifference(
            first.elapsedMs?.toDouble(),
            second.elapsedMs?.toDouble(),
            format = { SessionFormatter.duration(it.toLong()) },
            suffix = "de duração"
        )
        val powerText = compactDifference(
            first.averagePowerW,
            second.averagePowerW,
            format = { formatNumber(it, "W", 2) },
            suffix = "de potência média"
        )

        val context = buildContext(durationDiff, averagePowerDiff)
        val explanation = buildExplanation(primary, context)
        val badge = if (primary == "Resultados muito próximos") "Resultados próximos" else "Maior resultado"

        return Summary(
            title = primary,
            badge = badge,
            chargeValue = chargeHighlight.first,
            chargeCaption = chargeHighlight.second,
            energyValue = energyHighlight.first,
            energyCaption = energyHighlight.second,
            durationText = durationText,
            powerText = powerText,
            contextText = context,
            explanation = explanation
        )
    }

    fun sessionTitle(entry: HistoryEntry): String {
        val base = HistoryFormatter.title(entry)
        val nominal = entry.nominalPowerW?.takeIf { it > 0.0 } ?: return base
        val power = formatNumber(nominal, "W", if (nominal % 1.0 == 0.0) 0 else 1)
        return if (base.contains(power, ignoreCase = true)) base else "$base • $power"
    }

    fun sessionDate(entry: HistoryEntry): String = HistoryFormatter.dateTime(entry.endedAtMs)

    fun sessionSource(entry: HistoryEntry): String = entry.profileType ?: entry.detectedSource ?: "Fonte não identificada"

    private fun durationRow(label: String, first: Long?, second: Long?, verb: String): Row {
        val a = SessionFormatter.duration(first)
        val b = SessionFormatter.duration(second)
        val ratios = relativeRatios(first?.toDouble(), second?.toDouble())
        val delta = if (first == null || second == null) "Diferença indisponível" else {
            humanDifference(first.toDouble(), second.toDouble(), { SessionFormatter.duration(it.toLong()) }, verb)
        }
        return Row(label, a, b, delta, ratios.first, ratios.second)
    }

    private fun doubleRow(
        label: String,
        first: Double?,
        second: Double?,
        unit: String,
        decimals: Int,
        verb: String
    ): Row {
        val a = formatNumber(first, unit, decimals)
        val b = formatNumber(second, unit, decimals)
        val ratios = relativeRatios(first, second)
        val delta = if (first == null || second == null) "Diferença indisponível" else {
            humanDifference(first, second, { formatNumber(it, unit, decimals) }, verb)
        }
        return Row(label, a, b, delta, ratios.first, ratios.second)
    }

    private fun intRow(label: String, first: Int, second: Int, unit: String): Row = Row(
        label = label,
        first = "$first$unit",
        second = "$second$unit",
        difference = when {
            first > second -> "A teve ${first - second} a mais"
            second > first -> "B teve ${second - first} a mais"
            else -> "Mesmo valor nas duas sessões"
        }
    )

    private fun nullableIntRow(label: String, first: Int?, second: Int?, unit: String): Row {
        val a = first?.let { "$it$unit" } ?: "Indisponível"
        val b = second?.let { "$it$unit" } ?: "Indisponível"
        val delta = if (first == null || second == null) "Diferença indisponível" else when {
            first > second -> "A ficou +${first - second}$unit"
            second > first -> "B ficou +${second - first}$unit"
            else -> "Mesmo resultado"
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
        val delta = if (a == null || b == null) "Diferença indisponível" else when {
            a > b -> "A ficou ${a - b} p.p. acima"
            b > a -> "B ficou ${b - a} p.p. acima"
            else -> "Mesmo percentual da referência"
        }
        return Row(label, a?.let { "$it%" } ?: "Indisponível", b?.let { "$it%" } ?: "Indisponível", delta)
    }

    private fun stabilityRow(first: HistoryEntry, second: HistoryEntry): Row = Row(
        label = "Estabilidade da potência",
        first = HistoryFormatter.stability(first.powerVariationRatio),
        second = HistoryFormatter.stability(second.powerVariationRatio),
        difference = "Descrição do comportamento observado; não prova defeito ou qualidade da fonte."
    )

    private fun metricWinner(diffFirstMinusSecond: Double, phrase: String): String = when {
        diffFirstMinusSecond > 0.0 -> "Sessão A $phrase"
        diffFirstMinusSecond < 0.0 -> "Sessão B $phrase"
        else -> "Resultados muito próximos"
    }

    private fun highlight(
        first: Double?,
        second: Double?,
        unit: String,
        decimals: Int,
        caption: String
    ): Pair<String, String> {
        if (first == null || second == null) return "—" to "comparação indisponível"
        val diff = first - second
        if (abs(diff) < 0.000001) return "≈ igual" to caption
        val winner = if (diff > 0.0) "A" else "B"
        val value = formatNumber(abs(diff), unit, decimals)
        return "$winner +$value" to caption
    }

    private fun compactDifference(
        first: Double?,
        second: Double?,
        format: (Double) -> String,
        suffix: String
    ): String {
        if (first == null || second == null) return "Comparação indisponível"
        val diff = first - second
        if (abs(diff) < 0.000001) return "Mesmo valor de $suffix"
        val winner = if (diff > 0.0) "A" else "B"
        return "$winner +${format(abs(diff))} $suffix"
    }

    private fun buildContext(
        durationDiff: Double?,
        powerDiff: Double?
    ): String {
        if (durationDiff == null || powerDiff == null) {
            return "Alguns dados não estavam disponíveis; compare também nível da bateria, temperatura e uso do celular."
        }
        val durationWinner = signLabel(durationDiff)
        val powerWinner = signLabel(powerDiff)
        if (durationWinner == null && powerWinner == null) return "Duração e potência média ficaram muito próximas."
        if (durationWinner != null && powerWinner != null && durationWinner != powerWinner) {
            return "Sessão $powerWinner teve maior potência média, mas a Sessão $durationWinner permaneceu carregando por mais tempo."
        }
        if (durationWinner != null && powerWinner == durationWinner) {
            return "Sessão $durationWinner ficou mais tempo e também teve maior potência média."
        }
        if (durationWinner != null) return "Sessão $durationWinner permaneceu carregando por mais tempo."
        return "Sessão $powerWinner teve maior potência média."
    }

    private fun buildExplanation(primary: String, context: String): String {
        val start = if (primary == "Resultados muito próximos") {
            "As duas sessões tiveram resultados próximos nos dados principais disponíveis."
        } else {
            "$primary no período observado."
        }
        return "$start $context Isso não prova que uma fonte é melhor: duração, nível da bateria, temperatura, cabo, protocolo e uso do celular podem alterar o resultado."
    }

    private fun signLabel(value: Double): String? = when {
        value > 0.000001 -> "A"
        value < -0.000001 -> "B"
        else -> null
    }

    private fun difference(first: Double?, second: Double?): Double? =
        if (first == null || second == null) null else first - second

    private fun relativeRatios(first: Double?, second: Double?): Pair<Int, Int> {
        if (first == null && second == null) return 0 to 0
        if (first != null && second == null) return 100 to 0
        if (first == null && second != null) return 0 to 100
        val a = max(0.0, first ?: 0.0)
        val b = max(0.0, second ?: 0.0)
        val maximum = max(a, b)
        if (maximum <= 0.0) return 0 to 0
        return ((a / maximum) * 100.0).roundToInt().coerceIn(0, 100) to
            ((b / maximum) * 100.0).roundToInt().coerceIn(0, 100)
    }

    private fun humanDifference(
        first: Double,
        second: Double,
        formatter: (Double) -> String,
        verb: String
    ): String {
        val diff = first - second
        if (abs(diff) < 0.000001) return "Mesmo valor nas duas sessões"
        val winner = if (diff > 0.0) "A" else "B"
        return "$winner $verb +${formatter(abs(diff))}"
    }

    private fun percentage(power: Double?, reference: Double?): Int? {
        if (power == null || reference == null || reference <= 0.0) return null
        return ((power / reference) * 100.0).roundToInt()
    }

    private fun formatNumber(value: Double?, unit: String, decimals: Int): String {
        if (value == null || !value.isFinite()) return "Indisponível"
        val pattern = "%.$decimals" + "f"
        return String.format(Locale.getDefault(), "$pattern %s", value, unit).trim()
    }
}
