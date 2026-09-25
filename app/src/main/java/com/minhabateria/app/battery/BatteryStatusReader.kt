package com.minhabateria.app.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.minhabateria.app.calculation.PowerCalculator
import kotlin.math.abs

class BatteryStatusReader(private val context: Context) {
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    fun read(): BatteryInfo {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale).coerceIn(0, 100) else 0

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val source = readSource(plugged, isCharging)

        val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            ?.takeIf { it > 0 }

        val temperatureC = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            ?.takeIf { it != Int.MIN_VALUE }
            ?.div(10.0)

        val currentMa = readChargingCurrent(isCharging)
        val powerW = PowerCalculator.watts(voltageMv, currentMa)

        return BatteryInfo(
            percent = percent,
            isCharging = isCharging,
            source = source,
            voltageMv = voltageMv,
            currentMa = currentMa,
            temperatureC = temperatureC,
            powerW = powerW
        )
    }

    private fun readChargingCurrent(isCharging: Boolean): Double? {
        if (!isCharging) return null

        val currentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (currentUa == Int.MIN_VALUE || currentUa == 0) return null

        return abs(currentUa.toDouble()) / 1000.0
    }

    private fun readSource(plugged: Int, isCharging: Boolean): ChargingSource {
        if (!isCharging) return ChargingSource.BATTERY
        return when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> ChargingSource.AC
            BatteryManager.BATTERY_PLUGGED_USB -> ChargingSource.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingSource.WIRELESS
            else -> ChargingSource.UNKNOWN
        }
    }
}
