package com.minhabateria.app.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.discharge.ActiveDischarge
import com.minhabateria.app.discharge.CompletedDischarge
import com.minhabateria.app.discharge.DischargeFormatter

class DischargeScreenRenderer(private val activity: Activity) {
    private val status: TextView = activity.findViewById(R.id.dischargeStatus)
    private val rate: TextView = activity.findViewById(R.id.dischargeRateValue)
    private val drop: TextView = activity.findViewById(R.id.dischargeDropValue)
    private val elapsed: TextView = activity.findViewById(R.id.dischargeElapsedValue)
    private val autonomy: TextView = activity.findViewById(R.id.dischargeAutonomyValue)
    private val range: TextView = activity.findViewById(R.id.dischargeBatteryRange)
    private val empty: TextView = activity.findViewById(R.id.dischargeHistoryEmpty)
    private val list: LinearLayout = activity.findViewById(R.id.dischargeHistoryList)

    fun render(active: ActiveDischarge?, entries: List<CompletedDischarge>, monitoring: Boolean) {
        renderCurrent(active, monitoring)
        renderHistory(entries)
    }

    private fun renderCurrent(active: ActiveDischarge?, monitoring: Boolean) {
        if (active == null) {
            status.text = if (monitoring) "Conecte e retire o carregador para iniciar uma medição." else "Ative o monitoramento contínuo para registrar a descarga."
            rate.text = "—"
            drop.text = "—"
            elapsed.text = "—"
            autonomy.text = "—"
            range.text = "Nenhuma descarga em andamento"
            return
        }
        status.text = if (active.dropPercent == 0) {
            "Medição ativa. A taxa aparece após a bateria cair pelo menos 1%."
        } else {
            "Medição ativa • média desde o início desta descarga"
        }
        rate.text = DischargeFormatter.rate(active.ratePercentPerHour)
        drop.text = "${active.dropPercent}%"
        elapsed.text = DischargeFormatter.duration(active.durationMs)
        autonomy.text = DischargeFormatter.remaining(active.estimatedRemainingMs)
        range.text = "Bateria ${active.startPercent}% → ${active.currentPercent}%"
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
