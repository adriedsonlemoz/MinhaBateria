package com.minhabateria.app.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.battery.ChargingSource
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

    private val voltageCard = activity.findViewById<View>(R.id.voltageCard)
    private val currentCard = activity.findViewById<View>(R.id.currentCard)
    private val powerCard = activity.findViewById<View>(R.id.powerCard)
    private val temperatureCard = activity.findViewById<View>(R.id.temperatureCard)
    private val voltage = activity.findViewById<TextView>(R.id.voltageValue)
    private val currentLabel = activity.findViewById<TextView>(R.id.currentLabel)
    private val current = activity.findViewById<TextView>(R.id.currentValue)
    private val currentOrigin = activity.findViewById<TextView>(R.id.currentOrigin)
    private val powerLabel = activity.findViewById<TextView>(R.id.powerLabel)
    private val power = activity.findViewById<TextView>(R.id.powerValue)
    private val temperature = activity.findViewById<TextView>(R.id.temperatureValue)
    private val powerOrigin = activity.findViewById<TextView>(R.id.powerOrigin)

    private val elapsedLabel = activity.findViewById<TextView>(R.id.elapsedLabel)
    private val energyLabel = activity.findViewById<TextView>(R.id.energyLabel)
    private val chargeLabel = activity.findViewById<TextView>(R.id.chargeLabel)
    private val elapsed = activity.findViewById<TextView>(R.id.elapsedValue)
    private val energy = activity.findViewById<TextView>(R.id.energyValue)
    private val charge = activity.findViewById<TextView>(R.id.chargeValue)

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
        val chargeEstimate = ChargeTimeEstimator.estimate(info, session)
        renderGauge(info, session.reachedFull, chargeEstimate, discharge)
        renderStatus(info, session.reachedFull, chargeEstimate, discharge)
        renderDetectedSource(info)

        renderMetric(voltage, BatteryFormatter.voltage(info.voltageMv), info.voltageMv != null, R.color.accent_green)
        setCardAvailability(voltageCard, info.voltageMv != null)
        renderCurrent(info)
        renderChargeOrDischargeSpeed(info, session, discharge)
        renderMetric(
            temperature,
            BatteryFormatter.temperature(info.temperatureC),
            info.temperatureC != null,
            R.color.accent_orange
        )
        setCardAvailability(temperatureCard, info.temperatureC != null)
        renderSessionSummary(info, session, discharge)
    }

    fun renderSourceProfile(profile: EnergySourceProfile?) {
        sourceProfile = profile
        profileSource.text = profile?.name ?: "Não configurado"
        profileSource.setTextColor(
            activity.getColor(if (profile == null) R.color.value_unavailable else R.color.text_secondary)
        )
    }

    private fun renderDetectedSource(info: BatteryInfo) {
        detectedSource.text = BatteryFormatter.source(info.source)
        detectedSource.setTextColor(
            activity.getColor(if (info.source == ChargingSource.UNKNOWN) R.color.value_unavailable else R.color.accent_blue)
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

        val divergent = currentMa != null && (
            (info.isPlugged == false && currentMa > 0.0) ||
                (info.isCharging == true && currentMa < 0.0)
            )
        currentOrigin.text = when {
            currentMa == null -> MeasurementCatalog.current.origin.label
            divergent -> "SISTEMA • DIVERGENTE"
            else -> "SISTEMA • BRUTO"
        }
        currentOrigin.setTextColor(activity.getColor(if (divergent) R.color.accent_red else R.color.text_muted))
        setCardAvailability(currentCard, currentMa != null)
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
            powerOrigin.text = if (rate == null) "AGUARDANDO" else "SESSÃO REAL"
            setCardAvailability(powerCard, rate != null)
            return
        }

        powerLabel.text = "Velocidade de carga"
        renderMetric(power, BatteryFormatter.power(info.powerW), info.powerW != null, R.color.text_primary)
        powerOrigin.text = NominalPowerComparison.compact(
            info.powerW, session.peakPowerW, sourceProfile?.nominalPowerW
        ) ?: MeasurementCatalog.power.origin.label
        setCardAvailability(powerCard, info.powerW != null)
    }

    private fun renderSessionSummary(
        info: BatteryInfo,
        session: ChargingSession.Snapshot,
        discharge: ActiveDischarge?
    ) {
        if (info.isPlugged == false) {
            elapsedLabel.text = "Tempo na bateria"
            energyLabel.text = "Queda"
            chargeLabel.text = "Média"

            val hasDischarge = discharge != null
            renderMetric(
                elapsed,
                discharge?.let { TimeFormatter.elapsed(it.durationMs) } ?: "—",
                hasDischarge,
                R.color.accent_blue
            )
            renderMetric(
                energy,
                discharge?.let { "${it.dropPercent}%" } ?: "—",
                hasDischarge,
                if ((discharge?.dropPercent ?: 0) > 0) R.color.accent_red else R.color.text_primary
            )
            val rate = BatteryRateEstimator.dischargePercentPerHour(discharge)
            renderMetric(
                charge,
                if (rate == null) "Calculando…" else DischargeFormatter.rate(rate),
                rate != null,
                R.color.accent_orange
            )
            return
        }

        elapsedLabel.text = "Tempo"
        energyLabel.text = "Energia"
        chargeLabel.text = "Carga"
        renderMetric(
            elapsed,
            session.elapsedMs?.let(TimeFormatter::elapsed) ?: "—",
            session.elapsedMs != null,
            R.color.accent_blue
        )
        renderMetric(
            energy,
            session.energyWh?.let(SessionFormatter::energy) ?: "—",
            session.energyWh != null,
            R.color.text_primary
        )
        renderMetric(
            charge,
            session.chargeMah?.let(SessionFormatter::charge) ?: "—",
            session.chargeMah != null,
            R.color.text_primary
        )
    }

    private fun renderMetric(view: TextView, text: String, available: Boolean, colorRes: Int) {
        view.text = text
        view.setTextColor(activity.getColor(if (available) colorRes else R.color.value_unavailable))
    }

    private fun setCardAvailability(card: View, available: Boolean) {
        card.alpha = if (available) 1f else 0.68f
    }

    private fun renderGauge(
        info: BatteryInfo,
        reachedFull: Boolean,
        estimate: ChargeTimeEstimator.Estimate?,
        discharge: ActiveDischarge?
    ) {
        val full = info.percent == 100 || reachedFull
        val dischargeRemainingMs = BatteryRateEstimator.dischargeRemainingMs(discharge)
        val secondary = when {
            full -> "Carga completa"
            info.isCharging == true && estimate != null ->
                "~${ChargeTimeFormatter.compact(estimate.remainingMs)} até 100%"
            info.isCharging == true -> "Calculando tempo…"
            info.isPlugged == true -> "Carga pausada"
            info.isPlugged == false && dischargeRemainingMs != null ->
                "~${ChargeTimeFormatter.compact(dischargeRemainingMs)} restantes"
            info.isPlugged == false -> "Calculando autonomia…"
            else -> null
        }
        gauge.setBattery(info.percent, info.isCharging == true, secondary)
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
            full -> "Carga completa"
            info.isCharging == true -> "Carregando"
            info.isPlugged == true -> "Conectado • carga pausada"
            info.isPlugged == false -> "Na bateria"
            info.percent != null -> "Bateria"
            else -> "Status da bateria"
        }
        val active = info.isCharging == true || full
        status.setTextColor(activity.getColor(if (active) R.color.accent_green else R.color.text_primary))
        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (active) R.drawable.ic_status_charging else R.drawable.ic_status_idle,
            0,
            0,
            0
        )
        status.background = statusBackground(if (active) true else info.isCharging)
        status.contentDescription = when {
            full -> "Carga completa. Bateria em 100 por cento."
            active && estimate != null ->
                "Carregando. Aproximadamente ${ChargeTimeFormatter.compact(estimate.remainingMs)} até 100 por cento. ${ChargeTimeFormatter.sourceLabel(estimate)}"
            info.isCharging == true -> "Carregando. Calculando tempo até 100 por cento."
            info.isPlugged == false && dischargeRemainingMs != null ->
                "Na bateria. Aproximadamente ${ChargeTimeFormatter.compact(dischargeRemainingMs)} restantes. Estimativa calculada pelo ritmo real da sessão de descarga."
            info.isPlugged == false -> "Na bateria. Calculando autonomia."
            else -> status.text
        }
    }

    private fun statusBackground(charging: Boolean?): GradientDrawable {
        val colors = if (charging == true) {
            intArrayOf(Color.rgb(16, 61, 42), Color.rgb(9, 39, 28))
        } else {
            intArrayOf(Color.rgb(31, 47, 61), Color.rgb(22, 35, 47))
        }
        return GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f
            setStroke(
                1,
                activity.getColor(if (charging == true) R.color.accent_green_dark else R.color.status_idle_border)
            )
        }
    }
}
