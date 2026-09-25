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

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(45, 55, 64)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val batteryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = dp(4f)
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private var percent = 0
    private var charging = false

    fun setBattery(percent: Int, charging: Boolean) {
        this.percent = percent.coerceIn(0, 100)
        this.charging = charging
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        val stroke = size * 0.055f
        trackPaint.strokeWidth = stroke
        progressPaint.strokeWidth = stroke
        progressPaint.color = accentColor()

        val inset = stroke * 1.3f
        val arc = RectF(inset, inset, width - inset, height - inset)
        val sweepMax = 290f
        canvas.drawArc(arc, 125f, sweepMax, false, trackPaint)
        canvas.drawArc(arc, 125f, sweepMax * (percent / 100f), false, progressPaint)

        drawBatteryIcon(canvas, size)

        textPaint.textSize = size * 0.17f
        canvas.drawText("$percent%", width / 2f, height * 0.77f, textPaint)
    }

    private fun drawBatteryIcon(canvas: Canvas, size: Float) {
        val centerX = width / 2f
        val top = height * 0.29f
        val batteryWidth = size * 0.20f
        val batteryHeight = size * 0.24f
        val left = centerX - batteryWidth / 2f
        val right = centerX + batteryWidth / 2f
        val bottom = top + batteryHeight

        val body = RectF(left, top, right, bottom)
        canvas.drawRoundRect(body, dp(7f), dp(7f), batteryPaint)

        val capWidth = batteryWidth * 0.38f
        val capHeight = size * 0.025f
        val cap = RectF(
            centerX - capWidth / 2f,
            top - capHeight,
            centerX + capWidth / 2f,
            top
        )
        canvas.drawRoundRect(cap, dp(3f), dp(3f), batteryPaint)

        val innerPadding = dp(7f)
        val innerHeight = (batteryHeight - innerPadding * 2f) * (percent / 100f)
        if (innerHeight > 0f) {
            fillPaint.color = accentColor()
            val fill = RectF(
                left + innerPadding,
                bottom - innerPadding - innerHeight,
                right - innerPadding,
                bottom - innerPadding
            )
            canvas.drawRoundRect(fill, dp(3f), dp(3f), fillPaint)
        }
    }

    private fun accentColor(): Int = if (charging) {
        Color.rgb(42, 229, 79)
    } else {
        Color.rgb(69, 183, 255)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
