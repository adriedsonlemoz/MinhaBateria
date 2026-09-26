package com.minhabateria.app.battery

class BatteryCurrentReader {
    fun readSignedMa(info: BatteryInfo): Double? =
        info.currentMa?.takeIf { it.isFinite() }
}
