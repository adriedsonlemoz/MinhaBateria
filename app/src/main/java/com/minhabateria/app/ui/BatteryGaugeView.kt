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
        if (width <= 0 || height <= 0) return

        val size = min(width, height).toFloat()
        val originX = (width - size) / 2f
        val originY = (height - size) / 2f
        val stroke = size * 0.055f

        trackPaint.strokeWidth = stroke
        progressPaint.strokeWidth = stroke
        progressPaint.color = accentColor()
        batteryPaint.strokeWidth = size * 0.018f

        val inset = stroke * 1.3f
        val arc = RectF(
            originX + inset,
            originY + inset,
            originX + size - inset,
            originY + size - inset
        )
        val sweepMax = 290f
        canvas.drawArc(arc, 125f, sweepMax, false, trackPaint)
        canvas.drawArc(arc, 125f, sweepMax * (percent / 100f), false, progressPaint)

        drawBatteryIcon(canvas, size, originX, originY)

        textPaint.textSize = size * 0.17f
        canvas.drawText(
            "$percent%",
            originX + size / 2f,
            originY + size * 0.77f,
            textPaint
        )
    }

    private fun drawBatteryIcon(
        canvas: Canvas,
        size: Float,
        originX: Float,
        originY: Float
    ) {
        val centerX = originX + size / 2f
        val top = originY + size * 0.29f
        val batteryWidth = size * 0.20f
        val batteryHeight = size * 0.24f
        val left = centerX - batteryWidth / 2f
        val right = centerX + batteryWidth / 2f
        val bottom = top + batteryHeight

        val body = RectF(left, top, right, bottom)
        canvas.drawRoundRect(body, size * 0.025f, size * 0.025f, batteryPaint)

        val capWidth = batteryWidth * 0.38f
        val capHeight = size * 0.025f
        val cap = RectF(
            centerX - capWidth / 2f,
            top - capHeight,
            centerX + capWidth / 2f,
            top
        )
        canvas.drawRoundRect(cap, size * 0.01f, size * 0.01f, batteryPaint)

        val innerPadding = size * 0.025f
        val innerHeight = (batteryHeight - innerPadding * 2f) * (percent / 100f)
        if (innerHeight > 0f) {
            fillPaint.color = accentColor()
            val fill = RectF(
                left + innerPadding,
                bottom - innerPadding - innerHeight,
                right - innerPadding,
                bottom - innerPadding
            )
            canvas.drawRoundRect(fill, size * 0.01f, size * 0.01f, fillPaint)
        }
    }

    private fun accentColor(): Int = if (charging) {
        Color.rgb(42, 229, 79)
    } else {
        Color.rgb(69, 183, 255)
    }
}
