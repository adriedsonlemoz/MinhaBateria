package com.minhabateria.app.source

object SourcePresetCatalog {
    fun brands(type: EnergySourceType): List<String> = when (type) {
        EnergySourceType.CHARGER -> listOf(
            "Samsung", "Motorola", "Xiaomi", "Apple", "Baseus", "UGREEN", "Geonav", "Anker", "Outra"
        )
        EnergySourceType.POWER_BANK -> listOf(
            "Baseus", "Geonav", "Anker", "Xiaomi", "Samsung", "UGREEN", "Outra"
        )
        EnergySourceType.SOLAR_PANEL -> listOf(
            "Sem marca / genérico", "EcoFlow", "Bluetti", "Anker", "Renogy", "Outra"
        )
        EnergySourceType.OTHER -> listOf("Sem marca / genérico", "Outra")
    }

    fun models(type: EnergySourceType, brand: String?): List<SourcePreset> = when (type) {
        EnergySourceType.CHARGER -> chargerModels(brand)
        EnergySourceType.POWER_BANK -> powerBankPresets(brand)
        EnergySourceType.SOLAR_PANEL -> solarPresets(brand)
        EnergySourceType.OTHER -> emptyList()
    }

    fun powers(type: EnergySourceType): List<Double> = when (type) {
        EnergySourceType.CHARGER -> listOf(5.0, 10.0, 15.0, 18.0, 20.0, 25.0, 30.0, 33.0, 45.0, 65.0, 67.0, 100.0, 120.0, 140.0)
        EnergySourceType.POWER_BANK -> listOf(10.0, 15.0, 18.0, 20.0, 22.5, 25.0, 30.0, 45.0, 65.0, 100.0)
        EnergySourceType.SOLAR_PANEL -> listOf(5.0, 8.0, 10.0, 20.0, 30.0, 50.0, 60.0, 100.0)
        EnergySourceType.OTHER -> listOf(5.0, 10.0, 20.0, 25.0, 30.0, 45.0, 65.0, 100.0)
    }

    fun technologies(type: EnergySourceType): List<String> = when (type) {
        EnergySourceType.CHARGER, EnergySourceType.POWER_BANK -> listOf(
            "USB-PD", "USB-PD 3.0", "USB-PD 3.1", "PPS", "USB-PD + PPS",
            "Quick Charge 3.0", "Quick Charge 4+", "Samsung AFC", "Super Fast Charging", "Outra"
        )
        else -> emptyList()
    }

    fun ports(type: EnergySourceType): List<String> = when (type) {
        EnergySourceType.CHARGER, EnergySourceType.POWER_BANK -> listOf(
            "USB-C", "USB-A", "USB-C + USB-A", "Duas USB-C", "Outra"
        )
        else -> emptyList()
    }

    fun outputs(type: EnergySourceType): List<String> = when (type) {
        EnergySourceType.CHARGER, EnergySourceType.POWER_BANK -> listOf(
            "5V⎓2A",
            "5V⎓3A",
            "9V⎓2A",
            "9V⎓2,77A",
            "9V⎓3A",
            "12V⎓1,5A",
            "12V⎓3A",
            "15V⎓3A",
            "20V⎓2,25A",
            "PPS 3,3–11V",
            "Outra"
        )
        else -> emptyList()
    }

    fun capacities(): List<Int> = listOf(5_000, 10_000, 20_000, 30_000, 50_000)

    private fun chargerModels(brand: String?): List<SourcePreset> = when (brand?.trim()?.lowercase()) {
        "samsung" -> listOf(
            SourcePreset(
                label = "EP-TA800 • 25 W",
                brand = "Samsung",
                model = "EP-TA800",
                powerW = 25.0,
                technology = "USB-PD + PPS",
                portType = "USB-C"
            ),
            SourcePreset(
                label = "EP-T4510 • 45 W",
                brand = "Samsung",
                model = "EP-T4510",
                powerW = 45.0,
                technology = "USB-PD + PPS",
                portType = "USB-C"
            )
        )
        "apple" -> listOf(
            SourcePreset(
                label = "A2305 • 20 W",
                brand = "Apple",
                model = "A2305",
                powerW = 20.0,
                technology = "USB-PD",
                portType = "USB-C"
            )
        )
        else -> emptyList()
    }

    private fun powerBankPresets(brand: String?): List<SourcePreset> = listOf(
        SourcePreset("10.000 mAh", brand = cleanBrand(brand), capacityMah = 10_000),
        SourcePreset("20.000 mAh", brand = cleanBrand(brand), capacityMah = 20_000),
        SourcePreset("30.000 mAh", brand = cleanBrand(brand), capacityMah = 30_000)
    )

    private fun solarPresets(brand: String?): List<SourcePreset> = listOf(
        SourcePreset("Painel 8 W", brand = cleanBrand(brand), powerW = 8.0),
        SourcePreset("Painel 20 W", brand = cleanBrand(brand), powerW = 20.0),
        SourcePreset("Painel 30 W", brand = cleanBrand(brand), powerW = 30.0),
        SourcePreset("Painel 60 W", brand = cleanBrand(brand), powerW = 60.0)
    )

    private fun cleanBrand(brand: String?): String? = brand?.takeUnless {
        it.equals("Outra", ignoreCase = true) || it.equals("Sem marca / genérico", ignoreCase = true)
    }
}
