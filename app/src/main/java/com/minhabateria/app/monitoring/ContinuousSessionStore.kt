package com.minhabateria.app.monitoring

import android.content.Context
import android.content.SharedPreferences
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.session.SessionAccumulator

class ContinuousSessionStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): ChargingSession.SavedState? {
        if (!preferences.getBoolean(KEY_ACTIVE, false)) return null
        return ChargingSession.SavedState(
            active = true,
            startedAtMs = preferences.longOrNull(KEY_STARTED_AT),
            chargingTimeMs = preferences.getLong(KEY_CHARGING_TIME, 0L),
            interruptions = preferences.getInt(KEY_INTERRUPTION_COUNT, 0),
            startPercent = preferences.intOrNull(KEY_START_PERCENT),
            currentPercent = preferences.intOrNull(KEY_CURRENT_PERCENT),
            previousCharging = preferences.booleanOrNull(KEY_PREVIOUS_CHARGING),
            accumulator = loadAccumulator()
        )
    }

    fun save(state: ChargingSession.SavedState) {
        val a = state.accumulator
        preferences.edit()
            .putBoolean(KEY_ACTIVE, state.active)
            .putNullableLong(KEY_STARTED_AT, state.startedAtMs)
            .putLong(KEY_CHARGING_TIME, state.chargingTimeMs)
            .putInt(KEY_INTERRUPTION_COUNT, state.interruptions)
            .putNullableInt(KEY_START_PERCENT, state.startPercent)
            .putNullableInt(KEY_CURRENT_PERCENT, state.currentPercent)
            .putNullableBoolean(KEY_PREVIOUS_CHARGING, state.previousCharging)
            .putDouble(KEY_ENERGY_WH, a.energyWh)
            .putDouble(KEY_CHARGE_MAH, a.chargeMah)
            .putDouble(KEY_VOLTAGE_V_MS, a.voltageVoltMs)
            .putDouble(KEY_TEMPERATURE_C_MS, a.temperatureCelsiusMs)
            .putLong(KEY_POWER_DURATION, a.powerDurationMs)
            .putLong(KEY_CURRENT_DURATION, a.currentDurationMs)
            .putLong(KEY_VOLTAGE_DURATION, a.voltageDurationMs)
            .putLong(KEY_TEMPERATURE_DURATION, a.temperatureDurationMs)
            .putNullableDouble(KEY_MIN_POWER, a.minPowerW)
            .putNullableDouble(KEY_MAX_POWER, a.maxPowerW)
            .putNullableDouble(KEY_MIN_CURRENT, a.minCurrentMa)
            .putNullableDouble(KEY_MAX_CURRENT, a.maxCurrentMa)
            .putNullableDouble(KEY_MIN_VOLTAGE, a.minVoltageV)
            .putNullableDouble(KEY_MAX_VOLTAGE, a.maxVoltageV)
            .putNullableDouble(KEY_MAX_TEMPERATURE, a.maxTemperatureC)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun loadAccumulator(): SessionAccumulator.State = SessionAccumulator.State(
        energyWh = preferences.double(KEY_ENERGY_WH),
        chargeMah = preferences.double(KEY_CHARGE_MAH),
        voltageVoltMs = preferences.double(KEY_VOLTAGE_V_MS),
        temperatureCelsiusMs = preferences.double(KEY_TEMPERATURE_C_MS),
        powerDurationMs = preferences.getLong(KEY_POWER_DURATION, 0L),
        currentDurationMs = preferences.getLong(KEY_CURRENT_DURATION, 0L),
        voltageDurationMs = preferences.getLong(KEY_VOLTAGE_DURATION, 0L),
        temperatureDurationMs = preferences.getLong(KEY_TEMPERATURE_DURATION, 0L),
        minPowerW = preferences.doubleOrNull(KEY_MIN_POWER),
        maxPowerW = preferences.doubleOrNull(KEY_MAX_POWER),
        minCurrentMa = preferences.doubleOrNull(KEY_MIN_CURRENT),
        maxCurrentMa = preferences.doubleOrNull(KEY_MAX_CURRENT),
        minVoltageV = preferences.doubleOrNull(KEY_MIN_VOLTAGE),
        maxVoltageV = preferences.doubleOrNull(KEY_MAX_VOLTAGE),
        maxTemperatureC = preferences.doubleOrNull(KEY_MAX_TEMPERATURE)
    )

    private fun SharedPreferences.double(key: String): Double =
        getString(key, null)?.toDoubleOrNull() ?: 0.0

    private fun SharedPreferences.doubleOrNull(key: String): Double? =
        getString(key, null)?.toDoubleOrNull()

    private fun SharedPreferences.longOrNull(key: String): Long? =
        if (contains(key)) getLong(key, 0L) else null

    private fun SharedPreferences.intOrNull(key: String): Int? =
        if (contains(key)) getInt(key, 0) else null

    private fun SharedPreferences.booleanOrNull(key: String): Boolean? =
        if (contains(key)) getBoolean(key, false) else null

    private fun SharedPreferences.Editor.putDouble(key: String, value: Double): SharedPreferences.Editor =
        putString(key, value.toString())

    private fun SharedPreferences.Editor.putNullableDouble(key: String, value: Double?): SharedPreferences.Editor =
        if (value == null) remove(key) else putString(key, value.toString())

    private fun SharedPreferences.Editor.putNullableLong(key: String, value: Long?): SharedPreferences.Editor =
        if (value == null) remove(key) else putLong(key, value)

    private fun SharedPreferences.Editor.putNullableInt(key: String, value: Int?): SharedPreferences.Editor =
        if (value == null) remove(key) else putInt(key, value)

    private fun SharedPreferences.Editor.putNullableBoolean(key: String, value: Boolean?): SharedPreferences.Editor =
        if (value == null) remove(key) else putBoolean(key, value)

    private companion object {
        const val PREFS_NAME = "continuous_session"
        const val KEY_ACTIVE = "active"
        const val KEY_STARTED_AT = "started_at"
        const val KEY_CHARGING_TIME = "charging_time"
        const val KEY_INTERRUPTION_COUNT = "interruptions"
        const val KEY_START_PERCENT = "start_percent"
        const val KEY_CURRENT_PERCENT = "current_percent"
        const val KEY_PREVIOUS_CHARGING = "previous_charging"
        const val KEY_ENERGY_WH = "energy_wh"
        const val KEY_CHARGE_MAH = "charge_mah"
        const val KEY_VOLTAGE_V_MS = "voltage_v_ms"
        const val KEY_TEMPERATURE_C_MS = "temperature_c_ms"
        const val KEY_POWER_DURATION = "power_duration"
        const val KEY_CURRENT_DURATION = "current_duration"
        const val KEY_VOLTAGE_DURATION = "voltage_duration"
        const val KEY_TEMPERATURE_DURATION = "temperature_duration"
        const val KEY_MIN_POWER = "min_power"
        const val KEY_MAX_POWER = "max_power"
        const val KEY_MIN_CURRENT = "min_current"
        const val KEY_MAX_CURRENT = "max_current"
        const val KEY_MIN_VOLTAGE = "min_voltage"
        const val KEY_MAX_VOLTAGE = "max_voltage"
        const val KEY_MAX_TEMPERATURE = "max_temperature"
    }
}
