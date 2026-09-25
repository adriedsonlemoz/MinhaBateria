package com.minhabateria.app.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.source.EnergySourceProfile
import com.minhabateria.app.utils.BatteryFormatter
import com.minhabateria.app.utils.TimeFormatter

class MainScreenRenderer(private val activity: Activity) {
    private val gauge = activity.findViewById<BatteryGaugeView>(R.id.batteryGauge)
    private val status = activity.findViewById<TextView>(R.id.statusText)
    private val profileSource = activity.findViewById<TextView>(R.id.profileSourceValue)
    private val detectedSource = activity.findViewById<TextView>(R.id.detectedSourceValue)
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
        detectedSource.text = BatteryFormatter.source(info.source)
        renderMetric(voltage, BatteryFormatter.voltage(info.voltageMv), info.voltageMv != null, R.color.accent_green)
        renderMetric(current, BatteryFormatter.current(info.currentMa), info.currentMa != null, R.color.accent_green)
        renderMetric(power, BatteryFormatter.power(info.powerW), info.powerW != null, R.color.text_primary)
        renderMetric(temperature, BatteryFormatter.temperature(info.temperatureC), info.temperatureC != null, R.color.accent_orange)
        elapsed.text = TimeFormatter.elapsed(session.elapsedMs)
        renderMetric(peakCurrent, BatteryFormatter.current(session.peakCurrentMa), session.peakCurrentMa != null, R.color.text_primary)
        renderMetric(peakPower, BatteryFormatter.power(session.peakPowerW), session.peakPowerW != null, R.color.text_primary)
    }

    fun renderSourceProfile(profile: EnergySourceProfile?) {
        profileSource.text = profile?.name ?: "Não configurado"
        profileSource.setTextColor(
            activity.getColor(if (profile == null) R.color.value_unavailable else R.color.accent_blue)
        )
    }

    private fun renderMetric(view: TextView, text: String, available: Boolean, colorRes: Int) {
        view.text = text
        view.setTextColor(activity.getColor(if (available) colorRes else R.color.value_unavailable))
    }

    private fun renderStatus(charging: Boolean) {
        status.text = if (charging) "Carregando" else "Não está carregando"
        status.setTextColor(if (charging) Color.rgb(6, 35, 20) else Color.WHITE)
        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (charging) R.drawable.ic_status_charging else R.drawable.ic_status_idle,
            0,
            0,
            0
        )
        status.background = statusBackground(charging)
    }

    private fun statusBackground(charging: Boolean): GradientDrawable {
        val colors = if (charging) {
            intArrayOf(Color.rgb(41, 235, 113), Color.rgb(21, 196, 89))
        } else {
            intArrayOf(Color.rgb(46, 64, 81), Color.rgb(34, 48, 62))
        }
        return GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f
            setStroke(1, activity.getColor(if (charging) R.color.accent_green else R.color.status_idle_border))
        }
    }
}
