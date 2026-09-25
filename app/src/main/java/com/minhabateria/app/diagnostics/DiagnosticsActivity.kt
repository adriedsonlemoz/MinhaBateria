package com.minhabateria.app.diagnostics

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.minhabateria.app.R
import com.minhabateria.app.monitoring.LocalBatteryMonitorHub
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.ui.SystemBars

class DiagnosticsActivity : Activity() {
    private lateinit var reportView: TextView
    private var latestState = MonitoringState()
    private var latestReport = ""
    private var lastRenderElapsedMs = 0L

    private val stateListener: (MonitoringState) -> Unit = { state ->
        latestState = state
        runOnUiThread { render(force = false) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics)
        SystemBars.apply(this, findViewById(R.id.diagnosticsRoot))
        reportView = findViewById(R.id.diagnosticReportValue)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
        findViewById<Button>(R.id.refreshDiagnosticsButton).setOnClickListener { render(force = true) }
        findViewById<Button>(R.id.copyDiagnosticsButton).setOnClickListener { copyReport() }
    }

    override fun onStart() {
        super.onStart()
        MonitoringStateStore.addListener(stateListener)
        LocalBatteryMonitorHub.attach(this, this)
    }

    override fun onResume() {
        super.onResume()
        latestState = MonitoringStateStore.current()
        render(force = true)
    }

    override fun onStop() {
        LocalBatteryMonitorHub.detach(this)
        MonitoringStateStore.removeListener(stateListener)
        super.onStop()
    }

    private fun render(force: Boolean) {
        val now = SystemClock.elapsedRealtime()
        if (!force && now - lastRenderElapsedMs < REPORT_REFRESH_MS) return
        lastRenderElapsedMs = now
        latestReport = DiagnosticReportBuilder.build(this, latestState)
        reportView.text = latestReport
    }

    private fun copyReport() {
        if (latestReport.isBlank()) render(force = true)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Diagnóstico Minha Bateria", latestReport))
        Toast.makeText(this, "Diagnóstico copiado.", Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val REPORT_REFRESH_MS = 2_000L
    }
}
