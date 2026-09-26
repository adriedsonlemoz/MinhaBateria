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
    private lateinit var metricChart: MetricChartView
    private lateinit var windowLabel: TextView
    private var range = ChartRange.MINUTES_5
    private var metric = ChartMetric.BATTERY

    private val stateListener: (MonitoringState) -> Unit = {
        runOnUiThread {
            renderChart()
            LocalBatteryMonitorHub.sync(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)
        SystemBars.apply(this, findViewById(R.id.chartsRoot))
        restoreSelection(savedInstanceState)
        bindViews()
        bindMetrics()
        bindRanges()
        bindBottomTabs()
        renderSelectors()
        renderChart()
    }

    override fun onStart() {
        super.onStart()
        MonitoringStateStore.addListener(stateListener)
        LocalBatteryMonitorHub.attach(this, this)
    }

    override fun onResume() {
        super.onResume()
        renderChart()
    }

    override fun onStop() {
        LocalBatteryMonitorHub.detach(this)
        MonitoringStateStore.removeListener(stateListener)
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_METRIC, metric.name)
        outState.putString(STATE_RANGE, range.name)
        super.onSaveInstanceState(outState)
    }

    private fun restoreSelection(state: Bundle?) {
        metric = state?.getString(STATE_METRIC)
            ?.let { runCatching { ChartMetric.valueOf(it) }.getOrNull() }
            ?: ChartMetric.BATTERY
        range = state?.getString(STATE_RANGE)
            ?.let { runCatching { ChartRange.valueOf(it) }.getOrNull() }
            ?: ChartRange.MINUTES_5
    }

    private fun bindViews() {
        metricChart = findViewById(R.id.metricChart)
        windowLabel = findViewById(R.id.chartWindowLabel)
    }

    private fun bindMetrics() {
        findViewById<TextView>(R.id.metricBattery).setOnClickListener { selectMetric(ChartMetric.BATTERY) }
        findViewById<TextView>(R.id.metricCurrent).setOnClickListener { selectMetric(ChartMetric.CURRENT) }
        findViewById<TextView>(R.id.metricPower).setOnClickListener { selectMetric(ChartMetric.POWER) }
        findViewById<TextView>(R.id.metricTemperature).setOnClickListener { selectMetric(ChartMetric.TEMPERATURE) }
    }

    private fun bindRanges() {
        findViewById<TextView>(R.id.range5).setOnClickListener { selectRange(ChartRange.MINUTES_5) }
        findViewById<TextView>(R.id.range15).setOnClickListener { selectRange(ChartRange.MINUTES_15) }
        findViewById<TextView>(R.id.range60).setOnClickListener { selectRange(ChartRange.MINUTES_60) }
    }

    private fun bindBottomTabs() {
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
    }

    private fun selectMetric(newMetric: ChartMetric) {
        if (metric == newMetric) return
        metric = newMetric
        renderMetricButtons()
        renderChart()
    }

    private fun selectRange(newRange: ChartRange) {
        if (range == newRange) return
        range = newRange
        renderRangeButtons()
        renderChart()
    }

    private fun renderSelectors() {
        renderMetricButtons()
        renderRangeButtons()
    }

    private fun renderMetricButtons() {
        listOf(
            R.id.metricBattery to ChartMetric.BATTERY,
            R.id.metricCurrent to ChartMetric.CURRENT,
            R.id.metricPower to ChartMetric.POWER,
            R.id.metricTemperature to ChartMetric.TEMPERATURE
        ).forEach { (id, item) ->
            val view = findViewById<TextView>(id)
            val active = item == metric
            view.background = getDrawable(
                if (active) R.drawable.bg_chart_selector_active else R.drawable.bg_secondary_button
            )
            view.setTextColor(getColor(if (active) item.accentColorRes() else R.color.text_secondary))
        }
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
    }

    private fun renderChart() {
        val now = System.currentTimeMillis()
        val samples = ChartSampleRepository.samples(this, range, now)
        metricChart.setSeries(metric, samples, range.durationMs, now)
        metricChart.contentDescription = "${metric.title}, últimos ${range.minutes} minutos"
        windowLabel.text = "${metric.title} • 1 amostra a cada 10 s • últimos ${range.minutes} min"
    }

    private fun ChartMetric.accentColorRes(): Int = when (this) {
        ChartMetric.POWER -> R.color.accent_blue
        ChartMetric.CURRENT -> R.color.accent_green
        ChartMetric.TEMPERATURE -> R.color.accent_orange
        ChartMetric.BATTERY -> R.color.accent_blue
    }

    private companion object {
        const val STATE_METRIC = "chart_metric"
        const val STATE_RANGE = "chart_range"
    }
}
