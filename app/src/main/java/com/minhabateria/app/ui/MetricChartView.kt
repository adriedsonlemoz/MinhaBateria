package com.minhabateria.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.minhabateria.app.R
import com.minhabateria.app.charts.ChartMetric
import com.minhabateria.app.charts.ChartSample
import kotlin.math.abs
import kotlin.math.max

class MetricChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val areaPath = Path()
    private var metric = ChartMetric.BATTERY
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
        val left = paddingLeft.toFloat() + dp(14f)
        val right = width - paddingRight.toFloat() - dp(14f)
        val top = paddingTop.toFloat() + dp(14f)
        val bottom = height - paddingBottom.toFloat() - dp(12f)
        if (right <= left || bottom <= top) return

        val values = samples.mapNotNull { sample -> metric.value(sample)?.takeIf { it.isFinite() } }
        drawHeader(canvas, left, right, top, values)
        drawPlot(canvas, left, right, top + dp(72f), bottom - dp(24f), values)
        drawTimeAxis(canvas, left, right, bottom)
    }

    private fun drawHeader(canvas: Canvas, left: Float, right: Float, top: Float, values: List<Double>) {
        paint.shader = null
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = sp(12f)
        paint.color = context.getColor(R.color.text_secondary)
        canvas.drawText(metric.title.uppercase(), left, top + sp(12f), paint)

        val latest = samples.asReversed().firstNotNullOfOrNull { metric.value(it)?.takeIf(Double::isFinite) }
        paint.textSize = sp(27f)
        paint.color = metricColor(latest)
        canvas.drawText(metric.format(latest), left, top + dp(45f), paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = sp(10f)
        paint.color = context.getColor(R.color.text_muted)
        paint.textAlign = Paint.Align.RIGHT
        val summary = if (values.isEmpty()) {
            "sem dados observados"
        } else {
            "mín ${metric.format(values.minOrNull())}  •  máx ${metric.format(values.maxOrNull())}"
        }
        canvas.drawText(summary, right, top + dp(44f), paint)
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
        if (bottom <= top) return
        drawGrid(canvas, left, right, top, bottom)

        if (values.size < 2) {
            paint.shader = null
            paint.typeface = Typeface.DEFAULT
            paint.textSize = sp(11f)
            paint.color = context.getColor(R.color.text_muted)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Aguardando amostras", (left + right) / 2f, (top + bottom) / 2f, paint)
            paint.textAlign = Paint.Align.LEFT
            return
        }

        val (minValue, maxValue) = scaleFor(values)
        drawZeroLineIfVisible(canvas, left, right, top, bottom, minValue, maxValue)
        val cutoff = nowMs - rangeMs
        val segments = buildSegments()
        val latestPair = segments.lastOrNull()?.lastOrNull()
        val seriesColor = metricSeriesColor(values)
        val latestColor = metricColor(latestPair?.second)

        segments.filter { it.size >= 2 }.forEach { segment ->
            drawSegment(canvas, segment, left, right, top, bottom, cutoff, minValue, maxValue, seriesColor)
        }

        latestPair?.let { (sample, value) ->
            drawLatestMarker(canvas, sample, value, left, right, top, bottom, cutoff, minValue, maxValue, latestColor)
        }
    }

    private fun buildSegments(): List<List<Pair<ChartSample, Double>>> {
        val result = mutableListOf<MutableList<Pair<ChartSample, Double>>>()
        var current = mutableListOf<Pair<ChartSample, Double>>()
        var previousTimestamp: Long? = null

        fun closeSegment() {
            if (current.isNotEmpty()) result += current
            current = mutableListOf()
            previousTimestamp = null
        }

        samples.forEach { sample ->
            val value = metric.value(sample)?.takeIf { it.isFinite() }
            val previous = previousTimestamp
            if (value == null) {
                closeSegment()
                return@forEach
            }
            if (previous != null && sample.timestampMs - previous > MAX_CONNECTED_GAP_MS) {
                closeSegment()
            }
            current += sample to value
            previousTimestamp = sample.timestampMs
        }
        closeSegment()
        return result
    }

    private fun drawSegment(
        canvas: Canvas,
        segment: List<Pair<ChartSample, Double>>,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        cutoff: Long,
        minValue: Double,
        maxValue: Double,
        color: Int
    ) {
        path.reset()
        areaPath.reset()
        var firstX = 0f
        var lastX = 0f

        segment.forEachIndexed { index, (sample, value) ->
            val (x, y) = pointFor(sample, value, left, right, top, bottom, cutoff, minValue, maxValue)
            if (index == 0) {
                path.moveTo(x, y)
                areaPath.moveTo(x, bottom)
                areaPath.lineTo(x, y)
                firstX = x
            } else {
                path.lineTo(x, y)
                areaPath.lineTo(x, y)
            }
            lastX = x
        }

        areaPath.lineTo(lastX, bottom)
        areaPath.lineTo(firstX, bottom)
        areaPath.close()
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            0f, top, 0f, bottom,
            withAlpha(color, 58),
            withAlpha(color, 4),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(areaPath, paint)

        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2.6f)
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = color
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawLatestMarker(
        canvas: Canvas,
        sample: ChartSample,
        value: Double,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        cutoff: Long,
        minValue: Double,
        maxValue: Double,
        color: Int
    ) {
        val (x, y) = pointFor(sample, value, left, right, top, bottom, cutoff, minValue, maxValue)
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = context.getColor(R.color.surface)
        canvas.drawCircle(x, y, dp(5.2f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2.4f)
        paint.color = color
        canvas.drawCircle(x, y, dp(5.2f), paint)
        paint.style = Paint.Style.FILL
    }

    private fun pointFor(
        sample: ChartSample,
        value: Double,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        cutoff: Long,
        minValue: Double,
        maxValue: Double
    ): Pair<Float, Float> {
        val xRatio = ((sample.timestampMs - cutoff).toDouble() / rangeMs).coerceIn(0.0, 1.0)
        val yRatio = ((value - minValue) / (maxValue - minValue)).coerceIn(0.0, 1.0)
        val x = left + (right - left) * xRatio.toFloat()
        val y = bottom - (bottom - top) * yRatio.toFloat()
        return x to y
    }

    private fun drawGrid(canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float) {
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1f)
        paint.color = context.getColor(R.color.divider)
        repeat(4) { index ->
            val y = top + (bottom - top) * index / 3f
            canvas.drawLine(left, y, right, y, paint)
        }
        repeat(4) { index ->
            val x = left + (right - left) * index / 3f
            canvas.drawLine(x, top, x, bottom, paint)
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawZeroLineIfVisible(
        canvas: Canvas,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        minValue: Double,
        maxValue: Double
    ) {
        if (minValue >= 0.0 || maxValue <= 0.0) return
        val ratio = (0.0 - minValue) / (maxValue - minValue)
        val y = bottom - (bottom - top) * ratio.toFloat()
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.2f)
        paint.color = context.getColor(R.color.text_muted)
        canvas.drawLine(left, y, right, y, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawTimeAxis(canvas: Canvas, left: Float, right: Float, bottom: Float) {
        paint.shader = null
        paint.typeface = Typeface.DEFAULT
        paint.textSize = sp(9f)
        paint.color = context.getColor(R.color.text_muted)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("−${formatDuration(rangeMs)}", left, bottom, paint)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("−${formatDuration(rangeMs / 2)}", (left + right) / 2f, bottom, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("agora", right, bottom, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun scaleFor(values: List<Double>): Pair<Double, Double> {
        val rawMin = values.minOrNull() ?: return 0.0 to 1.0
        val rawMax = values.maxOrNull() ?: return 0.0 to 1.0
        return when (metric) {
            ChartMetric.BATTERY -> 0.0 to 100.0
            ChartMetric.POWER -> {
                if (rawMin >= 0.0) {
                    0.0 to max(rawMax * 1.12, 1.0)
                } else {
                    paddedRange(rawMin, rawMax, minimumSpread = 1.0)
                }
            }
            ChartMetric.CURRENT -> {
                val padding = max((rawMax - rawMin) * 0.12, 80.0)
                when {
                    rawMin >= 0.0 -> 0.0 to max(rawMax + padding, 100.0)
                    rawMax <= 0.0 -> minOf(rawMin - padding, -100.0) to 0.0
                    else -> (rawMin - padding) to (rawMax + padding)
                }
            }
            ChartMetric.TEMPERATURE -> paddedRange(rawMin, rawMax, minimumSpread = 2.0)
        }
    }

    private fun paddedRange(rawMin: Double, rawMax: Double, minimumSpread: Double): Pair<Double, Double> {
        val spread = max(rawMax - rawMin, max(abs(rawMax) * 0.08, minimumSpread))
        return (rawMin - spread * 0.15) to (rawMax + spread * 0.15)
    }


    private fun metricSeriesColor(values: List<Double>): Int = context.getColor(
        when (metric) {
            ChartMetric.POWER -> R.color.accent_blue
            ChartMetric.CURRENT -> when {
                values.isNotEmpty() && values.all { it < 0.0 } -> R.color.accent_red
                values.isNotEmpty() && values.all { it > 0.0 } -> R.color.accent_green
                else -> R.color.accent_blue
            }
            ChartMetric.TEMPERATURE -> R.color.accent_orange
            ChartMetric.BATTERY -> R.color.accent_blue
        }
    )

    private fun metricColor(latest: Double?): Int = context.getColor(
        when (metric) {
            ChartMetric.POWER -> R.color.accent_blue
            ChartMetric.CURRENT -> when {
                latest == null -> R.color.text_secondary
                latest < 0.0 -> R.color.accent_red
                latest > 0.0 -> R.color.accent_green
                else -> R.color.text_secondary
            }
            ChartMetric.TEMPERATURE -> R.color.accent_orange
            ChartMetric.BATTERY -> R.color.accent_blue
        }
    )

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return if (seconds == 0L) "${minutes} min" else "${minutes}m${seconds.toString().padStart(2, '0')}s"
    }

    private fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
    private fun sp(value: Float): Float = value * resources.displayMetrics.scaledDensity

    private companion object {
        const val MAX_CONNECTED_GAP_MS = 30_000L
    }
}
