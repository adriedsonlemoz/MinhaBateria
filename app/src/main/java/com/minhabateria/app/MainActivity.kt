package com.minhabateria.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.minhabateria.app.battery.BatteryMonitor
import com.minhabateria.app.battery.BatteryStatusReader
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.monitoring.MonitorPreferences
import com.minhabateria.app.monitoring.MonitoringServiceController
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.settings.SettingsActivity
import com.minhabateria.app.source.SourceProfileActivity
import com.minhabateria.app.source.SourceProfileStore
import com.minhabateria.app.ui.MainScreenRenderer
import com.minhabateria.app.ui.SystemBars

class MainActivity : Activity() {
    private lateinit var localMonitor: BatteryMonitor
    private lateinit var renderer: MainScreenRenderer
    private lateinit var preferences: MonitorPreferences
    private lateinit var sourceProfileStore: SourceProfileStore
    private lateinit var monitoringStatus: TextView
    private var activityStarted = false

    private val stateListener: (MonitoringState) -> Unit = { state ->
        runOnUiThread {
            state.info?.let { info ->
                state.session?.let { session -> renderer.render(info, session) }
            }
            renderMonitoringStatus(state.running)
            syncLocalMonitor()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SystemBars.apply(this, findViewById(R.id.mainRoot))

        preferences = MonitorPreferences(this)
        sourceProfileStore = SourceProfileStore(this)
        renderer = MainScreenRenderer(this)
        monitoringStatus = findViewById(R.id.monitoringStateValue)
        localMonitor = BatteryMonitor(
            reader = BatteryStatusReader(this),
            session = ChargingSession(),
            onUpdate = renderer::render
        )

        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        renderer.renderSourceProfile(sourceProfileStore.getProfile())
        renderMonitoringStatus(MonitoringStateStore.current().running)
        MonitoringServiceController.restoreIfRequested(this)
        offerInitialSourceProfileIfNeeded()
    }

    override fun onStart() {
        super.onStart()
        activityStarted = true
        MonitoringStateStore.addListener(stateListener)
        syncLocalMonitor()
    }

    override fun onResume() {
        super.onResume()
        renderer.renderSourceProfile(sourceProfileStore.getProfile())
    }

    override fun onStop() {
        activityStarted = false
        localMonitor.stop()
        MonitoringStateStore.removeListener(stateListener)
        super.onStop()
    }

    private fun offerInitialSourceProfileIfNeeded() {
        if (!sourceProfileStore.shouldOfferInitialSetup()) return
        startActivity(
            Intent(this, SourceProfileActivity::class.java)
                .putExtra(SourceProfileActivity.EXTRA_INITIAL_SETUP, true)
        )
    }

    private fun syncLocalMonitor() {
        if (!activityStarted) return
        if (preferences.isMonitoringRequested()) localMonitor.stop() else localMonitor.start()
    }

    private fun renderMonitoringStatus(running: Boolean) {
        monitoringStatus.text = when {
            running -> "● Monitoramento ativo"
            preferences.isMonitoringRequested() -> "● Iniciando monitoramento…"
            else -> "● Monitoramento inativo"
        }
        monitoringStatus.setTextColor(getColor(if (running) R.color.accent_green else R.color.accent_blue))
    }
}
