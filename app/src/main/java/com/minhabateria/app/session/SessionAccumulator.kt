package com.minhabateria.app.session

import com.minhabateria.app.battery.BatteryInfo
import kotlin.math.sqrt

class SessionAccumulator(savedState: State? = null) {
    data class State(
        val energyWh: Double,
        val chargeMah: Double,
        val voltageVoltMs: Double,
        val temperatureCelsiusMs: Double,
        val powerDurationMs: Long,
        val currentDurationMs: Long,
        val voltageDurationMs: Long,
        val temperatureDurationMs: Long,
        val minPowerW: Double?,
        val maxPowerW: Double?,
        val minCurrentMa: Double?,
        val maxCurrentMa: Double?,
        val minVoltageV: Double?,
        val maxVoltageV: Double?,
        val maxTemperatureC: Double?,
        val powerSampleCount: Int,
        val powerMeanW: Double,
        val powerM2W: Double
    )

    data class Snapshot(
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
        val averageTemperatureC: Double?,
        val maxTemperatureC: Double?,
        val powerVariationRatio: Double?
    )

    private var energyWh = 0.0
    private var chargeMah = 0.0
    private var voltageVoltMs = 0.0
    private var temperatureCelsiusMs = 0.0
    private var powerDurationMs = 0L
    private var currentDurationMs = 0L
    private var voltageDurationMs = 0L
    private var temperatureDurationMs = 0L
    private var minPowerW: Double? = null
    private var maxPowerW: Double? = null
    private var minCurrentMa: Double? = null
    private var maxCurrentMa: Double? = null
    private var minVoltageV: Double? = null
    private var maxVoltageV: Double? = null
    private var maxTemperatureC: Double? = null
    private var powerSampleCount = 0
    private var powerMeanW = 0.0
    private var powerM2W = 0.0

    init {
        savedState?.let(::restore)
    }

    fun observe(info: BatteryInfo) {
        if (info.isCharging != true) return
        info.powerW?.validPositive()?.let { value ->
            minPowerW = minOf(minPowerW ?: value, value)
            maxPowerW = maxOf(maxPowerW ?: value, value)
            observePowerVariation(value)
        }
        info.currentMa?.validPositive()?.let { value ->
            minCurrentMa = minOf(minCurrentMa ?: value, value)
            maxCurrentMa = maxOf(maxCurrentMa ?: value, value)
        }
        info.voltageMv?.takeIf { it > 0 }?.div(1000.0)?.let { value ->
            minVoltageV = minOf(minVoltageV ?: value, value)
            maxVoltageV = maxOf(maxVoltageV ?: value, value)
        }
        info.temperatureC?.takeIf { it.isFinite() }?.let { value ->
            maxTemperatureC = maxOf(maxTemperatureC ?: value, value)
        }
    }

    fun integrate(previous: BatteryInfo, current: BatteryInfo, durationMs: Long) {
        if (previous.isCharging != true || current.isCharging != true) return
        if (durationMs <= 0L || durationMs > MAX_INTERVAL_MS) return

        integratePositive(previous.powerW, current.powerW) { average ->
            energyWh += average * durationMs / MILLIS_PER_HOUR
            powerDurationMs += durationMs
        }
        integratePositive(previous.currentMa, current.currentMa) { average ->
            chargeMah += average * durationMs / MILLIS_PER_HOUR
            currentDurationMs += durationMs
        }

        val previousVoltage = previous.voltageMv?.takeIf { it > 0 }?.div(1000.0)
        val currentVoltage = current.voltageMv?.takeIf { it > 0 }?.div(1000.0)
        integratePositive(previousVoltage, currentVoltage) { average ->
            voltageVoltMs += average * durationMs
            voltageDurationMs += durationMs
        }

        val previousTemperature = previous.temperatureC?.takeIf { it.isFinite() }
        val currentTemperature = current.temperatureC?.takeIf { it.isFinite() }
        if (previousTemperature != null && currentTemperature != null) {
            temperatureCelsiusMs += ((previousTemperature + currentTemperature) / 2.0) * durationMs
            temperatureDurationMs += durationMs
        }
    }

