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
import kotlin.math.roundToInt

class DischargeScreenRenderer(private val activity: Activity) {
    private val status: TextView = activity.findViewById(R.id.dischargeStatus)
    private val rate: TextView = activity.findViewById(R.id.dischargeRateValue)
    private val drop: TextView = activity.findViewById(R.id.dischargeDropValue)
    private val elapsed: TextView = activity.findViewById(R.id.dischargeElapsedValue)
    private val autonomy: TextView = activity.findViewById(R.id.dischargeAutonomyValue)
    private val range: TextView = activity.findViewById(R.id.dischargeBatteryRange)
    private val quality: TextView = activity.findViewById(R.id.dischargeQualityValue)
    private val oneHour: TextView = activity.findViewById(R.id.dischargeOneHourValue)
    private val historicalAverage: TextView = activity.findViewById(R.id.dischargeHistoricalAverageValue)
    private val historicalLabel: TextView = activity.findViewById(R.id.dischargeHistoricalAverageLabel)
    private val estimateHint: TextView = activity.findViewById(R.id.dischargeEstimateHint)
    private val headerSummary: TextView = activity.findViewById(R.id.dischargeHeaderSummary)
    private val empty: TextView = activity.findViewById(R.id.dischargeHistoryEmpty)
    private val list: LinearLayout = activity.findViewById(R.id.dischargeHistoryList)

    fun render(active: ActiveDischarge?, entries: List<CompletedDischarge>, monitoring: Boolean) {
        renderCurrent(active, monitoring)
        renderHistoricalSummary(entries)
        renderHistory(entries)
    }

    private fun renderCurrent(active: ActiveDischarge?, monitoring: Boolean) {
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
            range.text = "Nenhuma descarga em andamento"
            quality.text = "Aguardando"
            oneHour.text = "—"
            estimateHint.text = "A estimativa aparece após pelo menos 3 min e 1% de queda real da bateria."
            headerSummary.text = "Acompanhe quanto a bateria consome fora da tomada"
            return
        }

        val currentRate = BatteryRateEstimator.dischargePercentPerHour(active)
        status.text = if (currentRate == null) {
            "Medição ativa. A taxa aparece após pelo menos 3 min e 1% de queda real."
        } else {
            "Medição automática ativa • média calculada desde ${active.startPercent}%"
        }
        rate.text = DischargeFormatter.rate(currentRate)
        drop.text = "${active.dropPercent}%"
        elapsed.text = DischargeFormatter.duration(active.durationMs)
        autonomy.text = DischargeFormatter.remaining(BatteryRateEstimator.dischargeRemainingMs(active))
        range.text = "Bateria ${active.startPercent}% → ${active.currentPercent}%"
        quality.text = qualityLabel(active)
        oneHour.text = projectedAfterOneHour(active, currentRate)
        estimateHint.text = qualityHint(active)
        headerSummary.text = "Medição atual: ${active.currentPercent}% de bateria"
    }

    private fun renderHistoricalSummary(entries: List<CompletedDischarge>) {
        val rates = entries.mapNotNull { it.ratePercentPerHour }.filter { it.isFinite() && it > 0.0 }
        historicalAverage.text = if (rates.isEmpty()) "—" else DischargeFormatter.rate(rates.average())
        historicalLabel.text = when (entries.size) {
            0 -> "Média histórica"
            1 -> "1 descarga salva"
            else -> "${entries.size} descargas salvas"
        }
    }

    private fun qualityLabel(active: ActiveDischarge): String = when {
        active.dropPercent <= 0 -> "Aguardando"
        active.dropPercent >= 5 || active.durationMs >= 60 * 60_000L -> "Boa amostra"
        active.dropPercent >= 2 || active.durationMs >= 20 * 60_000L -> "Em evolução"
        else -> "Amostra inicial"
    }

    private fun qualityHint(active: ActiveDischarge): String = when {
        !BatteryRateEstimator.hasEnoughDischargeSample(active) ->
            "A estimativa aparece após pelo menos 3 min e 1% de queda real da bateria."
        active.dropPercent >= 5 || active.durationMs >= 60 * 60_000L ->
            "Amostra mais estável. Mudanças de brilho, sinal, tela e uso ainda podem alterar o consumo."
        else -> "A média ainda pode variar bastante. Deixe a medição rodar por mais tempo para melhorar a referência."
    }

    private fun projectedAfterOneHour(active: ActiveDischarge, ratePerHour: Double?): String {
        val rateValue = ratePerHour ?: return "—"
        val projected = (active.currentPercent - rateValue).coerceIn(0.0, 100.0).roundToInt()
        return "$projected%"
    }

    private fun renderHistory(entries: List<CompletedDischarge>) {
        list.removeAllViews()
        empty.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        val inflater = LayoutInflater.from(activity)
        entries.forEach { entry ->
            val row = inflater.inflate(R.layout.discharge_history_item, list, false)
            row.findViewById<TextView>(R.id.dischargeHistoryRate).text = DischargeFormatter.rate(entry.ratePercentPerHour)
            row.findViewById<TextView>(R.id.dischargeHistoryDate).text = DischargeFormatter.date(entry.startedAtMs)
            row.findViewById<TextView>(R.id.dischargeHistoryRange).text = "${entry.startPercent}% → ${entry.endPercent}% • -${entry.dropPercent}%"
            row.findViewById<TextView>(R.id.dischargeHistoryDuration).text = DischargeFormatter.duration(entry.durationMs)
            list.addView(row)
        }
    }
}
