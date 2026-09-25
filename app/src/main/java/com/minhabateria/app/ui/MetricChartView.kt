package com.minhabateria.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.minhabateria.app.R
import com.minhabateria.app.charts.ChartMetric
import com.minhabateria.app.charts.ChartSample
import kotlin.math.max

class MetricChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private var metric = ChartMetric.POWER
    private var samples: List<ChartSample> = emptyList()
    private var rangeMs = 5 * 60_000L
    private var nowMs = System.currentTimeMillis()

    fun setSeries(metric: ChartMetric, samples: List<ChartSample>, rangeMs: Long, nowMs: Long) {
        this.metric = metric
        this.samples = samples
        this.rangeMs = rangeMs
        this.nowMs = nowMs
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val left = paddingLeft.toFloat() + dp(12f)
        val right = width - paddingRight.toFloat() - dp(12f)
        val top = paddingTop.toFloat() + dp(12f)
        val bottom = height - paddingBottom.toFloat() - dp(12f)
        if (right <= left || bottom <= top) return

        val values = samples.mapNotNull { metric.value(it) }.filter { it.isFinite() }
        drawHeader(canvas, left, right, top, values)
        drawPlot(canvas, left, right, top + dp(58f), bottom, values)
    }

    private fun drawHeader(canvas: Canvas, left: Float, right: Float, top: Float, values: List<Double>) {
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.textSize = sp(12f)
        paint.color = context.getColor(R.color.text_secondary)
        canvas.drawText(metric.title.uppercase(), left, top + sp(12f), paint)

        val latest = samples.asReversed().firstNotNullOfOrNull { metric.value(it) }
        paint.textSize = sp(21f)
        paint.color = metricColor()
        canvas.drawText(metric.format(latest), left, top + dp(39f), paint)

        paint.typeface = android.graphics.Typeface.DEFAULT
        paint.textSize = sp(9f)
        paint.color = context.getColor(R.color.text_muted)
        val range = if (values.isEmpty()) {
            "sem dados"
        } else {
            "mín ${metric.format(values.minOrNull())}  •  máx ${metric.format(values.maxOrNull())}"
        }
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(range, right, top + dp(38f), paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawPlot(
        canvas: Canvas,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        values: List<Double>
    ) {
        paint.strokeWidth = dp(1f)
        paint.color = context.getColor(R.color.divider)
        repeat(3) { index ->
            val y = top + (bottom - top) * index / 2f
            canvas.drawLine(left, y, right, y, paint)
        }

        if (values.size < 2) {
            paint.textSize = sp(10f)
            paint.color = context.getColor(R.color.text_muted)
            canvas.drawText("Aguardando amostras", left, bottom - dp(4f), paint)
            return
        }

        val rawMin = values.minOrNull() ?: return
        val rawMax = values.maxOrNull() ?: return
        val spread = max(rawMax - rawMin, max(kotlin.math.abs(rawMax) * 0.08, 1.0))
        val minValue = rawMin - spread * 0.12
        val maxValue = rawMax + spread * 0.12
        val cutoff = nowMs - rangeMs

        path.reset()
        var hasPoint = false
        samples.forEach { sample ->
            val value = metric.value(sample)
            if (value == null || !value.isFinite()) {
                hasPoint = false
                return@forEach
            }
            val xRatio = ((sample.timestampMs - cutoff).toDouble() / rangeMs).coerceIn(0.0, 1.0)
            val yRatio = ((value - minValue) / (maxValue - minValue)).coerceIn(0.0, 1.0)
            val x = left + (right - left) * xRatio.toFloat()
            val y = bottom - (bottom - top) * yRatio.toFloat()
            if (hasPoint) path.lineTo(x, y) else path.moveTo(x, y)
            hasPoint = true
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2.2f)
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = metricColor()
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
    }

    private fun metricColor(): Int = context.getColor(
        when (metric) {
            ChartMetric.POWER -> R.color.accent_blue
            ChartMetric.CURRENT -> R.color.accent_green
            ChartMetric.TEMPERATURE -> R.color.accent_orange
            ChartMetric.BATTERY -> R.color.accent_blue
        }
    )

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
    private fun sp(value: Float): Float = value * resources.displayMetrics.scaledDensity
}
