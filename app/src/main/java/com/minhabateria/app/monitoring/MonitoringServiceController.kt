package com.minhabateria.app.monitoring

import android.content.Context
import android.content.Intent

object MonitoringServiceController {
    fun start(context: Context) {
        val appContext = context.applicationContext
        MonitorPreferences(appContext).setMonitoringRequested(true)
        val intent = Intent(appContext, BatteryMonitorService::class.java)
            .setAction(BatteryMonitorService.ACTION_START)
        appContext.startForegroundService(intent)
    }

    fun stop(context: Context) {
        val appContext = context.applicationContext
        MonitorPreferences(appContext).setMonitoringRequested(false)
        ContinuousSessionStore(appContext).clear()
        appContext.stopService(Intent(appContext, BatteryMonitorService::class.java))
        MonitoringStateStore.publish(running = false)
    }

    fun restoreIfRequested(context: Context) {
        if (MonitorPreferences(context).isMonitoringRequested()) {
            start(context)
        }
    }
}
