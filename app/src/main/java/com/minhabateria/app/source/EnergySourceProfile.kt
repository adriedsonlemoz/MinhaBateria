package com.minhabateria.app.source

data class EnergySourceProfile(
    val type: EnergySourceType,
    val name: String,
    val nominalPowerW: Double?
)
