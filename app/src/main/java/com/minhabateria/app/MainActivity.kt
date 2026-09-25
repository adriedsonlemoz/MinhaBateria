package com.minhabateria.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.minhabateria.app.battery.BatteryMonitor
import com.minhabateria.app.battery.BatteryStatusReader
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.monitoring.MonitorPreferences
import com.minhabateria.app.monitoring.MonitoringServiceController
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.ui.MainScreenRenderer
import com.minhabateria.app.ui.MonitoringControls

class MainActivity : Activity() {
    private lateinit var localMonitor: BatteryMonitor
    private lateinit var renderer: MainScreenRenderer
    private lateinit var controls: MonitoringControls
    private lateinit var preferences: MonitorPreferences
    private var activityStarted = false
    private var pendingServiceStart = false

    private val stateListener: (MonitoringState) -> Unit = { state ->
        runOnUiThread {
            state.info?.let { info ->
                state.session?.let { session -> renderer.render(info, session) }
            }
            renderControls(state.running)
            syncLocalMonitor()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = MonitorPreferences(this)
        renderer = MainScreenRenderer(this)
        controls = MonitoringControls(this)
        localMonitor = BatteryMonitor(
            reader = BatteryStatusReader(this),
            session = ChargingSession(),
            onUpdate = renderer::render
        )

        controls.setResumeAfterBoot(preferences.shouldResumeAfterBoot())
        controls.setActions(
            onStart = ::requestContinuousMonitoring,
            onStop = ::stopContinuousMonitoring,
            onResumeAfterBootChanged = preferences::setResumeAfterBoot
        )
        renderControls(MonitoringStateStore.current().running)
        MonitoringServiceController.restoreIfRequested(this)
    }

    override fun onStart() {
        super.onStart()
        activityStarted = true
        MonitoringStateStore.addListener(stateListener)
        syncLocalMonitor()
    }

    override fun onStop() {
        activityStarted = false
        localMonitor.stop()
        MonitoringStateStore.removeListener(stateListener)
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATIONS && pendingServiceStart) {
            pendingServiceStart = false
            startContinuousMonitoring()
        }
    }

    private fun requestContinuousMonitoring() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingServiceStart = true
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
            return
        }
        startContinuousMonitoring()
    }

    private fun startContinuousMonitoring() {
        localMonitor.stop()
        MonitoringServiceController.start(this)
        renderControls(running = MonitoringStateStore.current().running)
    }

    private fun stopContinuousMonitoring() {
        MonitoringServiceController.stop(this)
        renderControls(running = false)
        syncLocalMonitor()
    }

    private fun syncLocalMonitor() {
        if (!activityStarted) return
        if (preferences.isMonitoringRequested()) localMonitor.stop() else localMonitor.start()
    }

    private fun renderControls(running: Boolean) {
        controls.render(preferences.isMonitoringRequested(), running)
    }

    private companion object {
        const val REQUEST_NOTIFICATIONS = 2001
    }
}
