package com.minhabateria.app.source

data class SourcePreset(
    val label: String,
    val brand: String? = null,
    val model: String? = null,
    val powerW: Double? = null,
    val technology: String? = null,
    val portType: String? = null,
    val outputs: String? = null,
    val capacityMah: Int? = null,
    val ratedVoltageV: Double? = null,
    val ratedCurrentA: Double? = null,
    val controllerInfo: String? = null,
    val cableInfo: String? = null
)
