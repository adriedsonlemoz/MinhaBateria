package com.minhabateria.app.audio

import android.content.Context
import android.net.Uri

class VoiceAlertPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, true)
    fun setEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()

    fun volume(): Int = prefs.getInt(KEY_VOLUME, 85).coerceIn(0, 100)
    fun setVolume(value: Int) = prefs.edit().putInt(KEY_VOLUME, value.coerceIn(0, 100)).apply()

    fun allowBackground(): Boolean = prefs.getBoolean(KEY_BACKGROUND, true)
    fun setAllowBackground(enabled: Boolean) = prefs.edit().putBoolean(KEY_BACKGROUND, enabled).apply()

    fun isEventEnabled(event: VoiceAlertEvent): Boolean = prefs.getBoolean(eventEnabledKey(event), true)
    fun setEventEnabled(event: VoiceAlertEvent, enabled: Boolean) =
        prefs.edit().putBoolean(eventEnabledKey(event), enabled).apply()

    fun customUri(event: VoiceAlertEvent): Uri? =
        prefs.getString(eventUriKey(event), null)?.let { runCatching { Uri.parse(it) }.getOrNull() }

    fun setCustomUri(event: VoiceAlertEvent, uri: Uri) =
        prefs.edit().putString(eventUriKey(event), uri.toString()).apply()

    fun clearCustomUri(event: VoiceAlertEvent) = prefs.edit().remove(eventUriKey(event)).apply()

    private fun eventEnabledKey(event: VoiceAlertEvent) = "event_enabled_${event.name.lowercase()}"
    private fun eventUriKey(event: VoiceAlertEvent) = "event_uri_${event.name.lowercase()}"

    private companion object {
        const val PREFS = "voice_alert_preferences"
        const val KEY_ENABLED = "voice_alerts_enabled"
        const val KEY_VOLUME = "voice_alert_volume"
        const val KEY_BACKGROUND = "voice_alert_background"
    }
}
