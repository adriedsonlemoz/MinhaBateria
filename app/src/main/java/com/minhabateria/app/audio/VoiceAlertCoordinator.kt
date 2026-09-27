package com.minhabateria.app.audio

import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession

class VoiceAlertCoordinator(private val runtime: VoiceAlertSink) {
    private var previous: BatteryInfo? = null
    private var sessionWasActive = false
    private var chargingInterrupted = false
    private var warm = false
    private var criticalTemperature = false
    private var chargeIssue = false
    private var lowPowerSince: Long? = null
    private var lowPowerAlerted = false
    private var dischargeWhilePluggedSince: Long? = null
    private var dischargeAlerted = false
    private var unstable = false
    private var oscillationAlerted = false
    private var normalSince: Long? = null

    fun monitoringStarted() = runtime.play(VoiceAlertEvent.MONITORING_STARTED)
    fun monitoringStopped() = runtime.play(VoiceAlertEvent.MONITORING_STOPPED)
    fun sessionEnded() {
        runtime.play(VoiceAlertEvent.SESSION_ENDED)
        sessionWasActive = false
        chargingInterrupted = false
        resetQualityState()
    }

    fun update(info: BatteryInfo, session: ChargingSession.Snapshot, nowMs: Long = System.currentTimeMillis()) {
        val old = previous
        if (old != null) {
            observeConnection(old, info)
        } else {
            observeInitialState(info)
        }
        observeSession(session)
        observeBattery(old?.percent, info.percent, info.isPlugged == true)
        observeTemperature(info.temperatureC)
        observeChargingQuality(info, session, nowMs)
        previous = info
    }


    private fun observeInitialState(info: BatteryInfo) {
        if (info.isPlugged == true) {
            runtime.play(VoiceAlertEvent.SOURCE_CONNECTED)
            if (info.isCharging == true) runtime.play(VoiceAlertEvent.CHARGING_STARTED)
        } else {
            val percent = info.percent
            when {
                percent != null && percent <= 5 -> runtime.play(VoiceAlertEvent.BATTERY_5)
                percent != null && percent <= 10 -> runtime.play(VoiceAlertEvent.BATTERY_10)
                percent != null && percent <= 20 -> runtime.play(VoiceAlertEvent.BATTERY_20)
            }
        }
    }

    private fun observeConnection(old: BatteryInfo, now: BatteryInfo) {
        if (old.isPlugged != true && now.isPlugged == true) runtime.play(VoiceAlertEvent.SOURCE_CONNECTED)
        if (old.isPlugged == true && now.isPlugged == false) {
            runtime.play(VoiceAlertEvent.SOURCE_DISCONNECTED)
            chargingInterrupted = false
        }

        if (old.isCharging != true && now.isCharging == true && now.isPlugged == true) {
            runtime.play(if (chargingInterrupted) VoiceAlertEvent.CHARGING_RESTORED else VoiceAlertEvent.CHARGING_STARTED)
            chargingInterrupted = false
        }
        if (old.isCharging == true && now.isCharging == false && now.isPlugged == true) {
            chargingInterrupted = true
            runtime.play(VoiceAlertEvent.CHARGING_INTERRUPTED)
        }
    }

    private fun observeSession(session: ChargingSession.Snapshot) {
        val active = session.elapsedMs != null
        if (!sessionWasActive && active) runtime.play(VoiceAlertEvent.SESSION_STARTED)
        sessionWasActive = active
    }

    private fun observeBattery(previousPercent: Int?, percent: Int?, plugged: Boolean) {
        if (percent == null || previousPercent == null) return
        if (plugged) {
            crossedUp(previousPercent, percent, 80) { runtime.play(VoiceAlertEvent.BATTERY_80) }
            crossedUp(previousPercent, percent, 90) { runtime.play(VoiceAlertEvent.BATTERY_90) }
            crossedUp(previousPercent, percent, 100) { runtime.play(VoiceAlertEvent.BATTERY_FULL) }
        } else {
            crossedDown(previousPercent, percent, 20) { runtime.play(VoiceAlertEvent.BATTERY_20) }
            crossedDown(previousPercent, percent, 10) { runtime.play(VoiceAlertEvent.BATTERY_10) }
            crossedDown(previousPercent, percent, 5) { runtime.play(VoiceAlertEvent.BATTERY_5) }
        }
    }

