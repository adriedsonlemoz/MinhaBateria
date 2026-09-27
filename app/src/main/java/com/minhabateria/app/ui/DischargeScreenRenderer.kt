package com.minhabateria.app.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.calculation.BatteryRateEstimator
import com.minhabateria.app.discharge.ActiveDischarge
import com.minhabateria.app.discharge.CompletedDischarge
import com.minhabateria.app.discharge.DischargeFormatter
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class DischargeScreenRenderer(private val activity: Activity) {
    private val status: TextView = activity.findViewById(R.id.dischargeStatus)
    private val rate: TextView = activity.findViewById(R.id.dischargeRateValue)
    private val drop: TextView = activity.findViewById(R.id.dischargeDropValue)
    private val elapsed: TextView = activity.findViewById(R.id.dischargeElapsedValue)
    private val autonomy: TextView = activity.findViewById(R.id.dischargeAutonomyValue)
    private val endTime: TextView = activity.findViewById(R.id.dischargeEndTimeValue)
    private val range: TextView = activity.findViewById(R.id.dischargeBatteryRange)
    private val quality: TextView = activity.findViewById(R.id.dischargeQualityValue)
    private val oneHour: TextView = activity.findViewById(R.id.dischargeOneHourValue)
    private val historicalAverage: TextView = activity.findViewById(R.id.dischargeHistoricalAverageValue)
    private val historicalLabel: TextView = activity.findViewById(R.id.dischargeHistoricalAverageLabel)
    private val estimateHint: TextView = activity.findViewById(R.id.dischargeEstimateHint)
    private val headerSummary: TextView = activity.findViewById(R.id.dischargeHeaderSummary)
    private val trend: DischargeTrendView = activity.findViewById(R.id.dischargeTrendView)
    private val trendStart: TextView = activity.findViewById(R.id.dischargeTrendStart)
    private val trendCurrent: TextView = activity.findViewById(R.id.dischargeTrendCurrent)
    private val trendElapsed: TextView = activity.findViewById(R.id.dischargeTrendElapsed)
    private val insightComparison: TextView = activity.findViewById(R.id.dischargeInsightComparison)
    private val insightEnd: TextView = activity.findViewById(R.id.dischargeInsightEnd)
    private val empty: TextView = activity.findViewById(R.id.dischargeHistoryEmpty)
    private val list: LinearLayout = activity.findViewById(R.id.dischargeHistoryList)
    private val locale = Locale("pt", "BR")

    fun render(active: ActiveDischarge?, entries: List<CompletedDischarge>, monitoring: Boolean) {
        val historicalRate = historicalRate(entries)
        renderCurrent(active, monitoring, historicalRate)
        renderHistoricalSummary(entries, historicalRate)
        renderHistory(entries)
    }

    private fun renderCurrent(active: ActiveDischarge?, monitoring: Boolean, historicalRate: Double?) {
        if (active == null) {
            status.text = if (monitoring) {
                "Retire o carregador para iniciar uma nova medição automaticamente."
            } else {
                "Ative o monitoramento contínuo para registrar a descarga mesmo com o app fechado."
            }
            rate.text = "—"
            drop.text = "—"
            elapsed.text = "—"
            autonomy.text = "—"
            endTime.text = "Horário previsto aparece com uma amostra válida"
            range.text = "Nenhuma descarga em andamento"
            quality.text = "Aguardando"
            quality.setTextColor(activity.getColor(R.color.accent_blue))
            oneHour.text = "—"
            estimateHint.text = "A estimativa aparece após pelo menos 3 min e 1% de queda real da bateria."
            headerSummary.text = "Acompanhe quanto a bateria consome fora da tomada"
            trend.setData(null, null, false)
            trendStart.text = "—"
            trendCurrent.text = "—"
            trendElapsed.text = "Aguardando"
            insightComparison.text = "• Aguardando dados para comparar o consumo"
            insightEnd.text = "• A previsão de término aparecerá quando houver uma amostra válida"
            return
        }

        val currentRate = BatteryRateEstimator.dischargePercentPerHour(active)
        val remainingMs = BatteryRateEstimator.dischargeRemainingMs(active)
        val reliable = BatteryRateEstimator.hasEnoughDischargeSample(active)
        status.text = if (currentRate == null) {
            "Medição ativa. A taxa aparece após pelo menos 3 min e 1% de queda real."
        } else {
            "Medição automática ativa • média calculada desde ${active.startPercent}%"
        }
        rate.text = DischargeFormatter.rate(currentRate)
        drop.text = "${active.dropPercent}%"
        elapsed.text = DischargeFormatter.duration(active.durationMs)
        autonomy.text = DischargeFormatter.remaining(remainingMs)
        endTime.text = predictedEndLabel(remainingMs)
        range.text = "Bateria ${active.startPercent}% → ${active.currentPercent}%"
        quality.text = qualityLabel(active)
        quality.setTextColor(activity.getColor(if (isGoodSample(active)) R.color.accent_green else R.color.accent_blue))
        oneHour.text = projectedAfterOneHour(active, currentRate)
        estimateHint.text = qualityHint(active)
        headerSummary.text = "Medição atual • ${active.currentPercent}% de bateria"
        trend.setData(active.startPercent, active.currentPercent, reliable)
        trendStart.text = "${active.startPercent}%"
        trendCurrent.text = "${active.currentPercent}%"
        trendElapsed.text = "Há ${DischargeFormatter.duration(active.durationMs)}"
        insightComparison.text = comparisonInsight(currentRate, historicalRate)
        insightEnd.text = endingInsight(remainingMs)
    }

    private fun renderHistoricalSummary(entries: List<CompletedDischarge>, historicalRate: Double?) {
        historicalAverage.text = historicalRate?.let(DischargeFormatter::rate) ?: "—"
        historicalLabel.text = when (entries.size) {
            0 -> "Sem descargas salvas"
            1 -> "1 descarga salva"
            else -> "${entries.size} descargas salvas"
        }
    }

    private fun historicalRate(entries: List<CompletedDischarge>): Double? {
        val rates = entries.mapNotNull { it.ratePercentPerHour }.filter { it.isFinite() && it > 0.0 }
        return rates.takeIf { it.isNotEmpty() }?.average()
    }

    private fun qualityLabel(active: ActiveDischarge): String = when {
        active.dropPercent <= 0 -> "Aguardando"
        isGoodSample(active) -> "Boa amostra"
        active.dropPercent >= 2 || active.durationMs >= 20 * 60_000L -> "Em evolução"
        else -> "Amostra inicial"
    }

    private fun isGoodSample(active: ActiveDischarge): Boolean =
        active.dropPercent >= 5 || active.durationMs >= 60 * 60_000L

    private fun qualityHint(active: ActiveDischarge): String = when {
        !BatteryRateEstimator.hasEnoughDischargeSample(active) ->
            "A estimativa aparece após pelo menos 3 min e 1% de queda real da bateria."
        isGoodSample(active) ->
            "Estimativa baseada no ritmo atual. Tela, brilho, sinal e uso podem alterar o resultado."
        else ->
            "A média ainda pode variar. Deixe a medição rodar por mais tempo para melhorar a previsão."
    }

    private fun projectedAfterOneHour(active: ActiveDischarge, ratePerHour: Double?): String {
        val rateValue = ratePerHour ?: return "—"
        val projected = (active.currentPercent - rateValue).coerceIn(0.0, 100.0).roundToInt()
        return "$projected%"
    }

    private fun comparisonInsight(currentRate: Double?, historicalRate: Double?): String {
        currentRate ?: return "• Aguardando amostra suficiente para comparar o consumo"
        historicalRate ?: return "• Primeira referência: ainda não há média histórica para comparar"
        val relation = currentRate / historicalRate
        return when {
            relation >= 1.12 -> "• Consumo acima da média histórica"
            relation <= 0.88 -> "• Consumo abaixo da média histórica"
            else -> "• Consumo próximo da média histórica"
        }
    }

    private fun predictedEndLabel(remainingMs: Long?): String {
        val target = predictedTarget(remainingMs) ?: return "Horário previsto aparece com uma amostra válida"
        return when (target.dayOffset) {
            0 -> "Pode acabar por volta de ${target.time}"
            1 -> "Pode acabar amanhã por volta de ${target.time}"
            else -> "Pode acabar em ${target.date} por volta de ${target.time}"
        }
    }

    private fun endingInsight(remainingMs: Long?): String {
        val target = predictedTarget(remainingMs)
            ?: return "• A previsão de término aparecerá quando houver uma amostra válida"
        return when (target.dayOffset) {
            0 -> "• No ritmo atual, pode chegar a 0% hoje por volta de ${target.time}"
            1 -> "• No ritmo atual, pode chegar a 0% amanhã por volta de ${target.time}"
            else -> "• No ritmo atual, pode chegar a 0% em ${target.date} por volta de ${target.time}"
        }
    }

    private fun predictedTarget(remainingMs: Long?): PredictedTarget? {
        remainingMs ?: return null
        if (remainingMs <= 0L) return null
        val nowMs = System.currentTimeMillis()
        val targetMs = nowMs + remainingMs
        if (targetMs < nowMs) return null
        val zone = java.time.ZoneId.systemDefault()
        val startDate = Instant.ofEpochMilli(nowMs).atZone(zone).toLocalDate()
        val targetDate = Instant.ofEpochMilli(targetMs).atZone(zone).toLocalDate()
        val dayOffset = ChronoUnit.DAYS.between(startDate, targetDate).toInt().coerceAtLeast(0)
        return PredictedTarget(
            time = SimpleDateFormat("HH:mm", locale).format(Date(targetMs)),
            date = SimpleDateFormat("dd/MM", locale).format(Date(targetMs)),
            dayOffset = dayOffset
        )
    }


    private fun renderHistory(entries: List<CompletedDischarge>) {
        list.removeAllViews()
        empty.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        val inflater = LayoutInflater.from(activity)
        entries.forEach { entry ->
            val row = inflater.inflate(R.layout.discharge_history_item, list, false)
            row.findViewById<TextView>(R.id.dischargeHistoryRate).text = DischargeFormatter.rate(entry.ratePercentPerHour)
            row.findViewById<TextView>(R.id.dischargeHistoryDate).text = DischargeFormatter.date(entry.startedAtMs)
            row.findViewById<TextView>(R.id.dischargeHistoryRange).text = "${entry.startPercent}% → ${entry.endPercent}%"
            row.findViewById<TextView>(R.id.dischargeHistoryDuration).text = DischargeFormatter.duration(entry.durationMs)
            list.addView(row)
        }
    }

    private data class PredictedTarget(val time: String, val date: String, val dayOffset: Int)
}
