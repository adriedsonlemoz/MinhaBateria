package com.minhabateria.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import com.minhabateria.app.audio.AppVisibility
import com.minhabateria.app.audio.VoiceAlertRuntime
import com.minhabateria.app.diagnostics.CrashHandler
import com.minhabateria.app.diagnostics.CrashRuntime
import com.minhabateria.app.source.SourceConnectionPromptController

class MinhaBateriaApplication : Application(), Application.ActivityLifecycleCallbacks {
    lateinit var voiceAlertRuntime: VoiceAlertRuntime
        private set

    override fun onCreate() {
        super.onCreate()
        voiceAlertRuntime = VoiceAlertRuntime(this)
        CrashRuntime.startedAtElapsedMs = SystemClock.elapsedRealtime()
        registerActivityLifecycleCallbacks(this)
        CrashHandler.install(this)
    }

    override fun onActivityResumed(activity: Activity) {
        AppVisibility.onResumed()
        CrashRuntime.currentScreen = activity.javaClass.simpleName
        SourceConnectionPromptController.maybeShow(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        AppVisibility.onPaused()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
