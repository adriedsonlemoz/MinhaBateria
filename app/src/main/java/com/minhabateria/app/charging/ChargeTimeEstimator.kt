package com.minhabateria.app.charging

import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession

object ChargeTimeEstimator {
    enum class Source { SYSTEM, SESSION }

    data class Estimate(
        val remainingMs: Long,
        val source: Source
    )

    fun estimate(info: BatteryInfo, session: ChargingSession.Snapshot): Estimate? {
        if (info.percent == 100 || session.reachedFull) return Estimate(0L, Source.SYSTEM)
        if (info.isCharging != true) return null

        info.chargeTimeRemainingMs
            ?.takeIf { it in 1..MAX_REASONABLE_REMAINING_MS }
            ?.let { return Estimate(it, Source.SYSTEM) }

        val currentPercent = info.percent ?: session.currentPercent ?: return null
        val gain = session.gainPercent ?: return null
        val chargingTimeMs = session.chargingTimeMs ?: return null

        if (currentPercent !in 1..99) return null
        if (gain < MIN_GAIN_PERCENT || chargingTimeMs < MIN_SESSION_TIME_MS) return null

        val remainingPercent = 100 - currentPercent
        val estimated = (chargingTimeMs.toDouble() / gain.toDouble() * remainingPercent.toDouble()).toLong()
        if (estimated !in 1..MAX_REASONABLE_REMAINING_MS) return null
        return Estimate(estimated, Source.SESSION)
    }

    private const val MIN_GAIN_PERCENT = 2
    private const val MIN_SESSION_TIME_MS = 2 * 60 * 1000L
    private const val MAX_REASONABLE_REMAINING_MS = 24 * 60 * 60 * 1000L
}