    fun snapshot(): Snapshot = Snapshot(
        energyWh = energyWh.takeIf { powerDurationMs > 0L },
        chargeMah = chargeMah.takeIf { currentDurationMs > 0L },
        averagePowerW = averageFromEnergy(energyWh, powerDurationMs),
        averageCurrentMa = averageFromEnergy(chargeMah, currentDurationMs),
        averageVoltageV = averageWeighted(voltageVoltMs, voltageDurationMs),
        minPowerW = minPowerW,
        maxPowerW = maxPowerW,
        minCurrentMa = minCurrentMa,
        maxCurrentMa = maxCurrentMa,
        minVoltageV = minVoltageV,
        maxVoltageV = maxVoltageV,
        averageTemperatureC = averageWeighted(temperatureCelsiusMs, temperatureDurationMs),
        maxTemperatureC = maxTemperatureC,
        powerVariationRatio = powerVariationRatio()
    )

    fun savedState(): State = State(
        energyWh = energyWh,
        chargeMah = chargeMah,
        voltageVoltMs = voltageVoltMs,
        temperatureCelsiusMs = temperatureCelsiusMs,
        powerDurationMs = powerDurationMs,
        currentDurationMs = currentDurationMs,
        voltageDurationMs = voltageDurationMs,
        temperatureDurationMs = temperatureDurationMs,
        minPowerW = minPowerW,
        maxPowerW = maxPowerW,
        minCurrentMa = minCurrentMa,
        maxCurrentMa = maxCurrentMa,
        minVoltageV = minVoltageV,
        maxVoltageV = maxVoltageV,
        maxTemperatureC = maxTemperatureC,
        powerSampleCount = powerSampleCount,
        powerMeanW = powerMeanW,
        powerM2W = powerM2W
    )

    private fun restore(state: State) {
        energyWh = state.energyWh
        chargeMah = state.chargeMah
        voltageVoltMs = state.voltageVoltMs
        temperatureCelsiusMs = state.temperatureCelsiusMs
        powerDurationMs = state.powerDurationMs
        currentDurationMs = state.currentDurationMs
        voltageDurationMs = state.voltageDurationMs
        temperatureDurationMs = state.temperatureDurationMs
        minPowerW = state.minPowerW
        maxPowerW = state.maxPowerW
        minCurrentMa = state.minCurrentMa
        maxCurrentMa = state.maxCurrentMa
        minVoltageV = state.minVoltageV
        maxVoltageV = state.maxVoltageV
        maxTemperatureC = state.maxTemperatureC
        powerSampleCount = state.powerSampleCount
        powerMeanW = state.powerMeanW
        powerM2W = state.powerM2W
    }

    private fun observePowerVariation(value: Double) {
        powerSampleCount += 1
        val delta = value - powerMeanW
        powerMeanW += delta / powerSampleCount
        powerM2W += delta * (value - powerMeanW)
    }

    private fun powerVariationRatio(): Double? {
        if (powerSampleCount < MIN_STABILITY_SAMPLES || powerMeanW <= 0.0) return null
        val variance = powerM2W / (powerSampleCount - 1)
        return sqrt(variance.coerceAtLeast(0.0)) / powerMeanW
    }

    private fun integratePositive(previous: Double?, current: Double?, block: (Double) -> Unit) {
        val start = previous?.validPositive() ?: return
        val end = current?.validPositive() ?: return
        block((start + end) / 2.0)
    }

    private fun averageFromEnergy(total: Double, durationMs: Long): Double? {
        if (durationMs <= 0L) return null
        return total / (durationMs / MILLIS_PER_HOUR)
    }

    private fun averageWeighted(weightedTotal: Double, durationMs: Long): Double? =
        if (durationMs > 0L) weightedTotal / durationMs else null

    private fun Double.validPositive(): Double? = takeIf { isFinite() && it > 0.0 }

    private companion object {
        const val MAX_INTERVAL_MS = 15_000L
        const val MILLIS_PER_HOUR = 3_600_000.0
        const val MIN_STABILITY_SAMPLES = 5
    }
}
