package com.minhabateria.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.minhabateria.app.history.HistoryEntry
import com.minhabateria.app.history.HistoryFormatter
import com.minhabateria.app.history.HistoryStore
import com.minhabateria.app.monitoring.LocalBatteryMonitorHub
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.ui.AppActionDialog
import com.minhabateria.app.ui.BottomTab
import com.minhabateria.app.ui.BottomTabsBinder
import com.minhabateria.app.ui.HistoryScreenRenderer
import com.minhabateria.app.ui.HistorySelectionMode
import com.minhabateria.app.ui.SystemBars

class HistoryActivity : Activity() {
    private lateinit var renderer: HistoryScreenRenderer
    private lateinit var store: HistoryStore
    private lateinit var compareButton: Button
    private lateinit var selectionText: TextView
    private lateinit var selectionCount: TextView
    private lateinit var comparisonPanel: View
    private lateinit var managementPanel: View
    private lateinit var manageButton: Button
    private lateinit var manageCount: TextView
    private lateinit var deleteSelectedButton: Button
    private lateinit var deleteAllButton: Button
    private val compareSelectedIds = linkedSetOf<String>()
    private val manageSelectedIds = linkedSetOf<String>()
    private var mode = HistorySelectionMode.COMPARE
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
        bindViews()
        restoreSelectionState(savedInstanceState)
        bindActions()
        BottomTabsBinder(this).bind(
            active = BottomTab.HISTORY,
            openNow = { open(MainActivity::class.java) },
            openCharts = { open(ChartsActivity::class.java) },
            openSession = { open(SessionActivity::class.java) },
            openDischarge = { open(DischargeRateActivity::class.java) },
            openHistory = {}
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_MODE, mode.name)
        outState.putStringArrayList(STATE_COMPARE, ArrayList(compareSelectedIds))
        outState.putStringArrayList(STATE_MANAGE, ArrayList(manageSelectedIds))
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

    private fun bindViews() {
        compareButton = findViewById(R.id.compareSessionsButton)
        selectionText = findViewById(R.id.historySelectionText)
        selectionCount = findViewById(R.id.historySelectionCount)
        comparisonPanel = findViewById(R.id.historyComparisonPanel)
        managementPanel = findViewById(R.id.historyManagementPanel)
        manageButton = findViewById(R.id.historyManageButton)
        manageCount = findViewById(R.id.manageSelectionCount)
        deleteSelectedButton = findViewById(R.id.deleteSelectedSessionsButton)
        deleteAllButton = findViewById(R.id.deleteAllHistoryButton)
    }

    private fun bindActions() {
        compareButton.setOnClickListener { openComparison() }
        manageButton.setOnClickListener { setMode(if (mode == HistorySelectionMode.COMPARE) HistorySelectionMode.MANAGE else HistorySelectionMode.COMPARE) }
        findViewById<Button>(R.id.selectAllSessionsButton).setOnClickListener {
            manageSelectedIds.clear()
            manageSelectedIds.addAll(store.entries().map { it.id })
            refresh()
        }
        findViewById<Button>(R.id.clearSessionSelectionButton).setOnClickListener {
            manageSelectedIds.clear()
            refresh()
        }
        deleteSelectedButton.setOnClickListener { confirmDeleteSelected() }
        deleteAllButton.setOnClickListener { confirmDeleteAll() }
    }

    private fun restoreSelectionState(state: Bundle?) {
        mode = state?.getString(STATE_MODE)?.let { runCatching { HistorySelectionMode.valueOf(it) }.getOrNull() }
            ?: HistorySelectionMode.COMPARE
        state?.getStringArrayList(STATE_COMPARE)?.let(compareSelectedIds::addAll)
        state?.getStringArrayList(STATE_MANAGE)?.let(manageSelectedIds::addAll)
    }

    private fun refresh() {
        val entries = store.entries()
        val valid = entries.mapTo(mutableSetOf()) { it.id }
        compareSelectedIds.retainAll(valid)
        manageSelectedIds.retainAll(valid)
        renderer.render(
            entries = entries,
            mode = mode,
            compareSelectedIds = compareSelectedIds,
            manageSelectedIds = manageSelectedIds,
            onCompareToggle = ::toggleComparison,
            onManageToggle = ::toggleManagement,
            onLongPress = ::enterManagementFor,
            onDelete = ::confirmDelete
        )
        updateControls(entries.size)
    }

    private fun toggleComparison(entry: HistoryEntry) {
        if (!compareSelectedIds.remove(entry.id)) {
            if (compareSelectedIds.size >= 2) {
                Toast.makeText(this, "Desmarque uma sessão antes de selecionar outra.", Toast.LENGTH_SHORT).show()
                return
            }
            compareSelectedIds.add(entry.id)
        }
        refresh()
    }

