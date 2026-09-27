package com.minhabateria.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class DischargeTrendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(70, 73, 123, 153)
        strokeWidth = dp(1f)
    }
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(83, 216, 255)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = dp(2.3f)
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var startPercent: Int? = null
    private var currentPercent: Int? = null
    private var hasReliableSample: Boolean = false

    fun setData(startPercent: Int?, currentPercent: Int?, reliable: Boolean) {
        this.startPercent = startPercent
        this.currentPercent = currentPercent
        hasReliableSample = reliable
        contentDescription = if (startPercent != null && currentPercent != null) {
            "Tendência visual da descarga, de $startPercent% para $currentPercent%"
        } else {
            "Tendência da descarga aguardando dados"
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val left = paddingLeft.toFloat()
        val top = paddingTop.toFloat()
        val right = width.toFloat() - paddingRight
        val bottom = height.toFloat() - paddingBottom
        if (right <= left || bottom <= top) return

        drawGrid(canvas, left, top, right, bottom)
        val start = startPercent
        val current = currentPercent
        if (start == null || current == null) {
            drawWaitingBars(canvas, left, top, right, bottom)
            return
        }

        val low = min(start, current)
        val high = max(start, current)
        var scaleMin = (low - 5).coerceAtLeast(0)
        var scaleMax = (high + 5).coerceAtMost(100)
        if (scaleMax - scaleMin < 12) {
            val missing = 12 - (scaleMax - scaleMin)
            scaleMin = (scaleMin - missing / 2).coerceAtLeast(0)
            scaleMax = (scaleMax + missing - missing / 2).coerceAtMost(100)
        }

        val count = 12
        val gap = dp(3f)
        val usableWidth = right - left
        val barWidth = ((usableWidth - gap * (count - 1)) / count).coerceAtLeast(dp(2f))
        val path = Path()
        val points = ArrayList<Pair<Float, Float>>(count)

        repeat(count) { index ->
            val fraction = index / (count - 1f)
            val percent = start + (current - start) * fraction
            val denominator = (scaleMax - scaleMin).coerceAtLeast(1)
            val normalized = ((percent - scaleMin) / denominator.toFloat()).coerceIn(0f, 1f)
            val graphTop = top + dp(5f)
            val graphBottom = bottom - dp(3f)
            val y = graphBottom - normalized * (graphBottom - graphTop)
            val x = left + index * (barWidth + gap)
            val centerX = x + barWidth / 2f
            val color = blendColor(Color.rgb(27, 178, 255), Color.rgb(255, 183, 0), fraction)
            barPaint.color = color
            barPaint.alpha = if (hasReliableSample) 150 else 90
            canvas.drawRoundRect(x, y, x + barWidth, graphBottom, dp(3f), dp(3f), barPaint)
            points += centerX to y
        }

        points.forEachIndexed { index, point ->
            if (index == 0) path.moveTo(point.first, point.second) else path.lineTo(point.first, point.second)
        }
        linePaint.alpha = if (hasReliableSample) 255 else 150
        canvas.drawPath(path, linePaint)

        val startPoint = points.first()
        val endPoint = points.last()
        pointPaint.color = Color.rgb(72, 221, 255)
        pointPaint.alpha = 255
        canvas.drawCircle(startPoint.first, startPoint.second, dp(4.5f), pointPaint)
        pointPaint.color = Color.rgb(255, 194, 31)
        canvas.drawCircle(endPoint.first, endPoint.second, dp(4.7f), pointPaint)
        pointPaint.style = Paint.Style.STROKE
        pointPaint.strokeWidth = dp(1.4f)
        pointPaint.color = Color.WHITE
        canvas.drawCircle(endPoint.first, endPoint.second, dp(4.7f), pointPaint)
        pointPaint.style = Paint.Style.FILL
    }

    private fun drawGrid(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        repeat(5) { index ->
            val x = left + (right - left) * index / 4f
            canvas.drawLine(x, top, x, bottom, gridPaint)
        }
        canvas.drawLine(left, bottom, right, bottom, gridPaint)
    }

    private fun drawWaitingBars(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        val count = 8
        val gap = dp(4f)
        val barWidth = ((right - left - gap * (count - 1)) / count).coerceAtLeast(dp(2f))
        barPaint.color = Color.rgb(40, 112, 154)
        barPaint.alpha = 65
        repeat(count) { index ->
            val fraction = index / (count - 1f)
            val x = left + index * (barWidth + gap)
            val y = top + (bottom - top) * (0.32f + fraction * 0.22f)
            canvas.drawRoundRect(x, y, x + barWidth, bottom, dp(3f), dp(3f), barPaint)
        }
    }

    private fun blendColor(start: Int, end: Int, fraction: Float): Int {
        val f = fraction.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(start) + (Color.red(end) - Color.red(start)) * f).toInt(),
            (Color.green(start) + (Color.green(end) - Color.green(start)) * f).toInt(),
            (Color.blue(start) + (Color.blue(end) - Color.blue(start)) * f).toInt()
        )
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
