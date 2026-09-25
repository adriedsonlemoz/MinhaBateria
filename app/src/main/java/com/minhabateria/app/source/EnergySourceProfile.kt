package com.minhabateria.app.source

data class EnergySourceProfile(
    val type: EnergySourceType,
    val name: String,
    val nominalPowerW: Double?,
    val brand: String? = null,
    val model: String? = null,
    val labelOutputs: String? = null,
    val technology: String? = null,
    val portType: String? = null,
    val cableInfo: String? = null,
    val capacityMah: Int? = null,
    val ratedVoltageV: Double? = null,
    val ratedCurrentA: Double? = null,
    val controllerInfo: String? = null
)
