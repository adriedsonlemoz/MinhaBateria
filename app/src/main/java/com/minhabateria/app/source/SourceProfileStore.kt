package com.minhabateria.app.source

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Stores user-described physical power sources.
 *
 * Legacy releases stored a single profile in individual SharedPreferences keys. Those keys are
 * still read and mirrored so upgrades keep every existing profile usable. New releases additionally
 * keep a small JSON list and an explicit active/recent profile id.
 */
class SourceProfileStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getProfile(): EnergySourceProfile? {
        val list = profiles()
        if (list.isEmpty()) return null
        val activeId = preferences.getString(KEY_ACTIVE_PROFILE_ID, null)
        return list.firstOrNull { it.id == activeId } ?: list.first()
    }

    fun recentProfile(): EnergySourceProfile? {
        val recentId = preferences.getString(KEY_RECENT_PROFILE_ID, null)
        return profiles().firstOrNull { it.id == recentId } ?: getProfile()
    }

    fun profiles(): List<EnergySourceProfile> {
        val decoded = decodeProfiles(preferences.getString(KEY_PROFILES_JSON, null))
        if (decoded.isNotEmpty()) return decoded
        val legacy = readLegacyProfile() ?: return emptyList()
        val migrated = legacy.copy(id = legacy.id.ifBlank { UUID.randomUUID().toString() })
        writeProfiles(listOf(migrated), activeId = migrated.id, recentId = migrated.id)
        return listOf(migrated)
    }

    fun save(profile: EnergySourceProfile): EnergySourceProfile {
        val existing = profiles()
        val resolvedId = profile.id.ifBlank { UUID.randomUUID().toString() }
        val saved = profile.copy(id = resolvedId)
        val updated = (existing.filterNot { it.id == resolvedId } + saved)
            .sortedBy { it.name.lowercase() }
            .take(MAX_PROFILES)
        writeProfiles(updated, activeId = saved.id, recentId = saved.id)
        mirrorLegacy(saved)
        preferences.edit().putBoolean(KEY_INITIAL_PROMPT_SEEN, true).apply()
        return saved
    }

    fun selectProfile(id: String): EnergySourceProfile? {
        val profile = profiles().firstOrNull { it.id == id } ?: return null
        preferences.edit()
            .putString(KEY_ACTIVE_PROFILE_ID, id)
            .putString(KEY_RECENT_PROFILE_ID, id)
            .apply()
        mirrorLegacy(profile)
        return profile
    }

    fun shouldOfferInitialSetup(): Boolean =
        profiles().isEmpty() && !preferences.getBoolean(KEY_INITIAL_PROMPT_SEEN, false)

    fun dismissInitialSetup() {
        preferences.edit().putBoolean(KEY_INITIAL_PROMPT_SEEN, true).apply()
    }

    private fun decodeProfiles(raw: String?): List<EnergySourceProfile> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) decode(array.getJSONObject(index))?.let(::add)
            }
        }.getOrDefault(emptyList())
    }

    private fun writeProfiles(profiles: List<EnergySourceProfile>, activeId: String?, recentId: String?) {
        val array = JSONArray()
        profiles.forEach { array.put(encode(it)) }
        preferences.edit()
            .putString(KEY_PROFILES_JSON, array.toString())
            .putOptional(KEY_ACTIVE_PROFILE_ID, activeId)
            .putOptional(KEY_RECENT_PROFILE_ID, recentId)
            .apply()
    }

    private fun encode(profile: EnergySourceProfile): JSONObject = JSONObject().apply {
        put("id", profile.id)
        put("type", profile.type.storageValue)
        put("name", profile.name)
        putNullable("nominalPowerW", profile.nominalPowerW)
        putNullable("brand", profile.brand)
        putNullable("model", profile.model)
        putNullable("labelOutputs", profile.labelOutputs)
        putNullable("technology", profile.technology)
        putNullable("portType", profile.portType)
        putNullable("cableInfo", profile.cableInfo)
        putNullable("capacityMah", profile.capacityMah)
        putNullable("ratedVoltageV", profile.ratedVoltageV)
        putNullable("ratedCurrentA", profile.ratedCurrentA)
        putNullable("controllerInfo", profile.controllerInfo)
    }

    private fun decode(json: JSONObject): EnergySourceProfile? = runCatching {
        val type = EnergySourceType.fromStorage(json.optString("type")) ?: return@runCatching null
        EnergySourceProfile(
            type = type,
            name = json.optString("name").ifBlank {
                SourceProfileNameBuilder.build(
                    type, null, json.stringOrNull("brand"), json.stringOrNull("model"), json.doubleOrNull("nominalPowerW")
                )
            },
            nominalPowerW = json.doubleOrNull("nominalPowerW"),
            brand = json.stringOrNull("brand"),
            model = json.stringOrNull("model"),
            labelOutputs = json.stringOrNull("labelOutputs"),
            technology = json.stringOrNull("technology"),
            portType = json.stringOrNull("portType"),
            cableInfo = json.stringOrNull("cableInfo"),
            capacityMah = json.intOrNull("capacityMah"),
            ratedVoltageV = json.doubleOrNull("ratedVoltageV"),
            ratedCurrentA = json.doubleOrNull("ratedCurrentA"),
            controllerInfo = json.stringOrNull("controllerInfo"),
            id = json.optString("id").ifBlank { UUID.randomUUID().toString() }
        )
    }.getOrNull()

    private fun readLegacyProfile(): EnergySourceProfile? {
        val type = EnergySourceType.fromStorage(preferences.getString(KEY_TYPE, null)) ?: return null
        val power = preferences.stringDouble(KEY_POWER_W)
        val brand = preferences.optionalString(KEY_BRAND)
        val model = preferences.optionalString(KEY_MODEL)
        val storedName = preferences.optionalString(KEY_NAME)
        return EnergySourceProfile(
            type = type,
            name = SourceProfileNameBuilder.build(type, storedName, brand, model, power),
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

    private fun mirrorLegacy(profile: EnergySourceProfile) {
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
            .apply()
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

    private fun JSONObject.putNullable(key: String, value: Any?) {
        if (value == null) put(key, JSONObject.NULL) else put(key, value)
    }

    private fun JSONObject.stringOrNull(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key).trim().takeIf { it.isNotBlank() }

    private fun JSONObject.doubleOrNull(key: String): Double? =
        if (!has(key) || isNull(key)) null else optDouble(key).takeIf { it.isFinite() }

    private fun JSONObject.intOrNull(key: String): Int? =
        if (!has(key) || isNull(key)) null else optInt(key)

    private companion object {
        const val PREFS_NAME = "source_profile"
        const val KEY_PROFILES_JSON = "profiles_json_v2"
        const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        const val KEY_RECENT_PROFILE_ID = "recent_profile_id"
        const val MAX_PROFILES = 24

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
