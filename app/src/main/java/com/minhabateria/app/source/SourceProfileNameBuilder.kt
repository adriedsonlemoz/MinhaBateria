package com.minhabateria.app.source

import java.util.Locale

object SourceProfileNameBuilder {
    fun build(
        type: EnergySourceType,
        customName: String?,
        brand: String?,
        model: String?,
        nominalPowerW: Double?
    ): String {
        customName.clean()?.let { return it }

        val identity = listOfNotNull(brand.clean(), model.clean()).joinToString(" ").trim()
        val base = identity.ifBlank { type.label }
        val power = nominalPowerW?.takeIf { it > 0.0 }?.let(::formatPower)
        return if (power == null || base.contains(power, ignoreCase = true)) base else "$base • $power"
    }

    fun formatPower(watts: Double): String {
        val value = String.format(Locale.getDefault(), "%.1f", watts)
            .removeSuffix(",0")
            .removeSuffix(".0")
        return "$value W"
    }

    private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotBlank() }
}
