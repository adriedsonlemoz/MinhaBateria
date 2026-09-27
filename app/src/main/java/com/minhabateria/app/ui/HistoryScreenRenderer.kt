package com.minhabateria.app.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.history.HistoryEntry
import com.minhabateria.app.history.HistoryFormatter
import com.minhabateria.app.session.SessionFormatter

class HistoryScreenRenderer(private val activity: Activity) {
    private val list = activity.findViewById<LinearLayout>(R.id.historyList)
    private val empty = activity.findViewById<TextView>(R.id.historyEmpty)
    private val count = activity.findViewById<TextView>(R.id.historyCount)
    private val inflater = LayoutInflater.from(activity)

    fun render(
        entries: List<HistoryEntry>,
        selectedIds: Set<String> = emptySet(),
        onToggleSelection: ((HistoryEntry) -> Unit)? = null
    ) {
        list.removeAllViews()
        count.text = if (entries.size == 1) "1 sessão salva" else "${entries.size} sessões salvas"
        empty.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        entries.forEach { entry ->
            list.addView(createItem(entry, entry.id in selectedIds, onToggleSelection))
        }
    }

    private fun createItem(
        entry: HistoryEntry,
        selected: Boolean,
        onToggleSelection: ((HistoryEntry) -> Unit)?
    ): View {
        val view = inflater.inflate(R.layout.history_item, list, false)
        text(view, R.id.historyItemTitle).text = HistoryFormatter.title(entry)
        text(view, R.id.historyItemDate).text = HistoryFormatter.dateTime(entry.endedAtMs)
        text(view, R.id.historyItemSource).text = HistoryFormatter.source(entry)
        text(view, R.id.historyItemDuration).text = SessionFormatter.duration(entry.elapsedMs)
        text(view, R.id.historyItemEnergy).text = SessionFormatter.energy(entry.energyWh)
        text(view, R.id.historyItemCharge).text = SessionFormatter.charge(entry.chargeMah)
        text(view, R.id.historyItemAverage).text = SessionFormatter.power(entry.averagePowerW)
        text(view, R.id.historyItemPeak).text = SessionFormatter.power(entry.maxPowerW)
        text(view, R.id.historyItemBatteryRange).text = SessionFormatter.batteryRange(entry.startPercent, entry.endPercent)

        val gain = text(view, R.id.historyItemGain)
        gain.text = SessionFormatter.gain(entry.gainPercent)
        gain.setTextColor(
            activity.getColor(
                when {
                    entry.gainPercent == null -> R.color.text_muted
                    entry.gainPercent > 0 -> R.color.accent_green
                    entry.gainPercent < 0 -> R.color.accent_red
                    else -> R.color.text_secondary
                }
            )
        )

        text(view, R.id.historyItemMaxTemperature).text = SessionFormatter.temperature(entry.maxTemperatureC)
        text(view, R.id.historyItemInterruptions).text = interruptionLabel(entry.interruptions)
        renderStability(view, entry)

        val selector = text(view, R.id.historyItemSelector)
        val action = text(view, R.id.historyItemSelect)
        selector.text = if (selected) "✓" else "○"
        selector.setTextColor(activity.getColor(if (selected) R.color.accent_green else R.color.accent_blue))
        action.text = if (selected) "Selecionada ✓" else "Selecionar para comparar  ›"
        action.setTextColor(activity.getColor(if (selected) R.color.accent_green else R.color.accent_blue))
        view.setBackgroundResource(if (selected) R.drawable.bg_card_selected else R.drawable.bg_card)

        if (onToggleSelection != null) {
            view.isClickable = true
            view.isFocusable = true
            view.setOnClickListener { onToggleSelection(entry) }
        } else {
            selector.visibility = View.GONE
            action.visibility = View.GONE
        }
        return view
    }

    private fun renderStability(view: View, entry: HistoryEntry) {
        val stability = HistoryFormatter.stability(entry.powerVariationRatio)
        val chip = text(view, R.id.historyItemStability)
        chip.text = stability
        when (stability) {
            "Muito estável" -> {
                chip.setBackgroundResource(R.drawable.bg_chip_green)
                chip.setTextColor(activity.getColor(R.color.accent_green))
            }
            "Estável", "Oscilando" -> {
                chip.setBackgroundResource(R.drawable.bg_chip_blue)
                chip.setTextColor(activity.getColor(R.color.accent_blue))
            }
            "Muito variável" -> {
                chip.setBackgroundResource(R.drawable.bg_chip_orange)
                chip.setTextColor(activity.getColor(R.color.accent_orange))
            }
            else -> {
                chip.setBackgroundResource(R.drawable.bg_status_chip)
                chip.setTextColor(activity.getColor(R.color.text_secondary))
            }
        }
    }

    private fun interruptionLabel(count: Int): String =
        if (count == 1) "1 interrupção" else "$count interrupções"

    private fun text(view: View, id: Int): TextView = view.findViewById(id)
}
