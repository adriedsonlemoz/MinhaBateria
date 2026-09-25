package com.minhabateria.app.battery

class ChargingSession {
    data class Snapshot(
        val elapsedMs: Long?,
        val peakCurrentMa: Double?,
        val peakPowerW: Double?
    )

    private var charging = false
    private var startedAt = 0L
    private var peakCurrentMa: Double? = null
    private var peakPowerW: Double? = null

    fun update(info: BatteryInfo, nowMs: Long = System.currentTimeMillis()): Snapshot {
        if (info.isCharging && !charging) {
            charging = true
            startedAt = nowMs
            peakCurrentMa = null
            peakPowerW = null
        } else if (!info.isCharging) {
            charging = false
        }

        if (!charging) {
            return Snapshot(null, null, null)
        }

        info.currentMa?.let { current ->
            peakCurrentMa = maxOf(peakCurrentMa ?: current, current)
        }
        info.powerW?.let { power ->
            peakPowerW = maxOf(peakPowerW ?: power, power)
        }

        return Snapshot(
            elapsedMs = (nowMs - startedAt).coerceAtLeast(0L),
            peakCurrentMa = peakCurrentMa,
            peakPowerW = peakPowerW
        )
    }
}
