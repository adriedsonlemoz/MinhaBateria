package com.minhabateria.app.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.minhabateria.app.calculation.PowerCalculator
import kotlin.math.abs

class BatteryStatusReader(private val context: Context) {
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    fun read(): BatteryInfo {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) {
            (level * 100 / scale).coerceIn(0, 100)
        } else {
            null
        }

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING,
            BatteryManager.BATTERY_STATUS_FULL -> true
            BatteryManager.BATTERY_STATUS_DISCHARGING,
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false
            else -> null
        }

        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val isPlugged = when {
            plugged == 0 -> false
            plugged > 0 -> true
            else -> null
        }
        val source = readSource(plugged)

        val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            ?.takeIf { it > 0 }
        val temperatureC = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            ?.takeIf { it != Int.MIN_VALUE }
            ?.div(10.0)
        val currentMa = readChargingCurrent(isCharging)
        val powerW = PowerCalculator.watts(voltageMv, currentMa)
        val chargeTimeRemainingMs = readChargeTimeRemaining(isCharging, percent)

        return BatteryInfo(
            percent = percent,
            isCharging = isCharging,
            isPlugged = isPlugged,
            source = source,
            voltageMv = voltageMv,
            currentMa = currentMa,
            temperatureC = temperatureC,
            powerW = powerW,
            chargeTimeRemainingMs = chargeTimeRemainingMs
        )
    }

    private fun readChargeTimeRemaining(isCharging: Boolean?, percent: Int?): Long? {
        if (isCharging != true || percent == 100 || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return batteryManager.computeChargeTimeRemaining()
            .takeIf { it > 0L }
    }

    private fun readChargingCurrent(isCharging: Boolean?): Double? {
        if (isCharging != true) return null
        val currentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (currentUa == Int.MIN_VALUE || currentUa == 0) return null
        return abs(currentUa.toDouble())
            .div(1000.0)
            .takeIf { it.isFinite() && it > 0.0 }
    }

    private fun readSource(plugged: Int): ChargingSource = when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> ChargingSource.AC
        BatteryManager.BATTERY_PLUGGED_USB -> ChargingSource.USB
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingSource.WIRELESS
        0 -> ChargingSource.BATTERY
        else -> ChargingSource.UNKNOWN
    }
}
