package com.minhabateria.app.discharge

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class DischargeStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun active(): ActiveDischarge? {
        val raw = preferences.getString(KEY_ACTIVE, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            ActiveDischarge(
                startedAtMs = json.getLong("startedAtMs"),
                startPercent = json.getInt("startPercent"),
                lastObservedAtMs = json.getLong("lastObservedAtMs"),
                currentPercent = json.getInt("currentPercent")
            )
        }.getOrNull()
    }

    fun saveActive(session: ActiveDischarge) {
        val json = JSONObject()
            .put("startedAtMs", session.startedAtMs)
            .put("startPercent", session.startPercent)
            .put("lastObservedAtMs", session.lastObservedAtMs)
            .put("currentPercent", session.currentPercent)
        preferences.edit().putString(KEY_ACTIVE, json.toString()).apply()
    }

    fun clearActive() {
        preferences.edit().remove(KEY_ACTIVE).apply()
    }

    fun resetActive(session: ActiveDischarge?) {
        val editor = preferences.edit()
        if (session == null) {
            editor.remove(KEY_ACTIVE)
        } else {
            val json = JSONObject()
                .put("startedAtMs", session.startedAtMs)
                .put("startPercent", session.startPercent)
                .put("lastObservedAtMs", session.lastObservedAtMs)
                .put("currentPercent", session.currentPercent)
            editor.putString(KEY_ACTIVE, json.toString())
        }
        editor.putLong(KEY_ACTIVE_REVISION, System.currentTimeMillis()).apply()
    }

    fun activeRevision(): Long = preferences.getLong(KEY_ACTIVE_REVISION, 0L)

    fun entries(): List<CompletedDischarge> {
        val raw = preferences.getString(KEY_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    add(
                        CompletedDischarge(
                            id = json.getString("id"),
                            startedAtMs = json.getLong("startedAtMs"),
                            endedAtMs = json.getLong("endedAtMs"),
                            startPercent = json.getInt("startPercent"),
                            endPercent = json.getInt("endPercent")
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    fun add(entry: CompletedDischarge) {
        val updated = (listOf(entry) + entries())
            .distinctBy { it.id }
            .take(MAX_HISTORY)
        val array = JSONArray()
        updated.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("startedAtMs", item.startedAtMs)
                    .put("endedAtMs", item.endedAtMs)
                    .put("startPercent", item.startPercent)
                    .put("endPercent", item.endPercent)
            )
        }
        preferences.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun clearHistory() {
        preferences.edit().remove(KEY_HISTORY).apply()
    }

    private companion object {
        const val PREFS = "discharge_sessions"
        const val KEY_ACTIVE = "active"
        const val KEY_HISTORY = "history"
        const val KEY_ACTIVE_REVISION = "active_revision"
        const val MAX_HISTORY = 50
    }
}
