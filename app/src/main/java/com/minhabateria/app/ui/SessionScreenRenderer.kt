package com.minhabateria.app.ui

import android.app.Activity
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.session.SessionFormatter
import com.minhabateria.app.source.EnergySourceProfile
import com.minhabateria.app.source.NominalPowerComparison

class SessionScreenRenderer(private val activity: Activity) {
    private val sourceName = text(R.id.sessionSourceName)
    private val sourceReference = text(R.id.sessionSourceReference)
    private var sourceProfile: EnergySourceProfile? = null
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

    fun renderSource(profile: EnergySourceProfile?) {
        sourceProfile = profile
        sourceName.text = profile?.name ?: "Perfil não configurado"
        sourceName.setTextColor(activity.getColor(if (profile == null) R.color.value_unavailable else R.color.accent_blue))
        sourceReference.text = profile?.nominalPowerW?.let { "Referência nominal configurada: ${SessionFormatter.power(it)}" }
            ?: "Sem referência nominal configurada"
    }

    fun render(snapshot: ChargingSession.Snapshot?) {
        elapsed.text = SessionFormatter.duration(snapshot?.elapsedMs)
        chargingTime.text = SessionFormatter.duration(snapshot?.chargingTimeMs)
        interruptions.text = snapshot?.interruptions?.toString() ?: "—"
        energy.text = SessionFormatter.energy(snapshot?.energyWh)
        charge.text = SessionFormatter.charge(snapshot?.chargeMah)
        averagePower.text = SessionFormatter.power(snapshot?.averagePowerW)
        peakPower.text = SessionFormatter.power(snapshot?.peakPowerW)
        val comparison = NominalPowerComparison.detailed(snapshot?.peakPowerW, sourceProfile?.nominalPowerW)
        sourceReference.text = when {
            comparison != null -> "Pico: ${SessionFormatter.power(snapshot?.peakPowerW)} • $comparison"
            sourceProfile?.nominalPowerW != null -> "Referência nominal configurada: ${SessionFormatter.power(sourceProfile?.nominalPowerW)}"
            else -> "Sem referência nominal configurada"
        }
        averageCurrent.text = SessionFormatter.current(snapshot?.averageCurrentMa)
        currentRange.text = SessionFormatter.currentRange(snapshot?.minCurrentMa, snapshot?.maxCurrentMa)
        averageVoltage.text = SessionFormatter.voltage(snapshot?.averageVoltageV)
        voltageRange.text = SessionFormatter.voltageRange(snapshot?.minVoltageV, snapshot?.maxVoltageV)
        averageTemperature.text = SessionFormatter.temperature(snapshot?.averageTemperatureC)
        maxTemperature.text = SessionFormatter.temperature(snapshot?.maxTemperatureC)
        batteryRange.text = SessionFormatter.batteryRange(snapshot?.startPercent, snapshot?.currentPercent)
        batteryGain.text = SessionFormatter.gain(snapshot?.gainPercent)
    }

    private fun text(id: Int): TextView = activity.findViewById(id)
}
