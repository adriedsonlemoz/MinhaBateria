package com.minhabateria.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.minhabateria.app.discharge.DischargeRecorder
import com.minhabateria.app.discharge.DischargeStore
import com.minhabateria.app.monitoring.LocalBatteryMonitorHub
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.ui.BottomTab
import com.minhabateria.app.ui.BottomTabsBinder
import com.minhabateria.app.ui.DischargeScreenRenderer
import com.minhabateria.app.ui.SystemBars

class DischargeRateActivity : Activity() {
    private lateinit var store: DischargeStore
    private lateinit var renderer: DischargeScreenRenderer
    private val stateListener: (MonitoringState) -> Unit = {
        runOnUiThread {
            render()
            LocalBatteryMonitorHub.sync(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discharge_rate)
        SystemBars.apply(this, findViewById(R.id.dischargeRoot))
        store = DischargeStore(this)
        renderer = DischargeScreenRenderer(this)

        findViewById<Button>(R.id.resetDischargeButton).setOnClickListener {
            DischargeRecorder(this).reset(MonitoringStateStore.current().info)
            render()
        }
        findViewById<Button>(R.id.clearDischargeHistoryButton).setOnClickListener { confirmClearHistory() }
        bindBottomTabs()
    }

    override fun onStart() {
        super.onStart()
        MonitoringStateStore.addListener(stateListener)
        LocalBatteryMonitorHub.attach(this, this)
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    override fun onStop() {
        LocalBatteryMonitorHub.detach(this)
        MonitoringStateStore.removeListener(stateListener)
        super.onStop()
    }

    private fun bindBottomTabs() {
        BottomTabsBinder(this).bind(
            active = BottomTab.DISCHARGE,
            openNow = { open(MainActivity::class.java) },
            openCharts = { open(ChartsActivity::class.java) },
            openSession = { open(SessionActivity::class.java) },
            openDischarge = {},
            openHistory = { open(HistoryActivity::class.java) }
        )
    }

    private fun open(target: Class<out Activity>) {
        startActivity(Intent(this, target))
        finish()
    }

    private fun render() {
        val state = MonitoringStateStore.current()
        val stored = store.active()
        val active = if (stored != null && state.info?.isPlugged == false) {
            stored.copy(
                lastObservedAtMs = System.currentTimeMillis(),
                currentPercent = state.info?.percent ?: stored.currentPercent
            )
        } else {
            stored
        }
        renderer.render(
            active = active,
            entries = store.entries(),
            monitoring = state.running || state.info != null
        )
    }

    private fun confirmClearHistory() {
        AlertDialog.Builder(this)
            .setTitle("Limpar histórico de descarga?")
            .setMessage("As medições concluídas serão apagadas. A medição atual não será interrompida.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Limpar") { _, _ ->
                store.clearHistory()
                render()
            }
            .show()
    }
}
