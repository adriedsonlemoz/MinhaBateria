package com.minhabateria.app.source

object SourcePresetCatalog {
    fun brands(type: EnergySourceType): List<String> = when (type) {
        EnergySourceType.CHARGER -> listOf(
            "Samsung", "Motorola", "Xiaomi", "Apple", "Baseus", "UGREEN", "Geonav", "Anker",
            "Sem marca / genérico", "Outra marca", "Digitar manualmente"
        )
        EnergySourceType.POWER_BANK -> listOf(
            "Baseus", "Geonav", "Anker", "Xiaomi", "Samsung", "UGREEN", "Sem marca / genérico", "Outra marca", "Digitar manualmente"
        )
        EnergySourceType.SOLAR_PANEL -> listOf(
            "X-TRAD", "EcoFlow", "Bluetti", "Anker", "Renogy", "Sem marca / genérico",
            "Outra marca", "Digitar manualmente"
        )
        EnergySourceType.OTHER -> listOf("Sem marca / genérico", "Outra marca", "Digitar manualmente")
    }

    fun models(type: EnergySourceType, brand: String?): List<SourcePreset> = when (type) {
        EnergySourceType.CHARGER -> chargerModels(brand)
        EnergySourceType.POWER_BANK -> powerBankPresets(brand)
        EnergySourceType.SOLAR_PANEL -> solarPresets(brand)
        EnergySourceType.OTHER -> genericOtherPresets(brand)
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
            "5V⎓2A", "5V⎓3A", "9V⎓2A", "9V⎓2,77A", "9V⎓3A", "12V⎓1,5A",
            "12V⎓3A", "15V⎓3A", "20V⎓2,25A", "PPS 3,3–11V", "Outra"
        )
        else -> emptyList()
    }

    fun capacities(): List<Int> = listOf(5_000, 10_000, 20_000, 30_000, 50_000)
    fun voltages(): List<Double> = listOf(5.0, 6.0, 9.0, 12.0, 18.0, 20.0)
    fun currents(): List<Double> = listOf(0.5, 1.0, 1.5, 1.6, 2.0, 2.4, 3.0, 5.0)
    fun controllers(): List<String> = listOf("Saída USB integrada", "Controlador PWM", "Controlador MPPT", "Conversor DC-DC USB", "Sem controlador", "Outro")
    fun cables(): List<String> = listOf("Cabo 3 A / 60 W", "Cabo 5 A / 100 W", "Cabo 5 A / 240 W", "Cabo USB-A → USB-C", "Cabo USB-C → USB-C", "Outro")

    private fun chargerModels(brand: String?): List<SourcePreset> {
        val normalized = brand?.trim()?.lowercase()
        val exact = when (normalized) {
            "samsung" -> listOf(
                SourcePreset("EP-TA800 • 25 W", "Samsung", "EP-TA800", 25.0, "USB-PD + PPS", "USB-C", "9V⎓2,77A"),
                SourcePreset("EP-T4510 • 45 W", "Samsung", "EP-T4510", 45.0, "USB-PD + PPS", "USB-C")
            )
            "apple" -> listOf(
                SourcePreset("A2305 • 20 W", "Apple", "A2305", 20.0, "USB-PD", "USB-C")
            )
            else -> emptyList()
        }
        if (exact.isNotEmpty()) return exact
        val clean = cleanBrand(brand)
        return listOf(
            SourcePreset("USB-C 20 W • PD", clean, null, 20.0, "USB-PD", "USB-C"),
            SourcePreset("USB-C 25 W • PD/PPS", clean, null, 25.0, "USB-PD + PPS", "USB-C"),
            SourcePreset("USB-C 33 W", clean, null, 33.0, null, "USB-C"),
            SourcePreset("USB-C 45 W • PD/PPS", clean, null, 45.0, "USB-PD + PPS", "USB-C"),
            SourcePreset("USB-C 65 W • PD", clean, null, 65.0, "USB-PD", "USB-C")
        )
    }

    private fun powerBankPresets(brand: String?): List<SourcePreset> {
        val clean = cleanBrand(brand)
        return listOf(
            SourcePreset("10.000 mAh • 20 W", clean, null, 20.0, "USB-PD", "USB-C", capacityMah = 10_000),
            SourcePreset("20.000 mAh • 22,5 W", clean, null, 22.5, null, "USB-C + USB-A", capacityMah = 20_000),
            SourcePreset("20.000 mAh • 30 W", clean, null, 30.0, "USB-PD", "USB-C", capacityMah = 20_000),
            SourcePreset("30.000 mAh • 30 W", clean, null, 30.0, "USB-PD", "USB-C + USB-A", capacityMah = 30_000)
        )
    }

    private fun solarPresets(brand: String?): List<SourcePreset> {
        val clean = cleanBrand(brand)
        if (brand?.trim()?.equals("X-TRAD", ignoreCase = true) == true) {
            return listOf(
                SourcePreset(
                    label = "SH-106 • 8 W",
                    brand = "X-TRAD",
                    model = "SH-106",
                    powerW = 8.0,
                    ratedVoltageV = 5.0,
                    ratedCurrentA = 1.6
                )
            )
        }
        return listOf(
            SourcePreset(
                label = "Painel solar 8 W • 5 V • 1,6 A",
                brand = clean,
                model = "Painel solar 8 W",
                powerW = 8.0,
                ratedVoltageV = 5.0,
                ratedCurrentA = 1.6
            ),
            SourcePreset("Painel 20 W", clean, "Painel solar 20 W", 20.0),
            SourcePreset("Painel 30 W", clean, "Painel solar 30 W", 30.0),
            SourcePreset("Painel 60 W", clean, "Painel solar 60 W", 60.0)
        )
    }

    private fun genericOtherPresets(brand: String?): List<SourcePreset> {
        val clean = cleanBrand(brand)
        return listOf(
            SourcePreset("Fonte USB 5 W", clean, "Fonte USB", 5.0),
            SourcePreset("Fonte USB 10 W", clean, "Fonte USB", 10.0),
            SourcePreset("Fonte USB 20 W", clean, "Fonte USB", 20.0)
        )
    }

    private fun cleanBrand(brand: String?): String? = brand?.takeUnless {
        it.equals("Outra", ignoreCase = true) ||
            it.equals("Outra marca", ignoreCase = true) ||
            it.equals("Digitar manualmente", ignoreCase = true) ||
            it.equals("Sem marca / genérico", ignoreCase = true)
    }
}
