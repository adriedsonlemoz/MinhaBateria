package com.minhabateria.app.diagnostics

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        findViewById<Button>(R.id.exportDiagnosticsButton).setOnClickListener { exportReport() }
        findViewById<Button>(R.id.clearCrashReportsButton).setOnClickListener { confirmClearCrashes() }
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

    @Deprecated("Deprecated in Android API, mantido para compatibilidade sem AndroidX")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_EXPORT || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        val success = runCatching {
            contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8).use { writer ->
                requireNotNull(writer) { "Não foi possível abrir o destino" }
                writer.write(latestReport)
            }
        }.isSuccess
        Toast.makeText(
            this,
            if (success) "Relatório exportado." else "Não foi possível exportar o relatório.",
            Toast.LENGTH_SHORT
        ).show()
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

    private fun exportReport() {
        if (latestReport.isBlank()) render(force = true)
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "Minha-Bateria-diagnostico-$stamp.txt")
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_EXPORT)
    }

    private fun confirmClearCrashes() {
        val count = CrashStore.count(this)
        if (count == 0) {
            Toast.makeText(this, "Não há falhas capturadas para apagar.", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Apagar falhas capturadas?")
            .setMessage("Serão removidos $count relatório(s) de falha salvos neste aparelho.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Apagar") { _, _ ->
                CrashStore.clear(this)
                render(force = true)
                Toast.makeText(this, "Relatórios de falha apagados.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private companion object {
        const val REPORT_REFRESH_MS = 2_000L
        const val REQUEST_EXPORT = 3101
    }
}
