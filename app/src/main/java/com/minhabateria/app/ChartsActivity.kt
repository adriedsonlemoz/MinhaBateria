package com.minhabateria.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.minhabateria.app.charts.ChartMetric
import com.minhabateria.app.charts.ChartRange
import com.minhabateria.app.charts.ChartSample
import com.minhabateria.app.charts.ChartSampleRepository
import com.minhabateria.app.monitoring.LocalBatteryMonitorHub
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.ui.BottomTab
import com.minhabateria.app.ui.BottomTabsBinder
import com.minhabateria.app.ui.MetricChartView
import com.minhabateria.app.ui.SystemBars
import java.util.Locale
import kotlin.math.abs

class ChartsActivity : Activity() {
    private lateinit var metricChart: MetricChartView
    private lateinit var windowLabel: TextView
    private lateinit var chartMetricTitle: TextView
    private lateinit var chartMetricValue: TextView
    private lateinit var chartMetricMinMax: TextView
    private lateinit var chartInsightLine: TextView
    private lateinit var chartStat1Label: TextView
    private lateinit var chartStat1Value: TextView
    private lateinit var chartStat2Label: TextView
    private lateinit var chartStat2Value: TextView
    private lateinit var chartStat3Label: TextView
    private lateinit var chartStat3Value: TextView
    private lateinit var chartStat4Label: TextView
    private lateinit var chartStat4Value: TextView
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
        chartMetricTitle = findViewById(R.id.chartMetricTitle)
        chartMetricValue = findViewById(R.id.chartMetricValue)
        chartMetricMinMax = findViewById(R.id.chartMetricMinMax)
        chartInsightLine = findViewById(R.id.chartInsightLine)
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
        val values = samples.mapNotNull { metric.value(it)?.takeIf(Double::isFinite) }
        metricChart.setSeries(metric, samples, range.durationMs, now)
        metricChart.contentDescription = "${metric.title}, últimos ${range.minutes} minutos"
        windowLabel.text = "${metric.title} • 1 amostra a cada 10 s • últimos ${range.minutes} min"
        chartMetricTitle.text = metric.title.uppercase(Locale.getDefault())

