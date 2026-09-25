package com.minhabateria.app.calculation

object PowerCalculator {
    fun watts(voltageMv: Int?, currentMa: Double?): Double? {
        if (voltageMv == null || currentMa == null) return null
        if (voltageMv <= 0 || currentMa <= 0.0) return null
        return (voltageMv / 1000.0) * (currentMa / 1000.0)
    }
}
