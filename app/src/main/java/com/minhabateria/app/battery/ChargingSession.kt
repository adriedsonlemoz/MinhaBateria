package com.minhabateria.app.battery

import com.minhabateria.app.session.SessionAccumulator

class ChargingSession(savedState: SavedState? = null) {
    data class SavedState(
        val active: Boolean,
        val startedAtMs: Long?,
        val chargingTimeMs: Long,
        val interruptions: Int,
        val startPercent: Int?,
        val currentPercent: Int?,
        val previousCharging: Boolean?,
        val accumulator: SessionAccumulator.State
    )

    data class Snapshot(
        val elapsedMs: Long?,
        val chargingTimeMs: Long?,
        val energyWh: Double?,
        val chargeMah: Double?,
        val averagePowerW: Double?,
        val averageCurrentMa: Double?,
        val averageVoltageV: Double?,
        val minPowerW: Double?,
        val maxPowerW: Double?,
        val minCurrentMa: Double?,
        val maxCurrentMa: Double?,
        val minVoltageV: Double?,
        val maxVoltageV: Double?,
        val peakCurrentMa: Double?,
        val peakPowerW: Double?,
        val averageTemperatureC: Double?,
        val maxTemperatureC: Double?,
        val interruptions: Int,
        val startPercent: Int?,
        val currentPercent: Int?,
        val gainPercent: Int?
    )

    private val accumulator = SessionAccumulator(savedState?.accumulator)
    private var active = savedState?.active ?: false
    private var startedAtMs: Long? = savedState?.startedAtMs
    private var chargingTimeMs = savedState?.chargingTimeMs ?: 0L
    private var interruptions = savedState?.interruptions ?: 0
    private var startPercent: Int? = savedState?.startPercent
    private var currentPercent: Int? = savedState?.currentPercent
    private var previousInfo: BatteryInfo? = null
    private var previousAtMs: Long? = null
    private var previousCharging: Boolean? = savedState?.previousCharging

    fun update(info: BatteryInfo, nowMs: Long = System.currentTimeMillis()): Snapshot {
        if (!active && info.isCharging == true) start(info, nowMs)
        if (!active) return emptySnapshot()

        currentPercent = info.percent ?: currentPercent
        val previous = previousInfo
        val previousAt = previousAtMs

        if (previous != null && previousAt != null) {
            val intervalMs = nowMs - previousAt
            if (isSafeInterval(intervalMs) && canIntegrate(previous, info)) {
                chargingTimeMs += intervalMs
                accumulator.integrate(previous, info, intervalMs)
            }
        }

        if (previousCharging == true && info.isCharging == false) interruptions += 1
        if (info.isCharging == true) accumulator.observe(info)

        previousInfo = info
        previousAtMs = nowMs
        previousCharging = info.isCharging
        return snapshot(nowMs)
    }


    fun savedState(): SavedState = SavedState(
        active = active,
        startedAtMs = startedAtMs,
        chargingTimeMs = chargingTimeMs,
        interruptions = interruptions,
        startPercent = startPercent,
        currentPercent = currentPercent,
        previousCharging = previousCharging,
        accumulator = accumulator.savedState()
    )

    private fun start(info: BatteryInfo, nowMs: Long) {
        active = true
        startedAtMs = nowMs
        startPercent = info.percent
        currentPercent = info.percent
        accumulator.observe(info)
    }

    private fun snapshot(nowMs: Long): Snapshot {
        val accumulated = accumulator.snapshot()
        return Snapshot(
            elapsedMs = startedAtMs?.let { (nowMs - it).coerceAtLeast(0L) },
            chargingTimeMs = chargingTimeMs,
            energyWh = accumulated.energyWh,
            chargeMah = accumulated.chargeMah,
            averagePowerW = accumulated.averagePowerW,
            averageCurrentMa = accumulated.averageCurrentMa,
            averageVoltageV = accumulated.averageVoltageV,
            minPowerW = accumulated.minPowerW,
            maxPowerW = accumulated.maxPowerW,
            minCurrentMa = accumulated.minCurrentMa,
            maxCurrentMa = accumulated.maxCurrentMa,
            minVoltageV = accumulated.minVoltageV,
            maxVoltageV = accumulated.maxVoltageV,
            peakCurrentMa = accumulated.maxCurrentMa,
            peakPowerW = accumulated.maxPowerW,
            averageTemperatureC = accumulated.averageTemperatureC,
            maxTemperatureC = accumulated.maxTemperatureC,
            interruptions = interruptions,
            startPercent = startPercent,
            currentPercent = currentPercent,
            gainPercent = batteryGain()
        )
    }

    private fun emptySnapshot(): Snapshot = Snapshot(
        elapsedMs = null,
        chargingTimeMs = null,
        energyWh = null,
        chargeMah = null,
        averagePowerW = null,
        averageCurrentMa = null,
        averageVoltageV = null,
        minPowerW = null,
        maxPowerW = null,
        minCurrentMa = null,
        maxCurrentMa = null,
        minVoltageV = null,
        maxVoltageV = null,
        peakCurrentMa = null,
        peakPowerW = null,
        averageTemperatureC = null,
        maxTemperatureC = null,
        interruptions = 0,
        startPercent = null,
        currentPercent = null,
        gainPercent = null
    )

    private fun batteryGain(): Int? {
        val start = startPercent ?: return null
        val current = currentPercent ?: return null
        return current - start
    }

    private fun canIntegrate(previous: BatteryInfo, current: BatteryInfo): Boolean =
        previous.isCharging == true &&
            current.isCharging == true &&
            previous.source == current.source

    private fun isSafeInterval(intervalMs: Long): Boolean =
        intervalMs in 1..MAX_INTEGRATION_INTERVAL_MS

    private companion object {
        const val MAX_INTEGRATION_INTERVAL_MS = 15_000L
    }
}