        val latest = samples.asReversed().firstNotNullOfOrNull { metric.value(it)?.takeIf(Double::isFinite) }
        chartMetricValue.text = metric.format(latest)
        chartMetricValue.setTextColor(getColor(metricValueColor(latest)))
        chartMetricMinMax.text = if (values.isEmpty()) {
            "Sem dados observados"
        } else {
            "mín ${metric.format(values.minOrNull())} • máx ${metric.format(values.maxOrNull())}"
        }
        chartInsightLine.text = insightText(values, latest)
        renderStats(samples, values, latest)
    }

    private fun renderStats(samples: List<ChartSample>, values: List<Double>, latest: Double?) {
        val average = values.takeIf { it.isNotEmpty() }?.average()
        val min = values.minOrNull()
        val max = values.maxOrNull()
        val spread = if (min != null && max != null) max - min else null
        when (metric) {
            ChartMetric.BATTERY -> {
                stat1("Atual", metric.format(latest), metricValueColor(latest))
                stat2("Média", metric.format(average), R.color.text_primary)
                val delta = if (values.isEmpty()) null else values.last() - values.first()
                stat3("Variação", formatDeltaPercent(delta), R.color.text_primary)
                stat4("Amostra", stability(values), stabilityColor(values))
            }
            ChartMetric.CURRENT -> {
                stat1("Atual", metric.format(latest), metricValueColor(latest))
                stat2("Média", metric.format(average), R.color.text_primary)
                stat3("Faixa", metric.format(spread), R.color.text_primary)
                stat4("Estado", currentState(latest, values), metricValueColor(latest))
            }
            ChartMetric.POWER -> {
                stat1("Atual", metric.format(latest), metricValueColor(latest))
                stat2("Média", metric.format(average), R.color.text_primary)
                stat3("Pico", metric.format(max), R.color.text_primary)
                stat4("Amostra", stability(values), stabilityColor(values))
            }
            ChartMetric.TEMPERATURE -> {
                stat1("Atual", metric.format(latest), metricValueColor(latest))
                stat2("Média", metric.format(average), R.color.text_primary)
                stat3("Máx.", metric.format(max), R.color.text_primary)
                stat4("Faixa", metric.format(spread), R.color.accent_orange)
            }
        }
    }

    private fun stat1(label: String, value: String, colorRes: Int) {
        chartStat1Label.text = label
        chartStat1Value.text = value
        chartStat1Value.setTextColor(getColor(colorRes))
    }

    private fun stat2(label: String, value: String, colorRes: Int) {
        chartStat2Label.text = label
        chartStat2Value.text = value
        chartStat2Value.setTextColor(getColor(colorRes))
    }

    private fun stat3(label: String, value: String, colorRes: Int) {
        chartStat3Label.text = label
        chartStat3Value.text = value
        chartStat3Value.setTextColor(getColor(colorRes))
    }

    private fun stat4(label: String, value: String, colorRes: Int) {
        chartStat4Label.text = label
        chartStat4Value.text = value
        chartStat4Value.setTextColor(getColor(colorRes))
    }

    private fun insightText(values: List<Double>, latest: Double?): String = when (metric) {
        ChartMetric.BATTERY -> when {
            values.isEmpty() -> "Aguardando amostras do período"
            abs(values.last() - values.first()) < 0.5 -> "Sem variação no período"
            values.last() > values.first() -> "Subida de bateria observada no período"
            else -> "Queda de bateria observada no período"
        }
        ChartMetric.CURRENT -> when {
            latest == null -> "Aguardando leitura de corrente"
            latest < 0.0 -> "Descarga ativa"
            latest > 0.0 -> "Carga ativa"
            else -> "Corrente neutra no momento"
        }
        ChartMetric.POWER -> when {
            latest == null -> "Aguardando leitura de potência"
            latest <= 0.0 -> "Sem potência positiva observada"
            else -> "Potência observada em tempo real"
        }
        ChartMetric.TEMPERATURE -> when {
            latest == null -> "Aguardando leitura térmica"
            latest >= 42.0 -> "Temperatura elevada no momento"
            latest >= 37.0 -> "Temperatura de atenção"
            else -> "Temperatura sob controle"
        }
    }

    private fun stability(values: List<Double>): String {
        if (values.size < 2) return "aguardando"
        val min = values.minOrNull() ?: return "aguardando"
        val max = values.maxOrNull() ?: return "aguardando"
        val average = values.average().takeIf { it != 0.0 } ?: return "estável"
        val ratio = abs(max - min) / abs(average)
        return when {
            ratio <= 0.10 -> "estável"
            ratio <= 0.25 -> "moderada"
            ratio <= 0.45 -> "oscilando"
            else -> "instável"
        }
    }

    private fun stabilityColor(values: List<Double>): Int = when (stability(values)) {
        "estável" -> R.color.accent_green
        "moderada" -> R.color.accent_blue
        "oscilando" -> R.color.accent_orange
        "instável" -> R.color.accent_red
        else -> R.color.text_secondary
    }

    private fun currentState(latest: Double?, values: List<Double>): String = when {
        latest == null -> "aguardando"
        values.any { it < 0.0 } && values.any { it > 0.0 } -> "misto"
        latest < 0.0 -> "descarga"
        latest > 0.0 -> "carga"
        else -> "neutro"
    }

    private fun formatDeltaPercent(delta: Double?): String = when {
        delta == null -> "—"
        delta > 0.0 -> "+${delta.toInt()}%"
        delta < 0.0 -> "${delta.toInt()}%"
        else -> "0%"
    }

    private fun metricValueColor(latest: Double?): Int = when (metric) {
        ChartMetric.BATTERY -> R.color.accent_blue
        ChartMetric.POWER -> R.color.accent_blue
        ChartMetric.TEMPERATURE -> R.color.accent_orange
        ChartMetric.CURRENT -> when {
            latest == null -> R.color.text_secondary
            latest < 0.0 -> R.color.accent_red
            latest > 0.0 -> R.color.accent_green
            else -> R.color.text_secondary
        }
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
