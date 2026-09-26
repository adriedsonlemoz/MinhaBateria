package com.minhabateria.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class BatteryGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(72, 30, 152, 230)
        style = Paint.Style.STROKE
    }

    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(5, 18, 28)
        style = Paint.Style.FILL
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(43, 61, 76)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val progressHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        alpha = 54
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val batteryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val secondaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(182, 197, 211)
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private var percent: Int? = null
    private var charging = false
    private var secondaryText: String? = null

    fun setBattery(percent: Int?, charging: Boolean, secondaryText: String? = null) {
        this.percent = percent?.coerceIn(0, 100)
        this.charging = charging
        this.secondaryText = secondaryText?.takeIf { it.isNotBlank() }
        contentDescription = buildString {
            append(percent?.let { "Bateria em $it por cento" } ?: "Percentual da bateria indisponível")
            this@BatteryGaugeView.secondaryText?.let { append(". $it") }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        val size = min(width, height).toFloat()
        val originX = (width - size) / 2f
        val originY = (height - size) / 2f
        val centerX = originX + size / 2f
        val centerY = originY + size / 2f
        val stroke = size * 0.052f
        val accent = accentColor()

        canvas.drawCircle(centerX, centerY, size * 0.405f, innerPaint)

        haloPaint.strokeWidth = size * 0.007f
        canvas.drawCircle(centerX, centerY, size * 0.455f, haloPaint)

        trackPaint.strokeWidth = stroke
        progressHaloPaint.strokeWidth = stroke * 1.28f
        progressHaloPaint.color = accent
        progressPaint.strokeWidth = stroke
        progressPaint.color = accent
        batteryPaint.strokeWidth = size * 0.018f

        val inset = stroke * 1.48f
        val arc = RectF(
            originX + inset,
            originY + inset,
            originX + size - inset,
            originY + size - inset
        )
        val start = 128f
        val sweepMax = 284f
        val safePercent = percent ?: 0
        val sweep = sweepMax * (safePercent / 100f)
        canvas.drawArc(arc, start, sweepMax, false, trackPaint)
        canvas.drawArc(arc, start, sweep, false, progressHaloPaint)
        canvas.drawArc(arc, start, sweep, false, progressPaint)

        drawBatteryIcon(canvas, size, originX, originY, accent)

        textPaint.textSize = size * 0.172f
        canvas.drawText(
            percent?.let { "$it%" } ?: "—",
            centerX,
            originY + size * 0.705f,
            textPaint
        )

        secondaryText?.let { label ->
            secondaryTextPaint.textSize = size * 0.055f
            canvas.drawText(
                fitSecondaryText(label, size * 0.69f, secondaryTextPaint),
                centerX,
                originY + size * 0.792f,
                secondaryTextPaint
            )
        }
    }

    private fun fitSecondaryText(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        var shortened = text
        val suffix = "…"
        while (shortened.length > 1 && paint.measureText(shortened + suffix) > maxWidth) {
            shortened = shortened.dropLast(1)
        }
        return shortened.trimEnd() + suffix
    }

    private fun drawBatteryIcon(
        canvas: Canvas,
        size: Float,
        originX: Float,
        originY: Float,
        accent: Int
    ) {
        val centerX = originX + size / 2f
        val top = originY + size * 0.275f
        val batteryWidth = size * 0.205f
        val batteryHeight = size * 0.215f
        val left = centerX - batteryWidth / 2f
        val right = centerX + batteryWidth / 2f
        val bottom = top + batteryHeight

        canvas.drawRoundRect(
            RectF(left, top, right, bottom),
            size * 0.024f,
            size * 0.024f,
            batteryPaint
        )

        val capWidth = batteryWidth * 0.40f
        val capHeight = size * 0.024f
        canvas.drawRoundRect(
            RectF(centerX - capWidth / 2f, top - capHeight, centerX + capWidth / 2f, top),
            size * 0.01f,
            size * 0.01f,
            batteryPaint
        )

        val innerPadding = size * 0.025f
        val innerHeight = (batteryHeight - innerPadding * 2f) * ((percent ?: 0) / 100f)
        if (innerHeight > 0f) {
            fillPaint.color = accent
            canvas.drawRoundRect(
                RectF(
                    left + innerPadding,
                    bottom - innerPadding - innerHeight,
                    right - innerPadding,
                    bottom - innerPadding
                ),
                size * 0.010f,
                size * 0.010f,
                fillPaint
            )
        }
    }

    private fun accentColor(): Int = if (charging) {
        Color.rgb(33, 229, 109)
    } else {
        Color.rgb(34, 184, 255)
    }
}
