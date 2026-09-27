package com.minhabateria.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.minhabateria.app.charts.ChartInsightBuilder
import com.minhabateria.app.charts.ChartMetric
import com.minhabateria.app.charts.ChartRange
import com.minhabateria.app.charts.ChartSample
import com.minhabateria.app.charts.ChartSampleRepository
import com.minhabateria.app.interpretation.ChargeConditionInterpreter
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
    private lateinit var rangeSelector: TextView
    private lateinit var chartMetricTitle: TextView
    private lateinit var chartMetricValue: TextView
    private lateinit var chartMetricContext: TextView
    private lateinit var chartStateLabel: TextView
    private lateinit var chartInsightLine: TextView
    private lateinit var detailsToggle: TextView
    private lateinit var statsRow: View
    private lateinit var chartStat1Label: TextView
    private lateinit var chartStat1Value: TextView
    private lateinit var chartStat2Label: TextView
    private lateinit var chartStat2Value: TextView
    private lateinit var chartStat3Label: TextView
    private lateinit var chartStat3Value: TextView
    private lateinit var chartStat4Label: TextView
    private lateinit var chartStat4Value: TextView
    private var range = ChartRange.MINUTES_60
    private var metric = ChartMetric.BATTERY
    private var detailsVisible = false

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
        bindRangeSelector()
        bindDetails()
        bindBottomTabs()
        renderMetricButtons()
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
        outState.putBoolean(STATE_DETAILS, detailsVisible)
        super.onSaveInstanceState(outState)
    }

    private fun restoreSelection(state: Bundle?) {
        metric = state?.getString(STATE_METRIC)
            ?.let { runCatching { ChartMetric.valueOf(it) }.getOrNull() }
            ?: ChartMetric.BATTERY
        range = state?.getString(STATE_RANGE)
            ?.let { runCatching { ChartRange.valueOf(it) }.getOrNull() }
            ?: ChartRange.MINUTES_60
        detailsVisible = state?.getBoolean(STATE_DETAILS, false) ?: false
    }

    private fun bindViews() {
        metricChart = findViewById(R.id.metricChart)
        windowLabel = findViewById(R.id.chartWindowLabel)
        rangeSelector = findViewById(R.id.chartRangeSelector)
        chartMetricTitle = findViewById(R.id.chartMetricTitle)
        chartMetricValue = findViewById(R.id.chartMetricValue)
        chartMetricContext = findViewById(R.id.chartMetricMinMax)
        chartStateLabel = findViewById(R.id.chartStateLabel)
        chartInsightLine = findViewById(R.id.chartInsightLine)
        detailsToggle = findViewById(R.id.chartDetailsToggle)
        statsRow = findViewById(R.id.chartStatsRow)
        chartStat1Label = findViewById(R.id.chartStat1Label)
        chartStat1Value = findViewById(R.id.chartStat1Value)
        chartStat2Label = findViewById(R.id.chartStat2Label)
        chartStat2Value = findViewById(R.id.chartStat2Value)
        chartStat3Label = findViewById(R.id.chartStat3Label)
        chartStat3Value = findViewById(R.id.chartStat3Value)
        chartStat4Label = findViewById(R.id.chartStat4Label)
        chartStat4Value = findViewById(R.id.chartStat4Value)
    }

    private fun bindMetrics() {
        findViewById<TextView>(R.id.metricBattery).setOnClickListener { selectMetric(ChartMetric.BATTERY) }
        findViewById<TextView>(R.id.metricCurrent).setOnClickListener { selectMetric(ChartMetric.CURRENT) }
        findViewById<TextView>(R.id.metricPower).setOnClickListener { selectMetric(ChartMetric.POWER) }
        findViewById<TextView>(R.id.metricTemperature).setOnClickListener { selectMetric(ChartMetric.TEMPERATURE) }
    }

    private fun bindRangeSelector() {
        rangeSelector.setOnClickListener {
            val ranges = listOf(ChartRange.MINUTES_5, ChartRange.MINUTES_15, ChartRange.MINUTES_60)
            val labels = ranges.map { "${it.minutes} min" }
            AlertDialog.Builder(this)
                .setTitle("Período do gráfico")
                .setSingleChoiceItems(labels.toTypedArray(), ranges.indexOf(range)) { dialog, which ->
                    selectRange(ranges[which])
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun bindDetails() {
        detailsToggle.setOnClickListener {
            detailsVisible = !detailsVisible
            syncDetailsVisibility()
        }
        syncDetailsVisibility()
    }

    private fun syncDetailsVisibility() {
        statsRow.visibility = if (detailsVisible) View.VISIBLE else View.GONE
        detailsToggle.text = if (detailsVisible) "Ocultar detalhes do período  ‹" else "Ver detalhes do período  ›"
        detailsToggle.setTextColor(getColor(if (detailsVisible) R.color.text_primary else R.color.text_secondary))
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
        renderChart()
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
            view.background = getDrawable(if (active) R.drawable.bg_chart_selector_active else R.drawable.bg_secondary_button)
            view.setTextColor(getColor(if (active) item.accentColorRes() else R.color.text_secondary))
        }
    }

    private fun renderChart() {
        val now = System.currentTimeMillis()
        val samples = ChartSampleRepository.samples(this, range, now)
        val values = samples.mapNotNull { metric.value(it)?.takeIf(Double::isFinite) }
        val latest = samples.asReversed().firstNotNullOfOrNull { metric.value(it)?.takeIf(Double::isFinite) }
        val insight = ChartInsightBuilder.build(metric, samples)

        metricChart.setSeries(metric, samples, range.durationMs, now)
        metricChart.contentDescription = "${metric.title}, últimos ${range.minutes} minutos"
        rangeSelector.text = "Período: ${range.minutes} min ▼"
        windowLabel.text = "${values.size} amostras • até 1 a cada 10 s"
        chartMetricTitle.text = "${metric.title} agora"
        chartMetricValue.text = metric.format(latest)
        chartMetricValue.setTextColor(getColor(metricValueColor(latest)))
        chartStateLabel.text = "●  ${insight.state}"
        chartStateLabel.setTextColor(getColor(levelColor(insight.level)))
        chartMetricContext.text = insight.context
        chartInsightLine.text = insight.explanation
        renderStats(samples, values)
    }

    private fun renderStats(samples: List<ChartSample>, values: List<Double>) {
        val min = values.minOrNull()
        val max = values.maxOrNull()
        val average = values.takeIf { it.isNotEmpty() }?.average()
        when (metric) {
            ChartMetric.BATTERY -> {
                val first = samples.firstNotNullOfOrNull { metric.value(it) }
                stat1("Início", metric.format(first))
                stat2("Mín.", metric.format(min))
                stat3("Máx.", metric.format(max))
                stat4("Amostras", values.size.toString())
            }
            ChartMetric.CURRENT, ChartMetric.POWER, ChartMetric.TEMPERATURE -> {
                stat1("Mín.", metric.format(min))
                stat2("Máx.", metric.format(max))
                stat3("Média", metric.format(average))
                stat4("Amostras", values.size.toString())
            }
        }
    }

    private fun stat1(label: String, value: String) {
        chartStat1Label.text = label
        chartStat1Value.text = value
    }

    private fun stat2(label: String, value: String) {
        chartStat2Label.text = label
        chartStat2Value.text = value
    }

    private fun stat3(label: String, value: String) {
        chartStat3Label.text = label
        chartStat3Value.text = value
    }

    private fun stat4(label: String, value: String) {
        chartStat4Label.text = label
        chartStat4Value.text = value
    }

    private fun metricValueColor(latest: Double?): Int = when (metric) {
        ChartMetric.BATTERY -> R.color.accent_blue
        ChartMetric.POWER -> R.color.accent_blue
        ChartMetric.TEMPERATURE -> when {
            latest == null -> R.color.value_unavailable
            latest >= 45.0 -> R.color.accent_red
            latest >= 38.0 -> R.color.accent_orange
            else -> R.color.accent_green
        }
        ChartMetric.CURRENT -> when {
            latest == null -> R.color.value_unavailable
            latest < -50.0 -> R.color.accent_blue
            latest > 50.0 -> R.color.accent_green
            else -> R.color.text_secondary
        }
    }

    private fun levelColor(level: ChargeConditionInterpreter.Level): Int = when (level) {
        ChargeConditionInterpreter.Level.GOOD -> R.color.accent_green
        ChargeConditionInterpreter.Level.INFO -> R.color.accent_blue
        ChargeConditionInterpreter.Level.ATTENTION -> R.color.accent_orange
        ChargeConditionInterpreter.Level.ELEVATED -> R.color.accent_orange
        ChargeConditionInterpreter.Level.CRITICAL -> R.color.accent_red
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
        const val STATE_DETAILS = "chart_details"
    }
}
