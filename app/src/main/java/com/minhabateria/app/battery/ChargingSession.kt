package com.minhabateria.app.battery

import com.minhabateria.app.session.SessionAccumulator

class ChargingSession(savedState: SavedState? = null) {
    data class SavedState(
        val active: Boolean,
        val startedAtMs: Long?,
        val fullReachedAtMs: Long?,
        val chargingTimeMs: Long,
        val interruptions: Int,
        val startPercent: Int?,
        val currentPercent: Int?,
        val previousCharging: Boolean?,
        val accumulator: SessionAccumulator.State
    )

    data class CompletedSession(
        val startedAtMs: Long,
        val endedAtMs: Long,
        val snapshot: Snapshot
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
        val powerVariationRatio: Double?,
        val interruptions: Int,
        val startPercent: Int?,
        val currentPercent: Int?,
        val gainPercent: Int?,
        val reachedFull: Boolean
    )

    private var accumulator = SessionAccumulator(savedState?.accumulator)
    private var active = savedState?.active ?: false
    private var startedAtMs: Long? = savedState?.startedAtMs
    private var fullReachedAtMs: Long? = savedState?.fullReachedAtMs
    private var chargingTimeMs = savedState?.chargingTimeMs ?: 0L
    private var interruptions = savedState?.interruptions ?: 0
    private var startPercent: Int? = savedState?.startPercent
    private var currentPercent: Int? = savedState?.currentPercent
    private var previousInfo: BatteryInfo? = null
    private var previousAtMs: Long? = null
    private var previousCharging: Boolean? = savedState?.previousCharging
    private var completedSession: CompletedSession? = null

    fun update(info: BatteryInfo, nowMs: Long = System.currentTimeMillis()): Snapshot {
        if (!active && isConnected(info)) start(info, nowMs)
        if (!active) return emptySnapshot()

        currentPercent = info.percent ?: currentPercent
        if (info.isPlugged == false) {
            val finalSnapshot = snapshot(nowMs)
            startedAtMs?.let { started ->
                completedSession = CompletedSession(started, nowMs, finalSnapshot)
            }
            resetCurrent()
            return emptySnapshot()
        }

        if (fullReachedAtMs == null) updateActiveMeasurements(info, nowMs)
        if (fullReachedAtMs == null && currentPercent == 100 && isConnected(info)) {
            fullReachedAtMs = nowMs
        }

        previousInfo = info
        previousAtMs = nowMs
        previousCharging = info.isCharging
        return snapshot(nowMs)
    }

    fun takeCompletedSession(): CompletedSession? = completedSession.also { completedSession = null }

    fun takeCompletedSnapshot(): Snapshot? = takeCompletedSession()?.snapshot

    fun savedState(): SavedState = SavedState(
        active = active,
        startedAtMs = startedAtMs,
        fullReachedAtMs = fullReachedAtMs,
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
        fullReachedAtMs = null
        chargingTimeMs = 0L
        interruptions = 0
        startPercent = info.percent
        currentPercent = info.percent
        previousInfo = null
        previousAtMs = null
        previousCharging = info.isCharging
        accumulator = SessionAccumulator()
        if (info.isCharging == true) accumulator.observe(info)
    }

    private fun updateActiveMeasurements(info: BatteryInfo, nowMs: Long) {
        val previous = previousInfo
        val previousAt = previousAtMs
        if (previous != null && previousAt != null) {
            val intervalMs = nowMs - previousAt
            if (isSafeInterval(intervalMs) && canIntegrate(previous, info)) {
                chargingTimeMs += intervalMs
                accumulator.integrate(previous, info, intervalMs)
            }
        }
        if (previousCharging == true && info.isCharging == false && info.isPlugged == true) {
            interruptions += 1
        }
        if (info.isCharging == true) accumulator.observe(info)
    }

    private fun snapshot(nowMs: Long): Snapshot {
        val accumulated = accumulator.snapshot()
        val effectiveEnd = fullReachedAtMs ?: nowMs
        return Snapshot(
            elapsedMs = startedAtMs?.let { (effectiveEnd - it).coerceAtLeast(0L) },
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
            powerVariationRatio = accumulated.powerVariationRatio,
            interruptions = interruptions,
            startPercent = startPercent,
            currentPercent = currentPercent,
            gainPercent = batteryGain(),
            reachedFull = fullReachedAtMs != null
        )
    }

    private fun resetCurrent() {
        active = false
        startedAtMs = null
        fullReachedAtMs = null
        chargingTimeMs = 0L
        interruptions = 0
        startPercent = null
        currentPercent = null
        previousInfo = null
        previousAtMs = null
        previousCharging = null
        accumulator = SessionAccumulator()
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
        powerVariationRatio = null,
        interruptions = 0,
        startPercent = null,
        currentPercent = null,
        gainPercent = null,
        reachedFull = false
    )

    private fun batteryGain(): Int? {
        val start = startPercent ?: return null
        val current = currentPercent ?: return null
        return current - start
    }

    private fun isConnected(info: BatteryInfo): Boolean =
        info.isPlugged == true || (info.isPlugged == null && info.isCharging == true)

    private fun canIntegrate(previous: BatteryInfo, current: BatteryInfo): Boolean =
        previous.isCharging == true && current.isCharging == true && previous.source == current.source

    private fun isSafeInterval(intervalMs: Long): Boolean =
        intervalMs in 1..MAX_INTEGRATION_INTERVAL_MS

    private companion object {
        const val MAX_INTEGRATION_INTERVAL_MS = 15_000L
    }
}
