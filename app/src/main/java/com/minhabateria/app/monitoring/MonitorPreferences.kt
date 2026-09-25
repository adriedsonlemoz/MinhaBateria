package com.minhabateria.app.monitoring

import android.content.Context

class MonitorPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        FILE_NAME,
        Context.MODE_PRIVATE
    )

    fun isMonitoringRequested(): Boolean = preferences.getBoolean(KEY_MONITORING_ACTIVE, false)

    fun setMonitoringRequested(active: Boolean) {
        preferences.edit().putBoolean(KEY_MONITORING_ACTIVE, active).apply()
    }

    fun shouldResumeAfterBoot(): Boolean = preferences.getBoolean(KEY_RESUME_AFTER_BOOT, false)

    fun setResumeAfterBoot(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_RESUME_AFTER_BOOT, enabled).apply()
    }

    private companion object {
        const val FILE_NAME = "monitor_preferences"
        const val KEY_MONITORING_ACTIVE = "monitoring_active"
        const val KEY_RESUME_AFTER_BOOT = "resume_after_boot"
    }
}
