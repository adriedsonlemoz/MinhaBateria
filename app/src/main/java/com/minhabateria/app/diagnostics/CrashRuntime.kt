package com.minhabateria.app.diagnostics

import android.os.SystemClock

object CrashRuntime {
    @Volatile
    var startedAtElapsedMs: Long = SystemClock.elapsedRealtime()

    @Volatile
    var currentScreen: String = "Inicialização"
}
