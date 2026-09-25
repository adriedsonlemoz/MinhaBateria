package com.minhabateria.app.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.utils.BatteryFormatter
import com.minhabateria.app.utils.TimeFormatter

class MainScreenRenderer(activity: Activity) {
    private val gauge = activity.findViewById<BatteryGaugeView>(R.id.batteryGauge)
    private val status = activity.findViewById<TextView>(R.id.statusText)
    private val source = activity.findViewById<TextView>(R.id.sourceValue)
    private val voltage = activity.findViewById<TextView>(R.id.voltageValue)
    private val current = activity.findViewById<TextView>(R.id.currentValue)
    private val power = activity.findViewById<TextView>(R.id.powerValue)
    private val temperature = activity.findViewById<TextView>(R.id.temperatureValue)
    private val elapsed = activity.findViewById<TextView>(R.id.elapsedValue)
    private val peakCurrent = activity.findViewById<TextView>(R.id.peakCurrentValue)
    private val peakPower = activity.findViewById<TextView>(R.id.peakPowerValue)

    fun render(info: BatteryInfo, session: ChargingSession.Snapshot) {
        gauge.setBattery(info.percent, info.isCharging)
        renderStatus(info.isCharging)

        source.text = BatteryFormatter.source(info.source)
        voltage.text = BatteryFormatter.voltage(info.voltageMv)
        current.text = BatteryFormatter.current(info.currentMa)
        power.text = BatteryFormatter.power(info.powerW)
        temperature.text = BatteryFormatter.temperature(info.temperatureC)
        elapsed.text = TimeFormatter.elapsed(session.elapsedMs)
        peakCurrent.text = BatteryFormatter.current(session.peakCurrentMa)
        peakPower.text = BatteryFormatter.power(session.peakPowerW)
    }

    private fun renderStatus(charging: Boolean) {
        status.text = if (charging) "⚡ Carregando" else "Não está carregando"
        status.setTextColor(if (charging) Color.BLACK else Color.WHITE)
        status.background = roundedBackground(
            if (charging) Color.rgb(42, 229, 79) else Color.rgb(47, 58, 67)
        )
    }

    private fun roundedBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f
            setColor(color)
        }
    }
}
