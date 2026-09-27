package com.minhabateria.app.ui

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.interpretation.ChargeConditionInterpreter
import com.minhabateria.app.session.SessionFormatter
import com.minhabateria.app.session.SessionInsightBuilder
import com.minhabateria.app.source.EnergySourceProfile
import kotlin.math.roundToInt

class SessionScreenRenderer(private val activity: Activity) {
    private val sourceName = text(R.id.sessionSourceName)
    private val sourceReference = text(R.id.sessionSourceReference)
    private val insightState = text(R.id.sessionInsightState)
    private val insightHeadline = text(R.id.sessionInsightHeadline)
    private val insightPower = text(R.id.sessionInsightPower)
    private val insightStability = text(R.id.sessionInsightStability)
    private val insightDetail = text(R.id.sessionInsightDetail)
    private val statusChip = text(R.id.sessionStatusChip)
    private val guideCard = activity.findViewById<View>(R.id.sessionGuideCard)
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
    private val sampleCount = text(R.id.sessionSampleCount)
    private val simpleBattery = text(R.id.sessionSimpleBattery)
    private val simpleTime = text(R.id.sessionSimpleTime)
    private val simpleEnergy = text(R.id.sessionSimpleEnergy)
    private val simplePower = text(R.id.sessionSimplePower)
    private val simpleTemperature = text(R.id.sessionSimpleTemperature)
    private var sourceProfile: EnergySourceProfile? = null

    fun renderSource(profile: EnergySourceProfile?) {
        sourceProfile = profile
        sourceName.text = profile?.name ?: "Perfil não configurado"
        sourceName.setTextColor(activity.getColor(if (profile == null) R.color.value_unavailable else R.color.text_primary))
        sourceReference.text = profile?.nominalPowerW?.let {
            "Referência configurada: ${SessionFormatter.power(it)} • toque para editar"
        } ?: "Sem referência configurada • toque para informar a fonte"
    }

    fun render(info: BatteryInfo?, snapshot: ChargingSession.Snapshot?) {
        renderInsight(info, snapshot)
        renderSessionState(info, snapshot)
        elapsed.text = SessionFormatter.duration(snapshot?.elapsedMs)
        chargingTime.text = SessionFormatter.duration(snapshot?.chargingTimeMs)
        interruptions.text = snapshot?.takeIf { it.elapsedMs != null }?.interruptions?.toString() ?: "—"
        energy.text = SessionFormatter.energy(snapshot?.energyWh)
        charge.text = SessionFormatter.charge(snapshot?.chargeMah)
        averagePower.text = SessionFormatter.power(snapshot?.averagePowerW)
        peakPower.text = SessionFormatter.power(snapshot?.peakPowerW)
        averageCurrent.text = SessionFormatter.current(snapshot?.averageCurrentMa)
        currentRange.text = SessionFormatter.currentRange(snapshot?.minCurrentMa, snapshot?.maxCurrentMa)
        averageVoltage.text = SessionFormatter.voltage(snapshot?.averageVoltageV)
        voltageRange.text = SessionFormatter.voltageRange(snapshot?.minVoltageV, snapshot?.maxVoltageV)
        averageTemperature.text = SessionFormatter.temperature(snapshot?.averageTemperatureC)
        maxTemperature.text = SessionFormatter.temperature(snapshot?.maxTemperatureC)
        batteryRange.text = SessionFormatter.batteryRange(snapshot?.startPercent, snapshot?.currentPercent)
        batteryGain.text = SessionFormatter.gain(snapshot?.gainPercent)
        sampleCount.text = snapshot?.takeIf { it.elapsedMs != null }?.sampleCount?.toString() ?: "—"
        renderPowerReference(snapshot)
        renderSimpleSummary(snapshot)
    }

    private fun renderSimpleSummary(snapshot: ChargingSession.Snapshot?) {
        val start = snapshot?.startPercent
        val current = snapshot?.currentPercent
        simpleBattery.text = if (start != null && current != null) "$start% → $current%" else "—"
        simpleTime.text = SessionFormatter.duration(snapshot?.elapsedMs)
        simpleEnergy.text = SessionFormatter.energy(snapshot?.energyWh)
        simplePower.text = SessionFormatter.power(snapshot?.averagePowerW)
        simpleTemperature.text = SessionFormatter.temperature(snapshot?.maxTemperatureC)
        simpleTemperature.setTextColor(activity.getColor(temperatureColor(snapshot?.maxTemperatureC)))
    }

    private fun renderSessionState(info: BatteryInfo?, snapshot: ChargingSession.Snapshot?) {
        val hasSession = snapshot?.elapsedMs != null
        val full = snapshot?.reachedFull == true
        statusChip.text = when {
            full -> "●  Completa"
            info?.isCharging == true -> "●  Carregando"
            info?.isPlugged == true -> "●  Pausada"
            else -> "●  Sem carga"
        }
        statusChip.setTextColor(
            activity.getColor(
                when {
                    full || info?.isCharging == true -> R.color.accent_green
                    info?.isPlugged == true -> R.color.accent_orange
                    else -> R.color.text_secondary
                }
            )
        )
        guideCard.visibility = if (hasSession || info?.isPlugged == true) View.GONE else View.VISIBLE
    }

    private fun renderInsight(info: BatteryInfo?, snapshot: ChargingSession.Snapshot?) {
        val insight = SessionInsightBuilder.build(info, snapshot, sourceProfile)
        insightState.text = insight.sessionState
        insightHeadline.text = insight.headline
        insightPower.text = insight.rhythm
        insightStability.text = "●  ${insight.diagnosis}"
        insightDetail.text = insight.detail
        val color = activity.getColor(levelColor(insight.level))
        insightStability.setTextColor(color)
        insightState.setTextColor(
            activity.getColor(if (snapshot?.reachedFull == true) R.color.accent_green else R.color.text_primary)
        )
    }

    private fun renderPowerReference(snapshot: ChargingSession.Snapshot?) {
        val peak = snapshot?.peakPowerW
        val nominal = sourceProfile?.nominalPowerW
        sourceReference.text = when {
            peak != null && nominal != null && nominal > 0.0 -> {
                val pct = ((peak / nominal) * 100.0).roundToInt().coerceAtLeast(0)
                "Pico observado: ${SessionFormatter.power(peak)} • ≈$pct% da referência configurada"
            }
            peak != null -> "Pico observado no aparelho: ${SessionFormatter.power(peak)}"
            nominal != null -> "Referência configurada: ${SessionFormatter.power(nominal)}"
            else -> "Sem referência configurada • toque para informar a fonte"
        }
    }

    private fun levelColor(level: ChargeConditionInterpreter.Level): Int = when (level) {
        ChargeConditionInterpreter.Level.GOOD -> R.color.accent_green
        ChargeConditionInterpreter.Level.INFO -> R.color.accent_blue
        ChargeConditionInterpreter.Level.ATTENTION -> R.color.accent_orange
        ChargeConditionInterpreter.Level.ELEVATED -> R.color.accent_orange
        ChargeConditionInterpreter.Level.CRITICAL -> R.color.accent_red
    }

    private fun temperatureColor(value: Double?): Int = when {
        value == null -> R.color.value_unavailable
        value >= 45.0 -> R.color.accent_red
        value >= 38.0 -> R.color.accent_orange
        else -> R.color.accent_green
    }

    private fun text(id: Int): TextView = activity.findViewById(id)
}
