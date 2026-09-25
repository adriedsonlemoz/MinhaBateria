package com.minhabateria.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
    private var lastPlugged: Boolean? = null

    private val stateListener: (MonitoringState) -> Unit = {
        runOnUiThread {
            val plugged = it.info?.isPlugged
            if (lastPlugged == true && plugged == false) renderer.render(store.entries())
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

        BottomTabsBinder(this).bind(
            active = BottomTab.HISTORY,
            openNow = {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            },
            openCharts = {
                startActivity(Intent(this, ChartsActivity::class.java))
                finish()
            },
            openSession = {
                startActivity(Intent(this, SessionActivity::class.java))
                finish()
            },
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
        renderer.render(store.entries())
    }

    override fun onStop() {
        LocalBatteryMonitorHub.detach(this)
        MonitoringStateStore.removeListener(stateListener)
        super.onStop()
    }
}
