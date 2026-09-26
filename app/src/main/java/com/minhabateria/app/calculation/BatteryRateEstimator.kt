package com.minhabateria.app.calculation

import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.discharge.ActiveDischarge

object BatteryRateEstimator {
    fun chargingPercentPerHour(session: ChargingSession.Snapshot?): Double? {
        val gain = session?.gainPercent ?: return null
        val durationMs = session.chargingTimeMs ?: return null
        if (gain < MIN_PERCENT_CHANGE || durationMs < MIN_SAMPLE_DURATION_MS) return null
        val hours = durationMs / MILLIS_PER_HOUR
        return (gain / hours).takeIf(::isReasonableRate)
    }

    fun dischargePercentPerHour(discharge: ActiveDischarge?): Double? {
        discharge ?: return null
        if (discharge.dropPercent < MIN_PERCENT_CHANGE || discharge.durationMs < MIN_SAMPLE_DURATION_MS) return null
        return discharge.rawRatePercentPerHour?.takeIf(::isReasonableRate)
    }

    fun dischargeRemainingMs(discharge: ActiveDischarge?): Long? {
        val rate = dischargePercentPerHour(discharge) ?: return null
        val percent = discharge?.currentPercent ?: return null
        if (percent <= 0) return null
        return (percent / rate * MILLIS_PER_HOUR)
            .toLong()
            .takeIf { it > 0L }
    }

    fun hasEnoughDischargeSample(discharge: ActiveDischarge?): Boolean =
        discharge != null &&
            discharge.dropPercent >= MIN_PERCENT_CHANGE &&
            discharge.durationMs >= MIN_SAMPLE_DURATION_MS

    private fun isReasonableRate(rate: Double): Boolean =
        rate.isFinite() && rate in MIN_RATE_PERCENT_PER_HOUR..MAX_RATE_PERCENT_PER_HOUR

    private const val MIN_PERCENT_CHANGE = 1
    private const val MIN_SAMPLE_DURATION_MS = 3 * 60 * 1000L
    private const val MIN_RATE_PERCENT_PER_HOUR = 0.1
    private const val MAX_RATE_PERCENT_PER_HOUR = 100.0
    private const val MILLIS_PER_HOUR = 3_600_000.0
}
