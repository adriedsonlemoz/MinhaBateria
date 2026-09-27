package com.minhabateria.app.monitoring

import android.content.Context
import com.minhabateria.app.battery.ChargingSource
import com.minhabateria.app.source.EnergySourceProfile
import com.minhabateria.app.source.SourceProfileStore

/** Tracks Android power connection events separately from the physical source described by the user. */
class SourceConnectionState(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun onPowerConnected(nowMs: Long = System.currentTimeMillis()) {
        transition(plugged = true, source = null, nowMs = nowMs)
    }

    fun onPowerDisconnected(nowMs: Long = System.currentTimeMillis()) {
        transition(plugged = false, source = null, nowMs = nowMs)
    }

    fun observePlugged(plugged: Boolean?, source: ChargingSource?, nowMs: Long = System.currentTimeMillis()) {
        if (plugged == null) return
        transition(plugged, source, nowMs)
    }

    fun shouldPrompt(): Boolean = prefs.getBoolean(KEY_PENDING_PROMPT, false)

    fun markPromptShown() {
        prefs.edit()
            .putBoolean(KEY_PENDING_PROMPT, false)
            .putLong(KEY_LAST_PROMPT_AT, System.currentTimeMillis())
            .apply()
    }

    fun assignProfile(profile: EnergySourceProfile) {
        val saved = SourceProfileStore(appContext).selectProfile(profile.id) ?: profile
        prefs.edit()
            .putString(KEY_SESSION_PROFILE_ID, saved.id)
            .putLong(KEY_PROFILE_ASSIGNED_AT, System.currentTimeMillis())
            .putBoolean(KEY_ASSIGNMENT_REQUIRED, false)
            .apply()
    }

    fun sessionProfile(): EnergySourceProfile? {
        val id = prefs.getString(KEY_SESSION_PROFILE_ID, null) ?: return null
        return SourceProfileStore(appContext).profiles().firstOrNull { it.id == id }
    }

    fun profileForHistory(legacyFallback: EnergySourceProfile?): EnergySourceProfile? {
        sessionProfile()?.let { return it }
        return if (prefs.getBoolean(KEY_ASSIGNMENT_REQUIRED, false)) null else legacyFallback
    }

    fun clearSessionAssignment() {
        prefs.edit()
            .remove(KEY_SESSION_PROFILE_ID)
            .remove(KEY_PROFILE_ASSIGNED_AT)
            .remove(KEY_ASSIGNMENT_REQUIRED)
            .apply()
    }

    fun detectedTransportLabel(): String? = prefs.getString(KEY_DETECTED_TRANSPORT, null)

    private fun transition(plugged: Boolean, source: ChargingSource?, nowMs: Long) {
        val hasState = prefs.contains(KEY_CONNECTED)
        val wasConnected = prefs.getBoolean(KEY_CONNECTED, false)
        if (source != null && source != ChargingSource.BATTERY && source != ChargingSource.UNKNOWN) {
            prefs.edit().putString(KEY_DETECTED_TRANSPORT, source.simpleLabel()).apply()
        }
        if (hasState && wasConnected == plugged) return

        if (plugged) {
            val lastDisconnected = prefs.getLong(KEY_LAST_DISCONNECTED_AT, 0L)
            val quickReconnect = lastDisconnected > 0L && nowMs - lastDisconnected <= RECONNECT_GRACE_MS
            val editor = prefs.edit()
                .putBoolean(KEY_CONNECTED, true)
                .putLong(KEY_LAST_CONNECTED_AT, nowMs)
            if (!quickReconnect) {
                editor.putBoolean(KEY_PENDING_PROMPT, true)
                    .putBoolean(KEY_ASSIGNMENT_REQUIRED, true)
                // A genuinely new connection starts a new source-assignment opportunity.
                editor.remove(KEY_SESSION_PROFILE_ID).remove(KEY_PROFILE_ASSIGNED_AT)
            }
            editor.apply()
        } else {
            prefs.edit()
                .putBoolean(KEY_CONNECTED, false)
                .putLong(KEY_LAST_DISCONNECTED_AT, nowMs)
                .apply()
        }
    }

    private fun ChargingSource.simpleLabel(): String = when (this) {
        ChargingSource.AC -> "AC"
        ChargingSource.USB -> "USB"
        ChargingSource.WIRELESS -> "sem fio"
        ChargingSource.BATTERY -> "bateria"
        ChargingSource.UNKNOWN -> "não identificado"
    }

    companion object {
        const val RECONNECT_GRACE_MS = 15_000L
        private const val PREFS = "source_connection_state"
        private const val KEY_CONNECTED = "connected"
        private const val KEY_LAST_CONNECTED_AT = "last_connected_at"
        private const val KEY_LAST_DISCONNECTED_AT = "last_disconnected_at"
        private const val KEY_LAST_PROMPT_AT = "last_prompt_at"
        private const val KEY_PENDING_PROMPT = "pending_prompt"
        private const val KEY_SESSION_PROFILE_ID = "session_profile_id"
        private const val KEY_PROFILE_ASSIGNED_AT = "profile_assigned_at"
        private const val KEY_ASSIGNMENT_REQUIRED = "assignment_required"
        private const val KEY_DETECTED_TRANSPORT = "detected_transport"
    }
}
