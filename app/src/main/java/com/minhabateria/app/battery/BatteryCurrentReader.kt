package com.minhabateria.app.battery

import android.content.Context
import android.os.BatteryManager
import kotlin.math.abs

class BatteryCurrentReader(context: Context) {
    private val batteryManager = context.getSystemService(BatteryManager::class.java)

    fun readSignedMa(info: BatteryInfo): Double? {
        val currentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (currentUa == Int.MIN_VALUE || currentUa == 0) return null

        val rawMa = currentUa.toDouble() / 1000.0
        if (!rawMa.isFinite()) return null
        val magnitude = abs(rawMa).takeIf { it > 0.0 } ?: return null

        return when {
            info.isCharging == true -> magnitude
            info.isPlugged == false -> -magnitude
            else -> rawMa
        }
    }
}
