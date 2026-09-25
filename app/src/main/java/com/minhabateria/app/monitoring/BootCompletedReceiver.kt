package com.minhabateria.app.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val preferences = MonitorPreferences(context)
        if (preferences.shouldResumeAfterBoot() && preferences.isMonitoringRequested()) {
            MonitoringServiceController.start(context)
        }
    }
}
