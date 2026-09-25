package com.minhabateria.app.source

object SourceProfileFormatter {
    fun settingsSummary(profile: EnergySourceProfile): String {
        val details = mutableListOf(profile.type.label)
        profile.nominalPowerW?.let { details += "Máx. ${SourceProfileNameBuilder.formatPower(it)}" }
        profile.technology?.takeIf { it.isNotBlank() }?.let(details::add)
        profile.portType?.takeIf { it.isNotBlank() }?.let(details::add)
        profile.capacityMah?.let { details += "$it mAh" }
        return "${profile.name}\n${details.joinToString(" • ")}"
    }
}
