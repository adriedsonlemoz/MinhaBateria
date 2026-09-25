package com.minhabateria.app.ui

import android.app.Activity
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.session.SessionFormatter
import com.minhabateria.app.session.SessionInsightBuilder
import com.minhabateria.app.source.EnergySourceProfile
import com.minhabateria.app.source.NominalPowerComparison

class SessionScreenRenderer(private val activity: Activity) {
    private val sourceName = text(R.id.sessionSourceName)
    private val sourceReference = text(R.id.sessionSourceReference)
    private val insightState = text(R.id.sessionInsightState)
    private val insightHeadline = text(R.id.sessionInsightHeadline)
    private val insightPower = text(R.id.sessionInsightPower)
    private val insightStability = text(R.id.sessionInsightStability)
    private val insightDetail = text(R.id.sessionInsightDetail)
    private val elapsed = text(R.id.sessionElapsed)
    private val chargingTime = text(R.id.sessionChargingTime)
    private val interruptions = text(R.id.sessionInterruptions)
    private val energy = text(R.id.sessionEnergy)
    private val charge = text(R.id.sessionCharge)
    private val averagePower = text(R.id.sessionAveragePower)
    private val peakPower = text(R.id.sessionPeakPower)
    private val averageCurrent = text(R.id.sessionAverageCurrent)
    private val currentRange = text(R.id.sessionCurrentRange)
    private val averageVoltage = text(R.id.sessionAverageVoltage)
    private val voltageRange = text(R.id.sessionVoltageRange)
    private val averageTemperature = text(R.id.sessionAverageTemperature)
    private val maxTemperature = text(R.id.sessionMaxTemperature)
    private val batteryRange = text(R.id.sessionBatteryRange)
    private val batteryGain = text(R.id.sessionBatteryGain)
    private val simpleBattery = text(R.id.sessionSimpleBattery)
    private val simpleTime = text(R.id.sessionSimpleTime)
    private val simpleEnergy = text(R.id.sessionSimpleEnergy)
    private val simplePower = text(R.id.sessionSimplePower)
    private val simpleTemperature = text(R.id.sessionSimpleTemperature)
    private var sourceProfile: EnergySourceProfile? = null

    fun renderSource(profile: EnergySourceProfile?) {
        sourceProfile = profile
        sourceName.text = profile?.name ?: "Perfil não configurado"
        sourceName.setTextColor(activity.getColor(if (profile == null) R.color.value_unavailable else R.color.accent_blue))
        sourceReference.text = profile?.nominalPowerW?.let {
            "Referência nominal configurada: ${SessionFormatter.power(it)}"
        } ?: "Sem referência nominal configurada"
    }

    fun render(info: BatteryInfo?, snapshot: ChargingSession.Snapshot?) {
        renderInsight(info, snapshot)
        elapsed.text = SessionFormatter.duration(snapshot?.elapsedMs)
        chargingTime.text = SessionFormatter.duration(snapshot?.chargingTimeMs)
        interruptions.text = snapshot?.takeIf { it.elapsedMs != null }?.interruptions?.toString() ?: "—"
        energy.text = SessionFormatter.energy(snapshot?.energyWh)
        charge.text = SessionFormatter.charge(snapshot?.chargeMah)
        averagePower.text = SessionFormatter.power(snapshot?.averagePowerW)
        peakPower.text = SessionFormatter.power(snapshot?.peakPowerW)
        renderPowerReference(snapshot)
        averageCurrent.text = SessionFormatter.current(snapshot?.averageCurrentMa)
        currentRange.text = SessionFormatter.currentRange(snapshot?.minCurrentMa, snapshot?.maxCurrentMa)
        averageVoltage.text = SessionFormatter.voltage(snapshot?.averageVoltageV)
        voltageRange.text = SessionFormatter.voltageRange(snapshot?.minVoltageV, snapshot?.maxVoltageV)
        averageTemperature.text = SessionFormatter.temperature(snapshot?.averageTemperatureC)
        maxTemperature.text = SessionFormatter.temperature(snapshot?.maxTemperatureC)
        batteryRange.text = SessionFormatter.batteryRange(snapshot?.startPercent, snapshot?.currentPercent)
        batteryGain.text = SessionFormatter.gain(snapshot?.gainPercent)
        renderSimpleSummary(snapshot)
    }

    private fun renderSimpleSummary(snapshot: ChargingSession.Snapshot?) {
        val range = SessionFormatter.batteryRange(snapshot?.startPercent, snapshot?.currentPercent)
        val gain = snapshot?.gainPercent
        simpleBattery.text = if (range != "Indisponível" && gain != null) {
            "$range (${SessionFormatter.gain(gain)})"
        } else {
            range
        }
        simpleTime.text = SessionFormatter.duration(snapshot?.elapsedMs)
        simpleEnergy.text = SessionFormatter.energy(snapshot?.energyWh)
        simplePower.text = SessionFormatter.power(snapshot?.averagePowerW)
        simpleTemperature.text = SessionFormatter.temperature(snapshot?.maxTemperatureC)
    }

    private fun renderInsight(info: BatteryInfo?, snapshot: ChargingSession.Snapshot?) {
        val insight = SessionInsightBuilder.build(info, snapshot, sourceProfile?.nominalPowerW)
        insightState.text = insight.sessionState
        insightHeadline.text = insight.headline
        insightPower.text = insight.powerContext
        insightStability.text = insight.stability
        insightDetail.text = insight.detail
        insightState.setTextColor(
            activity.getColor(if (snapshot?.reachedFull == true) R.color.accent_green else R.color.text_primary)
        )
    }

    private fun renderPowerReference(snapshot: ChargingSession.Snapshot?) {
        val comparison = NominalPowerComparison.detailed(snapshot?.peakPowerW, sourceProfile?.nominalPowerW)
        sourceReference.text = when {
            comparison != null -> "Pico: ${SessionFormatter.power(snapshot?.peakPowerW)} • $comparison"
            sourceProfile?.nominalPowerW != null -> "Referência nominal configurada: ${SessionFormatter.power(sourceProfile?.nominalPowerW)}"
            else -> "Sem referência nominal configurada"
        }
    }

    private fun text(id: Int): TextView = activity.findViewById(id)
}