    private fun observeTemperature(value: Double?) {
        val temp = value ?: return
        when {
            temp >= CRITICAL_TEMP_C -> {
                if (!criticalTemperature) runtime.play(VoiceAlertEvent.TEMPERATURE_CRITICAL)
                criticalTemperature = true
                warm = true
            }
            temp >= WARM_TEMP_C -> {
                if (!warm) runtime.play(VoiceAlertEvent.TEMPERATURE_WARM)
                warm = true
                criticalTemperature = false
            }
            temp <= NORMAL_TEMP_C && warm -> {
                runtime.play(VoiceAlertEvent.TEMPERATURE_NORMAL)
                warm = false
                criticalTemperature = false
            }
        }
    }

    private fun observeChargingQuality(info: BatteryInfo, session: ChargingSession.Snapshot, nowMs: Long) {
        if (info.isPlugged != true) {
            resetQualityState()
            return
        }

        val power = info.powerW
        val lowPower = info.isCharging == true && power != null && power < LOW_POWER_W
        lowPowerSince = when {
            lowPower && lowPowerSince == null -> nowMs
            lowPower -> lowPowerSince
            else -> null
        }
        if (!lowPower) lowPowerAlerted = false
        if (!lowPowerAlerted && lowPowerSince != null && nowMs - lowPowerSince!! >= QUALITY_CONFIRM_MS) {
            runtime.play(if (power != null && power < VERY_LOW_POWER_W) VoiceAlertEvent.SOURCE_LOW_POWER else VoiceAlertEvent.CHARGE_SLOW)
            chargeIssue = true
            lowPowerAlerted = true
        }

        val discharging = info.currentMa?.let { it < -DISCHARGE_CURRENT_MA } == true && info.isPlugged == true
        dischargeWhilePluggedSince = when {
            discharging && dischargeWhilePluggedSince == null -> nowMs
            discharging -> dischargeWhilePluggedSince
            else -> null
        }
        if (!discharging) dischargeAlerted = false
        if (!dischargeAlerted && dischargeWhilePluggedSince != null && nowMs - dischargeWhilePluggedSince!! >= QUALITY_CONFIRM_MS) {
            runtime.play(VoiceAlertEvent.DISCHARGING_CONNECTED)
            chargeIssue = true
            dischargeAlerted = true
        }

        val unstableNow = session.sampleCount >= 20 && (session.powerVariationRatio ?: 0.0) >= UNSTABLE_RATIO
        if (unstableNow && !unstable) {
            runtime.play(VoiceAlertEvent.CHARGE_UNSTABLE)
            chargeIssue = true
        }
        unstable = unstableNow

        var oscillationTriggered = false
        if (!oscillationAlerted && session.interruptions >= 2) {
            runtime.play(VoiceAlertEvent.CONNECTION_OSCILLATING)
            oscillationAlerted = true
            chargeIssue = true
            oscillationTriggered = true
        }

        val issueNow = lowPower || discharging || unstableNow || oscillationTriggered || info.isCharging != true
        normalSince = when {
            !chargeIssue || issueNow -> null
            normalSince == null -> nowMs
            else -> normalSince
        }
        if (chargeIssue && normalSince != null && nowMs - normalSince!! >= NORMAL_CONFIRM_MS) {
            runtime.play(VoiceAlertEvent.CHARGE_NORMAL)
            chargeIssue = false
            normalSince = null
        }
    }


    private fun resetQualityState() {
        lowPowerSince = null
        lowPowerAlerted = false
        dischargeWhilePluggedSince = null
        dischargeAlerted = false
        unstable = false
        oscillationAlerted = false
        chargeIssue = false
        normalSince = null
    }

    private inline fun crossedUp(before: Int, now: Int, threshold: Int, action: () -> Unit) {
        if (before < threshold && now >= threshold) action()
    }

    private inline fun crossedDown(before: Int, now: Int, threshold: Int, action: () -> Unit) {
        if (before > threshold && now <= threshold) action()
    }

    private companion object {
        const val WARM_TEMP_C = 40.0
        const val CRITICAL_TEMP_C = 45.0
        const val NORMAL_TEMP_C = 38.0
        const val LOW_POWER_W = 3.0
        const val VERY_LOW_POWER_W = 1.0
        const val DISCHARGE_CURRENT_MA = 50.0
        const val QUALITY_CONFIRM_MS = 30_000L
        const val NORMAL_CONFIRM_MS = 20_000L
        const val UNSTABLE_RATIO = 0.35
    }
}
