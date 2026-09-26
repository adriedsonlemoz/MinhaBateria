package com.minhabateria.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.minhabateria.app.monitoring.LocalBatteryMonitorHub
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.source.SourceProfileStore
import com.minhabateria.app.ui.BottomTab
import com.minhabateria.app.ui.BottomTabsBinder
import com.minhabateria.app.ui.MeasurementHelpDialog
import com.minhabateria.app.ui.SessionScreenRenderer
import com.minhabateria.app.ui.SystemBars

class SessionActivity : Activity() {
    private lateinit var renderer: SessionScreenRenderer
    private lateinit var sourceStore: SourceProfileStore
    private var detailsVisible = false

    private val stateListener: (MonitoringState) -> Unit = { state ->
        runOnUiThread {
            renderer.render(state.info, state.session)
            LocalBatteryMonitorHub.sync(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        SystemBars.apply(this, findViewById(R.id.sessionRoot))
        renderer = SessionScreenRenderer(this)
        sourceStore = SourceProfileStore(this)

        BottomTabsBinder(this).bind(
            active = BottomTab.SESSION,
            openNow = { finish() },
            openCharts = {
                startActivity(android.content.Intent(this, ChartsActivity::class.java))
                finish()
            },
            openSession = {},
            openDischarge = {
                startActivity(android.content.Intent(this, DischargeRateActivity::class.java))
                finish()
            },
            openHistory = {
                startActivity(android.content.Intent(this, HistoryActivity::class.java))
                finish()
            }
        )
        bindDetailsControls()
        renderer.renderSource(sourceStore.getProfile())
        MonitoringStateStore.current().let { renderer.render(it.info, it.session) }
    }

    private fun bindDetailsControls() {
        val details = findViewById<View>(R.id.technicalDetailsContainer)
        val toggle = findViewById<TextView>(R.id.sessionDetailsToggle)
        toggle.setOnClickListener {
            detailsVisible = !detailsVisible
            details.visibility = if (detailsVisible) View.VISIBLE else View.GONE
            toggle.text = if (detailsVisible) "Ocultar detalhes técnicos" else "Ver detalhes técnicos"
        }
        findViewById<TextView>(R.id.sessionHelpLink).setOnClickListener {
            MeasurementHelpDialog.show(this)
        }
    }

    override fun onStart() {
        super.onStart()
        MonitoringStateStore.addListener(stateListener)
        LocalBatteryMonitorHub.attach(this, this)
    }

    override fun onResume() {
        super.onResume()
        renderer.renderSource(sourceStore.getProfile())
        MonitoringStateStore.current().let { renderer.render(it.info, it.session) }
    }

    override fun onStop() {
        LocalBatteryMonitorHub.detach(this)
        MonitoringStateStore.removeListener(stateListener)
        super.onStop()
    }
}
