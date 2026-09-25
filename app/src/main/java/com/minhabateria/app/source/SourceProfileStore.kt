package com.minhabateria.app.source

import android.content.Context
import android.content.SharedPreferences

class SourceProfileStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getProfile(): EnergySourceProfile? {
        val type = EnergySourceType.fromStorage(preferences.getString(KEY_TYPE, null)) ?: return null
        val power = preferences.stringDouble(KEY_POWER_W)
        val brand = preferences.optionalString(KEY_BRAND)
        val model = preferences.optionalString(KEY_MODEL)
        val storedName = preferences.optionalString(KEY_NAME)
        val name = SourceProfileNameBuilder.build(type, storedName, brand, model, power)

        return EnergySourceProfile(
            type = type,
            name = name,
            nominalPowerW = power,
            brand = brand,
            model = model,
            labelOutputs = preferences.optionalString(KEY_OUTPUTS),
            technology = preferences.optionalString(KEY_TECHNOLOGY),
            portType = preferences.optionalString(KEY_PORT),
            cableInfo = preferences.optionalString(KEY_CABLE),
            capacityMah = preferences.getString(KEY_CAPACITY_MAH, null)?.toIntOrNull(),
            ratedVoltageV = preferences.stringDouble(KEY_VOLTAGE_V),
            ratedCurrentA = preferences.stringDouble(KEY_CURRENT_A),
            controllerInfo = preferences.optionalString(KEY_CONTROLLER)
        )
    }

    fun save(profile: EnergySourceProfile) {
        preferences.edit()
            .putString(KEY_TYPE, profile.type.storageValue)
            .putOptional(KEY_NAME, profile.name)
            .putOptionalDouble(KEY_POWER_W, profile.nominalPowerW)
            .putOptional(KEY_BRAND, profile.brand)
            .putOptional(KEY_MODEL, profile.model)
            .putOptional(KEY_OUTPUTS, profile.labelOutputs)
            .putOptional(KEY_TECHNOLOGY, profile.technology)
            .putOptional(KEY_PORT, profile.portType)
            .putOptional(KEY_CABLE, profile.cableInfo)
            .putOptionalInt(KEY_CAPACITY_MAH, profile.capacityMah)
            .putOptionalDouble(KEY_VOLTAGE_V, profile.ratedVoltageV)
            .putOptionalDouble(KEY_CURRENT_A, profile.ratedCurrentA)
            .putOptional(KEY_CONTROLLER, profile.controllerInfo)
            .putBoolean(KEY_INITIAL_PROMPT_SEEN, true)
            .apply()
    }

    fun shouldOfferInitialSetup(): Boolean =
        getProfile() == null && !preferences.getBoolean(KEY_INITIAL_PROMPT_SEEN, false)

    fun dismissInitialSetup() {
        preferences.edit().putBoolean(KEY_INITIAL_PROMPT_SEEN, true).apply()
    }

    private fun SharedPreferences.optionalString(key: String): String? =
        getString(key, null)?.trim()?.takeIf { it.isNotBlank() }

    private fun SharedPreferences.stringDouble(key: String): Double? =
        getString(key, null)?.toDoubleOrNull()

    private fun SharedPreferences.Editor.putOptional(key: String, value: String?): SharedPreferences.Editor =
        if (value.isNullOrBlank()) remove(key) else putString(key, value.trim())

    private fun SharedPreferences.Editor.putOptionalDouble(key: String, value: Double?): SharedPreferences.Editor =
        if (value == null) remove(key) else putString(key, value.toString())

    private fun SharedPreferences.Editor.putOptionalInt(key: String, value: Int?): SharedPreferences.Editor =
        if (value == null) remove(key) else putString(key, value.toString())

    private companion object {
        const val PREFS_NAME = "source_profile"
        const val KEY_TYPE = "type"
        const val KEY_NAME = "name"
        const val KEY_POWER_W = "nominal_power_w"
        const val KEY_BRAND = "brand"
        const val KEY_MODEL = "model"
        const val KEY_OUTPUTS = "label_outputs"
        const val KEY_TECHNOLOGY = "technology"
        const val KEY_PORT = "port_type"
        const val KEY_CABLE = "cable_info"
        const val KEY_CAPACITY_MAH = "capacity_mah"
        const val KEY_VOLTAGE_V = "rated_voltage_v"
        const val KEY_CURRENT_A = "rated_current_a"
        const val KEY_CONTROLLER = "controller_info"
        const val KEY_INITIAL_PROMPT_SEEN = "initial_prompt_seen"
    }
}
