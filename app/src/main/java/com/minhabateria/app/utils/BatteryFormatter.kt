package com.minhabateria.app.utils

import com.minhabateria.app.battery.ChargingSource
import java.util.Locale

object BatteryFormatter {
    fun source(source: ChargingSource): String = when (source) {
        ChargingSource.AC -> "Tomada (AC)"
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

    fun temperature(celsius: Double?): String = celsius?.let {
        String.format(Locale.getDefault(), "%.1f °C", it)
    } ?: "—"
}
