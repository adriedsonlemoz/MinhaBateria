package com.minhabateria.app.utils

import com.minhabateria.app.battery.ChargingSource
import java.util.Locale

object BatteryFormatter {
    fun source(source: ChargingSource): String = when (source) {
        ChargingSource.AC -> "Conexão AC"
        ChargingSource.USB -> "USB"
        ChargingSource.WIRELESS -> "Sem fio"
        ChargingSource.BATTERY -> "Bateria"
        ChargingSource.UNKNOWN -> "Desconhecida"
    }

    fun voltage(millivolts: Int?): String = millivolts?.let {
        String.format(Locale.getDefault(), "%.2f V", it / 1000.0)
    } ?: "—"

    fun current(milliamps: Double?): String = milliamps?.let {
        String.format(Locale.getDefault(), "%.0f mA", it)
    } ?: "—"

    fun signedCurrent(milliamps: Double?): String = milliamps?.let {
        if (it == 0.0) "0 mA" else String.format(Locale.getDefault(), "%+.0f mA", it)
    } ?: "—"

    fun power(watts: Double?): String = watts?.let {
        String.format(Locale.getDefault(), "%.2f W", it)
    } ?: "—"

    fun temperature(celsius: Double?): String = celsius?.let(::formatTemperature) ?: "—"

    private fun formatTemperature(value: Double): String {
        val rounded = kotlin.math.round(value)
        val pattern = if (kotlin.math.abs(value - rounded) < 0.05) "%.0f °C" else "%.1f °C"
        return String.format(Locale.getDefault(), pattern, value)
    }
}
