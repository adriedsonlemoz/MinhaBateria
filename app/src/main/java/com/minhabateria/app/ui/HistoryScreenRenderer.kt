package com.minhabateria.app.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.history.HistoryEntry
import com.minhabateria.app.history.HistoryFormatter

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
        view.findViewById<TextView>(R.id.historyItemTitle).text = HistoryFormatter.title(entry)
        view.findViewById<TextView>(R.id.historyItemDate).text = HistoryFormatter.dateTime(entry.endedAtMs)
        view.findViewById<TextView>(R.id.historyItemSource).text = HistoryFormatter.source(entry)
        view.findViewById<TextView>(R.id.historyItemPrimary).text = HistoryFormatter.primary(entry)
        view.findViewById<TextView>(R.id.historyItemPower).text = HistoryFormatter.power(entry)
        view.findViewById<TextView>(R.id.historyItemBattery).text = HistoryFormatter.battery(entry)
        view.findViewById<TextView>(R.id.historyItemTemperature).text = HistoryFormatter.temperature(entry)

        val selector = view.findViewById<TextView>(R.id.historyItemSelect)
        selector.text = if (selected) "Selecionada ✓" else "Selecionar para comparar"
        selector.setTextColor(activity.getColor(if (selected) R.color.accent_green else R.color.accent_blue))
        view.setBackgroundResource(if (selected) R.drawable.bg_card_selected else R.drawable.bg_card)

        if (onToggleSelection != null) {
            view.isClickable = true
            view.isFocusable = true
            view.setOnClickListener { onToggleSelection(entry) }
        } else {
            selector.visibility = View.GONE
        }
        return view
    }
}
