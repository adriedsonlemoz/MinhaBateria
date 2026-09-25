package com.minhabateria.app.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.monitoring.MonitorPreferences
import com.minhabateria.app.monitoring.MonitoringServiceController
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.source.EnergySourceProfile
import com.minhabateria.app.source.SourceProfileActivity
import com.minhabateria.app.source.SourceProfileStore
import com.minhabateria.app.ui.MonitoringControls
import com.minhabateria.app.ui.SystemBars
import java.util.Locale

class SettingsActivity : Activity() {
    private lateinit var controls: MonitoringControls
    private lateinit var preferences: MonitorPreferences
    private lateinit var sourceProfileStore: SourceProfileStore
    private lateinit var sourceProfileSummary: TextView
    private var pendingServiceStart = false

    private val stateListener: (MonitoringState) -> Unit = { state ->
        runOnUiThread { controls.render(preferences.isMonitoringRequested(), state.running) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        SystemBars.apply(this, findViewById(R.id.settingsRoot))

        preferences = MonitorPreferences(this)
        sourceProfileStore = SourceProfileStore(this)
        controls = MonitoringControls(this)
        sourceProfileSummary = findViewById(R.id.sourceProfileSummaryValue)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
        findViewById<TextView>(R.id.versionValue).text = AppVersionInfo.fullVersion(this)
        findViewById<Button>(R.id.editSourceProfileButton).setOnClickListener {
            startActivity(Intent(this, SourceProfileActivity::class.java))
        }

        controls.setResumeAfterBoot(preferences.shouldResumeAfterBoot())
        controls.setActions(
            onStart = ::requestContinuousMonitoring,
            onStop = { MonitoringServiceController.stop(this) },
            onResumeAfterBootChanged = preferences::setResumeAfterBoot
        )
        controls.render(preferences.isMonitoringRequested(), MonitoringStateStore.current().running)
        DonationHelper.bind(this)
    }

    override fun onStart() {
        super.onStart()
        MonitoringStateStore.addListener(stateListener)
    }

    override fun onResume() {
        super.onResume()
        renderSourceProfile(sourceProfileStore.getProfile())
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

    private fun renderSourceProfile(profile: EnergySourceProfile?) {
        sourceProfileSummary.text = profile?.let {
            val power = it.nominalPowerW?.let(::formatPower).orEmpty()
            "${it.name} • ${it.type.label}$power"
        } ?: "Nenhum perfil configurado"
    }

    private fun formatPower(watts: Double): String {
        val text = String.format(Locale.getDefault(), "%.1f", watts)
            .removeSuffix(",0")
            .removeSuffix(".0")
        return " • $text W"
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
