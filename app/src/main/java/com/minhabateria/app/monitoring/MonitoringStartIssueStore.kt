package com.minhabateria.app.monitoring

import android.content.Context

class MonitoringStartIssueStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        FILE_NAME,
        Context.MODE_PRIVATE
    )

    data class Issue(
        val timestampMs: Long,
        val origin: String,
        val exceptionName: String,
        val message: String
    )

    fun record(origin: String, error: Throwable) {
        preferences.edit()
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .putString(KEY_ORIGIN, origin)
            .putString(KEY_EXCEPTION, error.javaClass.name)
            .putString(KEY_MESSAGE, error.message.orEmpty().take(MAX_MESSAGE_LENGTH))
            .apply()
    }

    fun latest(): Issue? {
        val timestamp = preferences.getLong(KEY_TIMESTAMP, 0L)
        if (timestamp <= 0L) return null
        return Issue(
            timestampMs = timestamp,
            origin = preferences.getString(KEY_ORIGIN, null).orEmpty(),
            exceptionName = preferences.getString(KEY_EXCEPTION, null).orEmpty(),
            message = preferences.getString(KEY_MESSAGE, null).orEmpty()
        )
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val FILE_NAME = "monitoring_start_issue"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_ORIGIN = "origin"
        const val KEY_EXCEPTION = "exception"
        const val KEY_MESSAGE = "message"
        const val MAX_MESSAGE_LENGTH = 1_000
    }
}
