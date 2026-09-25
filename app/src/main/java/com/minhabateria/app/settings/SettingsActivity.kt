package com.minhabateria.app.settings

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.monitoring.MonitorPreferences
import com.minhabateria.app.monitoring.MonitoringServiceController
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.ui.MonitoringControls

class SettingsActivity : Activity() {
    private lateinit var controls: MonitoringControls
    private lateinit var preferences: MonitorPreferences
    private var pendingServiceStart = false

    private val stateListener: (MonitoringState) -> Unit = { state ->
        runOnUiThread {
            controls.render(preferences.isMonitoringRequested(), state.running)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferences = MonitorPreferences(this)
        controls = MonitoringControls(this)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
        findViewById<TextView>(R.id.versionValue).text = AppVersionInfo.fullVersion(this)

        controls.setResumeAfterBoot(preferences.shouldResumeAfterBoot())
        controls.setActions(
            onStart = ::requestContinuousMonitoring,
            onStop = { MonitoringServiceController.stop(this) },
            onResumeAfterBootChanged = preferences::setResumeAfterBoot
        )
        controls.render(
            preferences.isMonitoringRequested(),
            MonitoringStateStore.current().running
        )
        DonationHelper.bind(this)
    }

    override fun onStart() {
        super.onStart()
        MonitoringStateStore.addListener(stateListener)
    }

    override fun onStop() {
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
            MonitoringServiceController.start(this)
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
        MonitoringServiceController.start(this)
    }

    private companion object {
        const val REQUEST_NOTIFICATIONS = 2001
    }
}
