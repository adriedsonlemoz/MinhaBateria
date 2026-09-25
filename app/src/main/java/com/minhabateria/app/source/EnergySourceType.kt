package com.minhabateria.app.source

enum class EnergySourceType(val storageValue: String, val label: String) {
    SOLAR_PANEL("solar_panel", "Painel solar"),
    CHARGER("charger", "Carregador"),
    POWER_BANK("power_bank", "Power bank"),
    OTHER("other", "Outra fonte");

    companion object {
        fun fromStorage(value: String?): EnergySourceType? =
            entries.firstOrNull { it.storageValue == value }
    }
}
