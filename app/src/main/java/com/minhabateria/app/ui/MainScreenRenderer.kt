package com.minhabateria.app.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.calculation.BatteryRateEstimator
import com.minhabateria.app.charging.ChargeTimeEstimator
import com.minhabateria.app.charging.ChargeTimeFormatter
import com.minhabateria.app.discharge.ActiveDischarge
import com.minhabateria.app.discharge.DischargeFormatter
import com.minhabateria.app.measurement.MeasurementCatalog
import com.minhabateria.app.source.EnergySourceProfile
import com.minhabateria.app.source.NominalPowerComparison
import com.minhabateria.app.session.SessionFormatter
import com.minhabateria.app.utils.BatteryFormatter
import com.minhabateria.app.utils.TimeFormatter

class MainScreenRenderer(private val activity: Activity) {
    private val gauge = activity.findViewById<BatteryGaugeView>(R.id.batteryGauge)
    private val status = activity.findViewById<TextView>(R.id.statusText)
    private val profileSource = activity.findViewById<TextView>(R.id.profileSourceValue)
    private val detectedSource = activity.findViewById<TextView>(R.id.detectedSourceValue)
    private val voltage = activity.findViewById<TextView>(R.id.voltageValue)
    private val currentLabel = activity.findViewById<TextView>(R.id.currentLabel)
    private val current = activity.findViewById<TextView>(R.id.currentValue)
    private val currentOrigin = activity.findViewById<TextView>(R.id.currentOrigin)
    private val powerLabel = activity.findViewById<TextView>(R.id.powerLabel)
    private val power = activity.findViewById<TextView>(R.id.powerValue)
    private val temperature = activity.findViewById<TextView>(R.id.temperatureValue)
    private val elapsed = activity.findViewById<TextView>(R.id.elapsedValue)
    private val energy = activity.findViewById<TextView>(R.id.energyValue)
    private val charge = activity.findViewById<TextView>(R.id.chargeValue)
    private val powerOrigin = activity.findViewById<TextView>(R.id.powerOrigin)
    private var sourceProfile: EnergySourceProfile? = null

    init {
        activity.findViewById<TextView>(R.id.voltageOrigin).text = MeasurementCatalog.voltage.origin.label
        currentOrigin.text = MeasurementCatalog.current.origin.label
        powerOrigin.text = MeasurementCatalog.power.origin.label
        activity.findViewById<TextView>(R.id.temperatureOrigin).text = MeasurementCatalog.temperature.origin.label
    }

    fun render(
        info: BatteryInfo,
        session: ChargingSession.Snapshot,
        discharge: ActiveDischarge? = null
    ) {
        gauge.setBattery(info.percent, info.isCharging == true)
        val chargeEstimate = ChargeTimeEstimator.estimate(info, session)
        renderStatus(info, session.reachedFull, chargeEstimate, discharge)
        detectedSource.text = BatteryFormatter.source(info.source)
        renderMetric(voltage, BatteryFormatter.voltage(info.voltageMv), info.voltageMv != null, R.color.accent_green)
        renderCurrent(info)
        renderChargeOrDischargeSpeed(info, session, discharge)
        renderMetric(temperature, BatteryFormatter.temperature(info.temperatureC), info.temperatureC != null, R.color.accent_orange)
        elapsed.text = TimeFormatter.elapsed(session.elapsedMs)
        renderMetric(energy, SessionFormatter.energy(session.energyWh), session.energyWh != null, R.color.text_primary)
        renderMetric(charge, SessionFormatter.charge(session.chargeMah), session.chargeMah != null, R.color.text_primary)
    }

    fun renderSourceProfile(profile: EnergySourceProfile?) {
        sourceProfile = profile
        profileSource.text = profile?.name ?: "Não configurado"
        profileSource.setTextColor(
            activity.getColor(if (profile == null) R.color.value_unavailable else R.color.accent_blue)
        )
    }

