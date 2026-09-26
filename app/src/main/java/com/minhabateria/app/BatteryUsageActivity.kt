package com.minhabateria.app

import android.app.Activity
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.minhabateria.app.battery.BatteryStatusReader
import com.minhabateria.app.discharge.DischargeStore
import com.minhabateria.app.monitoring.LocalBatteryMonitorHub
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.ui.SystemBars
import com.minhabateria.app.usage.BatteryUsageFormatter
import com.minhabateria.app.usage.BatteryUsageRepository
import kotlin.math.abs

class BatteryUsageActivity : Activity() {
    private lateinit var repository: BatteryUsageRepository
    private lateinit var appsList: LinearLayout
    private lateinit var accessCard: View
    private lateinit var emptyText: TextView
    private var appsLoadGeneration = 0

    private val stateListener: (MonitoringState) -> Unit = {
        runOnUiThread { renderSummary() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery_usage)
        SystemBars.apply(this, findViewById(R.id.batteryUsageRoot))
        repository = BatteryUsageRepository(this)
        appsList = findViewById(R.id.batteryUsageAppsList)
        accessCard = findViewById(R.id.usageAccessCard)
        emptyText = findViewById(R.id.usageEmptyText)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
        findViewById<Button>(R.id.usageAccessButton).setOnClickListener { repository.openUsageAccessSettings() }
        findViewById<Button>(R.id.usageRefreshButton).setOnClickListener { renderAll() }
    }

    override fun onStart() {
        super.onStart()
        MonitoringStateStore.addListener(stateListener)
        LocalBatteryMonitorHub.attach(this, this)
    }

    override fun onResume() {
        super.onResume()
        renderAll()
    }

    override fun onStop() {
        LocalBatteryMonitorHub.detach(this)
        MonitoringStateStore.removeListener(stateListener)
        super.onStop()
    }

    private fun renderAll() {
        renderSummary()
        renderApps()
    }

    private fun renderSummary() {
        val info = MonitoringStateStore.current().info ?: BatteryStatusReader(this).read()
        val active = DischargeStore(this).active()
        findViewById<TextView>(R.id.usageBatteryValue).text = info.percent?.let { "$it%" } ?: "—"
        findViewById<TextView>(R.id.usageRateValue).text = BatteryUsageFormatter.rate(active?.ratePercentPerHour)
        findViewById<TextView>(R.id.usageCurrentValue).text = when {
            info.isPlugged == true -> "Pausado"
            else -> BatteryUsageFormatter.current(readInstantCurrentMa())
        }
        findViewById<TextView>(R.id.usageLiveHint).text = when {
            info.isPlugged == true -> "A análise de descarga fica pausada enquanto o carregador está conectado."
            active?.ratePercentPerHour != null -> "Ritmo medido pela sessão de descarga atual."
            else -> "A taxa fica mais confiável depois de alguns minutos e de queda real da bateria."
        }
    }

    private fun renderApps() {
        val granted = repository.hasUsageAccess()
        appsLoadGeneration += 1
        val generation = appsLoadGeneration
        accessCard.visibility = if (granted) View.GONE else View.VISIBLE
        appsList.removeAllViews()
        if (!granted) {
            emptyText.visibility = View.VISIBLE
            emptyText.text = "Libere o Acesso ao uso para identificar os apps mais ativos durante a descarga."
            findViewById<TextView>(R.id.usageInsightText).text = "A análise por aplicativo ainda não está ativada."
            return
        }

        emptyText.visibility = View.VISIBLE
        emptyText.text = "Analisando as últimas 6 horas…"
        findViewById<TextView>(R.id.usageInsightText).text = "Preparando ranking de atividade…"
        Thread {
            val result = runCatching { repository.topApps() }
            runOnUiThread {
                if (isFinishing || isDestroyed || generation != appsLoadGeneration) return@runOnUiThread
                result.onSuccess(::renderAppsResult).onFailure {
                    appsList.removeAllViews()
                    emptyText.visibility = View.VISIBLE
                    emptyText.text = "Não foi possível consultar a atividade dos aplicativos agora."
                    findViewById<TextView>(R.id.usageInsightText).text = "Tente atualizar novamente. O restante do monitoramento continua funcionando."
                }
            }
        }.start()
    }

    private fun renderAppsResult(apps: List<BatteryUsageRepository.AppUsage>) {
        appsList.removeAllViews()
        emptyText.visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE
        if (apps.isEmpty()) {
            emptyText.text = "Ainda não há atividade suficiente nas últimas 6 horas para comparar."
            findViewById<TextView>(R.id.usageInsightText).text = "Continue usando o aparelho normalmente e atualize depois."
            return
        }

        val top = apps.first()
        findViewById<TextView>(R.id.usageInsightText).text =
            "${top.label} teve a maior atividade em primeiro plano: ${BatteryUsageFormatter.duration(top.foregroundMs)} nas últimas 6 h."
        val inflater = LayoutInflater.from(this)
        apps.forEachIndexed { index, app ->
            val row = inflater.inflate(R.layout.battery_usage_item, appsList, false)
            row.findViewById<TextView>(R.id.usageAppRank).text = "${index + 1}"
            row.findViewById<TextView>(R.id.usageAppName).text = app.label
            row.findViewById<TextView>(R.id.usageAppDetail).text =
                "${BatteryUsageFormatter.duration(app.foregroundMs)} em primeiro plano • ${app.sharePercent}% do uso observado"
            row.findViewById<TextView>(R.id.usageAppImpact).text = BatteryUsageFormatter.impactLabel(app.sharePercent)
            app.icon?.let { row.findViewById<ImageView>(R.id.usageAppIcon).setImageDrawable(it) }
            appsList.addView(row)
        }
    }

    private fun readInstantCurrentMa(): Double? {
        val manager = getSystemService(BatteryManager::class.java)
        val currentUa = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (currentUa == Int.MIN_VALUE || currentUa == 0) return null
        return abs(currentUa.toDouble() / 1000.0).takeIf { it.isFinite() && it > 0.0 }
    }
}
