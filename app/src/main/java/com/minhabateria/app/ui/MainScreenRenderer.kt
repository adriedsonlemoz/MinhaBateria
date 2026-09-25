package com.minhabateria.app.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.charging.ChargeTimeEstimator
import com.minhabateria.app.charging.ChargeTimeFormatter
import com.minhabateria.app.measurement.MeasurementCatalog
import com.minhabateria.app.source.EnergySourceProfile
import com.minhabateria.app.source.NominalPowerComparison
import com.minhabateria.app.session.SessionFormatter
import com.minhabateria.app.utils.BatteryFormatter
import com.minhabateria.app.utils.TimeFormatter

class MainScreenRenderer(private val activity: Activity) {
    private val gauge = activity.findViewById<BatteryGaugeView>(R.id.batteryGauge)
    private val status = activity.findViewById<TextView>(R.id.statusText)
    private val remainingTime = activity.findViewById<TextView>(R.id.remainingTimeText)
    private val profileSource = activity.findViewById<TextView>(R.id.profileSourceValue)
    private val detectedSource = activity.findViewById<TextView>(R.id.detectedSourceValue)
    private val voltage = activity.findViewById<TextView>(R.id.voltageValue)
    private val current = activity.findViewById<TextView>(R.id.currentValue)
    private val power = activity.findViewById<TextView>(R.id.powerValue)
    private val temperature = activity.findViewById<TextView>(R.id.temperatureValue)
    private val elapsed = activity.findViewById<TextView>(R.id.elapsedValue)
    private val energy = activity.findViewById<TextView>(R.id.energyValue)
    private val charge = activity.findViewById<TextView>(R.id.chargeValue)
    private val powerOrigin = activity.findViewById<TextView>(R.id.powerOrigin)
    private var sourceProfile: EnergySourceProfile? = null

    init {
        activity.findViewById<TextView>(R.id.voltageOrigin).text = MeasurementCatalog.voltage.origin.label
        activity.findViewById<TextView>(R.id.currentOrigin).text = MeasurementCatalog.current.origin.label
        powerOrigin.text = MeasurementCatalog.power.origin.label
        activity.findViewById<TextView>(R.id.temperatureOrigin).text = MeasurementCatalog.temperature.origin.label
    }

    fun render(info: BatteryInfo, session: ChargingSession.Snapshot) {
        gauge.setBattery(info.percent, info.isCharging == true)
        renderStatus(info.isCharging, info.percent, session.reachedFull)
        renderRemainingTime(info, session)
        detectedSource.text = BatteryFormatter.source(info.source)
        renderMetric(voltage, BatteryFormatter.voltage(info.voltageMv), info.voltageMv != null, R.color.accent_green)
        renderMetric(current, BatteryFormatter.current(info.currentMa), info.currentMa != null, R.color.accent_green)
        renderMetric(power, BatteryFormatter.power(info.powerW), info.powerW != null, R.color.text_primary)
        powerOrigin.text = NominalPowerComparison.compact(
            info.powerW, session.peakPowerW, sourceProfile?.nominalPowerW
        ) ?: MeasurementCatalog.power.origin.label
        renderMetric(temperature, BatteryFormatter.temperature(info.temperatureC), info.temperatureC != null, R.color.accent_orange)
        elapsed.text = TimeFormatter.elapsed(session.elapsedMs)
        renderMetric(energy, SessionFormatter.energy(session.energyWh), session.energyWh != null, R.color.text_primary)
        renderMetric(charge, SessionFormatter.charge(session.chargeMah), session.chargeMah != null, R.color.text_primary)
    }

    private fun renderRemainingTime(info: BatteryInfo, session: ChargingSession.Snapshot) {
        val estimate = ChargeTimeEstimator.estimate(info, session)
        when {
            info.percent == 100 && info.isPlugged == true -> {
                showRemaining("Carga completa")
            }
            info.isCharging == true && estimate != null -> {
                showRemaining(ChargeTimeFormatter.mainLabel(estimate) ?: "Calculando tempo restante…")
            }
            info.isCharging == true -> {
                showRemaining("Calculando tempo restante…")
            }
            info.isPlugged == true -> {
                showRemaining("Tempo restante indisponível")
            }
            else -> hideRemaining()
        }
    }

    private fun showRemaining(label: String) {
        remainingTime.text = label
        remainingTime.visibility = View.VISIBLE
    }

    private fun hideRemaining() {
        remainingTime.visibility = View.GONE
    }

    fun renderSourceProfile(profile: EnergySourceProfile?) {
        sourceProfile = profile
        profileSource.text = profile?.name ?: "Não configurado"
        profileSource.setTextColor(
            activity.getColor(if (profile == null) R.color.value_unavailable else R.color.accent_blue)
        )
    }

    private fun renderMetric(view: TextView, text: String, available: Boolean, colorRes: Int) {
        view.text = text
        view.setTextColor(activity.getColor(if (available) colorRes else R.color.value_unavailable))
    }

    private fun renderStatus(charging: Boolean?, percent: Int?, reachedFull: Boolean) {
        val full = percent == 100 || reachedFull
        status.text = when {
            full -> "Carga completa"
            charging == true -> "Carregando"
            charging == false -> "Não está carregando"
            else -> "Status indisponível"
        }
        val active = charging == true || full
        status.setTextColor(if (active) Color.rgb(6, 35, 20) else Color.WHITE)
        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (active) R.drawable.ic_status_charging else R.drawable.ic_status_idle,
            0,
            0,
            0
        )
        status.background = statusBackground(if (active) true else charging)
    }

    private fun statusBackground(charging: Boolean?): GradientDrawable {
        val colors = if (charging == true) {
            intArrayOf(Color.rgb(41, 235, 113), Color.rgb(21, 196, 89))
        } else {
            intArrayOf(Color.rgb(46, 64, 81), Color.rgb(34, 48, 62))
        }
        return GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f
            setStroke(1, activity.getColor(if (charging == true) R.color.accent_green else R.color.status_idle_border))
        }
    }
}