    private fun renderCurrent(info: BatteryInfo) {
        currentLabel.text = when {
            info.isCharging == true -> "Corrente de carga"
            info.isPlugged == false -> "Corrente de descarga"
            else -> "Corrente"
        }
        val currentMa = info.currentMa
        current.text = BatteryFormatter.signedCurrent(currentMa)
        current.setTextColor(
            activity.getColor(
                when {
                    currentMa == null -> R.color.value_unavailable
                    currentMa < 0.0 -> R.color.accent_red
                    currentMa > 0.0 -> R.color.accent_green
                    else -> R.color.text_secondary
                }
            )
        )
        currentOrigin.text = when {
            currentMa == null -> MeasurementCatalog.current.origin.label
            info.isPlugged == false && currentMa > 0.0 -> "SISTEMA • DIVERGENTE"
            info.isCharging == true && currentMa < 0.0 -> "SISTEMA • DIVERGENTE"
            else -> "SISTEMA • BRUTO"
        }
    }

    private fun renderChargeOrDischargeSpeed(
        info: BatteryInfo,
        session: ChargingSession.Snapshot,
        discharge: ActiveDischarge?
    ) {
        if (info.isPlugged == false) {
            powerLabel.text = "Velocidade de descarga"
            val rate = BatteryRateEstimator.dischargePercentPerHour(discharge)
            renderMetric(
                power,
                if (rate == null) "Calculando…" else DischargeFormatter.rate(rate),
                rate != null,
                R.color.accent_orange
            )
            powerOrigin.text = if (rate == null) "AGUARDANDO AMOSTRA REAL" else "MEDIDA NA SESSÃO"
            return
        }

        powerLabel.text = "Velocidade de carga"
        renderMetric(power, BatteryFormatter.power(info.powerW), info.powerW != null, R.color.text_primary)
        powerOrigin.text = NominalPowerComparison.compact(
            info.powerW, session.peakPowerW, sourceProfile?.nominalPowerW
        ) ?: MeasurementCatalog.power.origin.label
    }

    private fun renderMetric(view: TextView, text: String, available: Boolean, colorRes: Int) {
        view.text = text
        view.setTextColor(activity.getColor(if (available) colorRes else R.color.value_unavailable))
    }

    private fun renderStatus(
        info: BatteryInfo,
        reachedFull: Boolean,
        estimate: ChargeTimeEstimator.Estimate?,
        discharge: ActiveDischarge?
    ) {
        val full = info.percent == 100 || reachedFull
        val dischargeRemainingMs = BatteryRateEstimator.dischargeRemainingMs(discharge)
        status.text = when {
            full -> "100% • carga completa"
            info.isCharging == true && estimate != null -> ChargeTimeFormatter.mainLabel(estimate)
            info.isCharging == true -> "Calculando tempo até 100%…"
            info.isPlugged == true -> "Conectado • carga pausada"
            info.isPlugged == false && dischargeRemainingMs != null ->
                "Na bateria • ~${ChargeTimeFormatter.compact(dischargeRemainingMs)} restantes"
            info.isPlugged == false -> "Na bateria • Calculando autonomia…"
            info.percent != null -> "Bateria • ${info.percent}%"
            else -> "Status da bateria indisponível"
        }
        val active = info.isCharging == true || full
        status.setTextColor(if (active) Color.rgb(6, 35, 20) else Color.WHITE)
        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (active) R.drawable.ic_status_charging else R.drawable.ic_status_idle,
            0,
            0,
            0
        )
        status.background = statusBackground(if (active) true else info.isCharging)
        status.contentDescription = when {
            active && estimate != null -> "${status.text}. ${ChargeTimeFormatter.sourceLabel(estimate)}"
            info.isPlugged == false && dischargeRemainingMs != null ->
                "${status.text}. Estimativa calculada pelo ritmo real da sessão de descarga."
            else -> status.text
        }
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
