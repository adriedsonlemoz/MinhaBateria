package com.minhabateria.app.monitoring

internal object ForegroundServiceStartGuard {
    private const val START_NOT_ALLOWED_EXCEPTION =
        "android.app.ForegroundServiceStartNotAllowedException"

    fun isRecoverable(error: RuntimeException): Boolean =
        error is SecurityException || error.javaClass.name == START_NOT_ALLOWED_EXCEPTION
}
