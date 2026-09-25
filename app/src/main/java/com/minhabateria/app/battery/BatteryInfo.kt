package com.minhabateria.app.battery

data class BatteryInfo(
    val percent: Int,
    val isCharging: Boolean,
    val source: ChargingSource,
    val voltageMv: Int?,
    val currentMa: Double?,
    val temperatureC: Double?,
    val powerW: Double?
)

enum class ChargingSource {
    AC,
    USB,
    WIRELESS,
    BATTERY,
    UNKNOWN
}
