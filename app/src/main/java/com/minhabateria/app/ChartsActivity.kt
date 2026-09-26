package com.minhabateria.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.minhabateria.app.charts.ChartMetric
import com.minhabateria.app.charts.ChartRange
import com.minhabateria.app.charts.ChartSampleRepository
import com.minhabateria.app.monitoring.LocalBatteryMonitorHub
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.ui.BottomTab
import com.minhabateria.app.ui.BottomTabsBinder
import com.minhabateria.app.ui.MetricChartView
import com.minhabateria.app.ui.SystemBars

class ChartsActivity : Activity() {
    private lateinit var powerChart: MetricChartView
    private lateinit var currentChart: MetricChartView
    private lateinit var temperatureChart: MetricChartView
    private lateinit var batteryChart: MetricChartView
    private lateinit var windowLabel: TextView
    private var range = ChartRange.MINUTES_5

    private val stateListener: (MonitoringState) -> Unit = {
        runOnUiThread {
            renderCharts()
            LocalBatteryMonitorHub.sync(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)
        SystemBars.apply(this, findViewById(R.id.chartsRoot))
        bindViews()
        bindRanges()
        BottomTabsBinder(this).bind(
            active = BottomTab.CHARTS,
            openNow = { finish() },
            openCharts = {},
            openSession = {
                startActivity(Intent(this, SessionActivity::class.java))
                finish()
            },
            openDischarge = {
                startActivity(Intent(this, DischargeRateActivity::class.java))
                finish()
            },
            openHistory = {
                startActivity(Intent(this, HistoryActivity::class.java))
                finish()
            }
        )
        renderRangeButtons()
        renderCharts()
    }

    override fun onStart() {
        super.onStart()
        MonitoringStateStore.addListener(stateListener)
        LocalBatteryMonitorHub.attach(this, this)
    }

    override fun onResume() {
        super.onResume()
        renderCharts()
    }

    override fun onStop() {
        LocalBatteryMonitorHub.detach(this)
        MonitoringStateStore.removeListener(stateListener)
        super.onStop()
    }

    private fun bindViews() {
        powerChart = findViewById(R.id.powerChart)
        currentChart = findViewById(R.id.currentChart)
        temperatureChart = findViewById(R.id.temperatureChart)
        batteryChart = findViewById(R.id.batteryChart)
        windowLabel = findViewById(R.id.chartWindowLabel)
    }

    private fun bindRanges() {
        findViewById<TextView>(R.id.range5).setOnClickListener { selectRange(ChartRange.MINUTES_5) }
        findViewById<TextView>(R.id.range15).setOnClickListener { selectRange(ChartRange.MINUTES_15) }
        findViewById<TextView>(R.id.range60).setOnClickListener { selectRange(ChartRange.MINUTES_60) }
    }

    private fun selectRange(newRange: ChartRange) {
        if (range == newRange) return
        range = newRange
        renderRangeButtons()
        renderCharts()
    }

    private fun renderRangeButtons() {
        listOf(
            R.id.range5 to ChartRange.MINUTES_5,
            R.id.range15 to ChartRange.MINUTES_15,
            R.id.range60 to ChartRange.MINUTES_60
        ).forEach { (id, item) ->
            val view = findViewById<TextView>(id)
            val active = item == range
            view.background = getDrawable(if (active) R.drawable.bg_tab_active else R.drawable.bg_secondary_button)
            view.setTextColor(getColor(if (active) R.color.accent_green else R.color.text_secondary))
        }
        windowLabel.text = "1 amostra a cada 10 s • últimos ${range.minutes} min"
    }

    private fun renderCharts() {
        val now = System.currentTimeMillis()
        val samples = ChartSampleRepository.samples(this, range, now)
        powerChart.setSeries(ChartMetric.POWER, samples, range.durationMs, now)
        currentChart.setSeries(ChartMetric.CURRENT, samples, range.durationMs, now)
        temperatureChart.setSeries(ChartMetric.TEMPERATURE, samples, range.durationMs, now)
        batteryChart.setSeries(ChartMetric.BATTERY, samples, range.durationMs, now)
    }
}
