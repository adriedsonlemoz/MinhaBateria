package com.minhabateria.app.history

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class HistoryStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun entries(): List<HistoryEntry> {
        val raw = preferences.getString(KEY_ENTRIES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    decode(array.getJSONObject(index))?.let(::add)
                }
            }.sortedByDescending { it.endedAtMs }
        }.getOrDefault(emptyList())
    }

    fun add(entry: HistoryEntry) {
        val updated = (listOf(entry) + entries().filterNot { it.id == entry.id })
            .sortedByDescending { it.endedAtMs }
            .take(MAX_ENTRIES)
        val array = JSONArray()
        updated.forEach { array.put(encode(it)) }
        preferences.edit().putString(KEY_ENTRIES, array.toString()).apply()
    }

    fun clear() {
        preferences.edit().remove(KEY_ENTRIES).apply()
    }

    private fun encode(entry: HistoryEntry): JSONObject = JSONObject().apply {
        put("id", entry.id)
        put("startedAtMs", entry.startedAtMs)
        put("endedAtMs", entry.endedAtMs)
        putNullable("profileName", entry.profileName)
        putNullable("profileType", entry.profileType)
        putNullable("nominalPowerW", entry.nominalPowerW)
        putNullable("detectedSource", entry.detectedSource)
        putNullable("elapsedMs", entry.elapsedMs)
        putNullable("chargingTimeMs", entry.chargingTimeMs)
        putNullable("energyWh", entry.energyWh)
        putNullable("chargeMah", entry.chargeMah)
        putNullable("averagePowerW", entry.averagePowerW)
        putNullable("averageCurrentMa", entry.averageCurrentMa)
        putNullable("averageVoltageV", entry.averageVoltageV)
        putNullable("minPowerW", entry.minPowerW)
        putNullable("maxPowerW", entry.maxPowerW)
        putNullable("minCurrentMa", entry.minCurrentMa)
        putNullable("maxCurrentMa", entry.maxCurrentMa)
        putNullable("minVoltageV", entry.minVoltageV)
        putNullable("maxVoltageV", entry.maxVoltageV)
        putNullable("averageTemperatureC", entry.averageTemperatureC)
        putNullable("maxTemperatureC", entry.maxTemperatureC)
        putNullable("powerVariationRatio", entry.powerVariationRatio)
        put("interruptions", entry.interruptions)
        putNullable("startPercent", entry.startPercent)
        putNullable("endPercent", entry.endPercent)
        putNullable("gainPercent", entry.gainPercent)
        put("reachedFull", entry.reachedFull)
    }

    private fun decode(json: JSONObject): HistoryEntry? = runCatching {
        HistoryEntry(
            id = json.getString("id"),
            startedAtMs = json.getLong("startedAtMs"),
            endedAtMs = json.getLong("endedAtMs"),
            profileName = json.stringOrNull("profileName"),
            profileType = json.stringOrNull("profileType"),
            nominalPowerW = json.doubleOrNull("nominalPowerW"),
            detectedSource = json.stringOrNull("detectedSource"),
            elapsedMs = json.longOrNull("elapsedMs"),
            chargingTimeMs = json.longOrNull("chargingTimeMs"),
            energyWh = json.doubleOrNull("energyWh"),
            chargeMah = json.doubleOrNull("chargeMah"),
            averagePowerW = json.doubleOrNull("averagePowerW"),
            averageCurrentMa = json.doubleOrNull("averageCurrentMa"),
            averageVoltageV = json.doubleOrNull("averageVoltageV"),
            minPowerW = json.doubleOrNull("minPowerW"),
            maxPowerW = json.doubleOrNull("maxPowerW"),
            minCurrentMa = json.doubleOrNull("minCurrentMa"),
            maxCurrentMa = json.doubleOrNull("maxCurrentMa"),
            minVoltageV = json.doubleOrNull("minVoltageV"),
            maxVoltageV = json.doubleOrNull("maxVoltageV"),
            averageTemperatureC = json.doubleOrNull("averageTemperatureC"),
            maxTemperatureC = json.doubleOrNull("maxTemperatureC"),
            powerVariationRatio = json.doubleOrNull("powerVariationRatio"),
            interruptions = json.optInt("interruptions", 0),
            startPercent = json.intOrNull("startPercent"),
            endPercent = json.intOrNull("endPercent"),
            gainPercent = json.intOrNull("gainPercent"),
            reachedFull = json.optBoolean("reachedFull", false)
        )
    }.getOrNull()

    private fun JSONObject.putNullable(key: String, value: Any?) {
        if (value == null) put(key, JSONObject.NULL) else put(key, value)
    }

    private fun JSONObject.stringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private fun JSONObject.doubleOrNull(key: String): Double? =
        if (isNull(key) || !has(key)) null else optDouble(key).takeIf { it.isFinite() }

    private fun JSONObject.longOrNull(key: String): Long? =
        if (isNull(key) || !has(key)) null else optLong(key)

    private fun JSONObject.intOrNull(key: String): Int? =
        if (isNull(key) || !has(key)) null else optInt(key)

    private companion object {
        const val PREFS_NAME = "session_history"
        const val KEY_ENTRIES = "entries_json"
        const val MAX_ENTRIES = 100
    }
}
