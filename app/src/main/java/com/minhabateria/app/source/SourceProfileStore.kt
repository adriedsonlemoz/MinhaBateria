package com.minhabateria.app.source

import android.content.Context

class SourceProfileStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getProfile(): EnergySourceProfile? {
        val type = EnergySourceType.fromStorage(preferences.getString(KEY_TYPE, null)) ?: return null
        val name = preferences.getString(KEY_NAME, null)?.trim().orEmpty()
        if (name.isBlank()) return null
        val nominalPower = preferences.getString(KEY_POWER_W, null)?.toDoubleOrNull()
        return EnergySourceProfile(type, name, nominalPower)
    }

    fun save(profile: EnergySourceProfile) {
        val editor = preferences.edit()
            .putString(KEY_TYPE, profile.type.storageValue)
            .putString(KEY_NAME, profile.name.trim())
            .putBoolean(KEY_INITIAL_PROMPT_SEEN, true)
        if (profile.nominalPowerW == null) editor.remove(KEY_POWER_W)
        else editor.putString(KEY_POWER_W, profile.nominalPowerW.toString())
        editor.apply()
    }

    fun shouldOfferInitialSetup(): Boolean =
        getProfile() == null && !preferences.getBoolean(KEY_INITIAL_PROMPT_SEEN, false)

    fun dismissInitialSetup() {
        preferences.edit().putBoolean(KEY_INITIAL_PROMPT_SEEN, true).apply()
    }

    private companion object {
        const val PREFS_NAME = "source_profile"
        const val KEY_TYPE = "type"
        const val KEY_NAME = "name"
        const val KEY_POWER_W = "nominal_power_w"
        const val KEY_INITIAL_PROMPT_SEEN = "initial_prompt_seen"
    }
}