    private fun toggleManagement(entry: HistoryEntry) {
        if (!manageSelectedIds.remove(entry.id)) manageSelectedIds.add(entry.id)
        refresh()
    }

    private fun enterManagementFor(entry: HistoryEntry) {
        mode = HistorySelectionMode.MANAGE
        manageSelectedIds.add(entry.id)
        refresh()
    }

    private fun setMode(newMode: HistorySelectionMode) {
        mode = newMode
        refresh()
    }

    private fun confirmDelete(entry: HistoryEntry) {
        AppActionDialog.show(
            activity = this,
            title = "Excluir sessão?",
            details = "${HistoryFormatter.title(entry)}\n${HistoryFormatter.source(entry)}\n${HistoryFormatter.dateTime(entry.endedAtMs)}",
            message = "Esta sessão será removida permanentemente do histórico.",
            confirmText = "Excluir",
            destructive = true
        ) { deleteIds(setOf(entry.id), "Sessão excluída.") }
    }

    private fun confirmDeleteSelected() {
        val count = manageSelectedIds.size
        if (count == 0) return
        AppActionDialog.show(
            activity = this,
            title = "Excluir sessões?",
            details = "$count ${if (count == 1) "sessão selecionada" else "sessões selecionadas"}",
            message = "$count ${if (count == 1) "sessão será removida" else "sessões serão removidas"} permanentemente do histórico.\n\nEsta ação não pode ser desfeita.",
            confirmText = "Excluir $count ${if (count == 1) "sessão" else "sessões"}",
            destructive = true
        ) { deleteIds(manageSelectedIds.toSet(), "$count ${if (count == 1) "sessão excluída" else "sessões excluídas"}.") }
    }

    private fun confirmDeleteAll() {
        val count = store.entries().size
        if (count == 0) return
        AppActionDialog.show(
            activity = this,
            title = "Excluir todo o histórico?",
            details = "$count ${if (count == 1) "sessão salva" else "sessões salvas"}",
            message = "As sessões salvas, medições e comparações relacionadas serão removidas permanentemente. A sessão atualmente em andamento não será afetada.\n\nEsta ação não pode ser desfeita.",
            confirmText = "Excluir $count ${if (count == 1) "sessão" else "sessões"}",
            destructive = true
        ) {
            val removed = store.clear()
            if (removed > 0) {
                compareSelectedIds.clear()
                manageSelectedIds.clear()
                refresh()
                Toast.makeText(this, "Histórico excluído.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Não foi possível excluir o histórico.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteIds(ids: Set<String>, successMessage: String) {
        val removed = store.deleteMany(ids)
        if (removed > 0) {
            compareSelectedIds.removeAll(ids)
            manageSelectedIds.removeAll(ids)
            refresh()
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Não foi possível excluir as sessões.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateControls(entryCount: Int) {
        val comparing = mode == HistorySelectionMode.COMPARE
        comparisonPanel.visibility = if (comparing) View.VISIBLE else View.GONE
        managementPanel.visibility = if (comparing) View.GONE else View.VISIBLE
        manageButton.text = if (comparing) "Gerenciar" else "Comparar"
        manageButton.setTextColor(getColor(if (comparing) R.color.accent_blue else R.color.accent_green))

        selectionText.text = if (compareSelectedIds.size == 2) "Pronto para comparar" else "Selecione 2 sessões"
        selectionCount.text = "${compareSelectedIds.size}/2"
        compareButton.isEnabled = compareSelectedIds.size == 2
        compareButton.alpha = if (compareButton.isEnabled) 1f else 0.45f

        val selected = manageSelectedIds.size
        manageCount.text = "Selecionadas: $selected"
        deleteSelectedButton.text = "Excluir ($selected)"
        deleteSelectedButton.isEnabled = selected > 0
        deleteSelectedButton.alpha = if (selected > 0) 1f else 0.45f
        deleteAllButton.isEnabled = entryCount > 0
        deleteAllButton.alpha = if (entryCount > 0) 1f else 0.45f
    }

    private fun openComparison() {
        if (compareSelectedIds.size != 2) return
        val ids = compareSelectedIds.toList()
        startActivity(Intent(this, HistoryComparisonActivity::class.java).apply {
            putExtra(HistoryComparisonActivity.EXTRA_FIRST_ID, ids[0])
            putExtra(HistoryComparisonActivity.EXTRA_SECOND_ID, ids[1])
        })
    }

    private fun open(target: Class<out Activity>) {
        startActivity(Intent(this, target))
        finish()
    }

    private companion object {
        const val STATE_MODE = "history_mode"
        const val STATE_COMPARE = "history_compare_selection"
        const val STATE_MANAGE = "history_manage_selection"
    }
}
