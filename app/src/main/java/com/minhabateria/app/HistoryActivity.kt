package com.minhabateria.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.minhabateria.app.history.HistoryEntry
import com.minhabateria.app.history.HistoryStore
import com.minhabateria.app.monitoring.LocalBatteryMonitorHub
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.ui.BottomTab
import com.minhabateria.app.ui.BottomTabsBinder
import com.minhabateria.app.ui.HistoryScreenRenderer
import com.minhabateria.app.ui.SystemBars

class HistoryActivity : Activity() {
    private lateinit var renderer: HistoryScreenRenderer
    private lateinit var store: HistoryStore
    private lateinit var compareButton: Button
    private lateinit var selectionText: TextView
    private val selectedIds = linkedSetOf<String>()
    private var lastPlugged: Boolean? = null

    private val stateListener: (MonitoringState) -> Unit = {
        runOnUiThread {
            val plugged = it.info?.isPlugged
            if (lastPlugged == true && plugged == false) refresh()
            lastPlugged = plugged
            LocalBatteryMonitorHub.sync(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        SystemBars.apply(this, findViewById(R.id.historyRoot))
        renderer = HistoryScreenRenderer(this)
        store = HistoryStore(this)
        compareButton = findViewById(R.id.compareSessionsButton)
        selectionText = findViewById(R.id.historySelectionText)
        compareButton.setOnClickListener { openComparison() }

        BottomTabsBinder(this).bind(
            active = BottomTab.HISTORY,
            openNow = { open(MainActivity::class.java) },
            openCharts = { open(ChartsActivity::class.java) },
            openSession = { open(SessionActivity::class.java) },
            openHistory = {}
        )
    }

    override fun onStart() {
        super.onStart()
        MonitoringStateStore.addListener(stateListener)
        LocalBatteryMonitorHub.attach(this, this)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onStop() {
        LocalBatteryMonitorHub.detach(this)
        MonitoringStateStore.removeListener(stateListener)
        super.onStop()
    }

    private fun refresh() {
        val entries = store.entries()
        selectedIds.retainAll(entries.map { it.id }.toSet())
        renderer.render(entries, selectedIds, ::toggleSelection)
        updateSelectionControls()
    }

    private fun toggleSelection(entry: HistoryEntry) {
        if (!selectedIds.remove(entry.id)) {
            if (selectedIds.size >= 2) {
                Toast.makeText(this, "Desmarque uma sessão antes de selecionar outra.", Toast.LENGTH_SHORT).show()
                return
            }
            selectedIds.add(entry.id)
        }
        refresh()
    }

    private fun updateSelectionControls() {
        selectionText.text = "Selecione 2 sessões • ${selectedIds.size}/2"
        compareButton.isEnabled = selectedIds.size == 2
        compareButton.alpha = if (compareButton.isEnabled) 1f else 0.45f
    }

    private fun openComparison() {
        if (selectedIds.size != 2) return
        val ids = selectedIds.toList()
        startActivity(Intent(this, HistoryComparisonActivity::class.java).apply {
            putExtra(HistoryComparisonActivity.EXTRA_FIRST_ID, ids[0])
            putExtra(HistoryComparisonActivity.EXTRA_SECOND_ID, ids[1])
        })
    }

    private fun open(target: Class<out Activity>) {
        startActivity(Intent(this, target))
        finish()
    }
}
