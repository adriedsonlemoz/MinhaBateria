package com.minhabateria.app.diagnostics

import android.content.Context
import android.os.Process
import kotlin.system.exitProcess

class CrashHandler private constructor(
    private val appContext: Context,
    private val previous: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        runCatching {
            val report = CrashReportBuilder.build(appContext, thread, throwable)
            CrashStore.save(appContext, report)
        }
        if (previous != null && previous !== this) {
            previous.uncaughtException(thread, throwable)
        } else {
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    companion object {
        fun install(context: Context) {
            val current = Thread.getDefaultUncaughtExceptionHandler()
            if (current is CrashHandler) return
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context.applicationContext, current))
        }
    }
}
