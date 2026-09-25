package com.minhabateria.app.utils

import java.util.Locale

object TimeFormatter {
    fun elapsed(milliseconds: Long?): String {
        if (milliseconds == null) return "—"

        val totalSeconds = milliseconds / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
}
