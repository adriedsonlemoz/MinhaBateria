package com.minhabateria.app.monitoring

import android.content.Context
import android.content.Intent

object MonitoringServiceController {
    fun start(context: Context, origin: String = ORIGIN_APP_REQUEST): Boolean {
        val appContext = context.applicationContext
        MonitorPreferences(appContext).setMonitoringRequested(true)
        val intent = Intent(appContext, BatteryMonitorService::class.java)
            .setAction(BatteryMonitorService.ACTION_START)
            .putExtra(BatteryMonitorService.EXTRA_START_ORIGIN, origin)

        return try {
            appContext.startForegroundService(intent)
            true
        } catch (error: RuntimeException) {
            if (!ForegroundServiceStartGuard.isRecoverable(error)) throw error
            MonitoringStartIssueStore(appContext).record(origin, error)
            MonitoringStateStore.publish(running = false)
            false
        }
    }

    fun stop(context: Context) {
        val appContext = context.applicationContext
        MonitorPreferences(appContext).setMonitoringRequested(false)
        ContinuousSessionStore(appContext).clear()
        appContext.stopService(Intent(appContext, BatteryMonitorService::class.java))
        MonitoringStateStore.publish(running = false, discharge = null)
    }

    fun restoreIfRequested(context: Context) {
        if (MonitorPreferences(context).isMonitoringRequested()) {
            start(context, ORIGIN_APP_FOREGROUND_RESTORE)
        }
    }

    const val ORIGIN_USER = "Iniciado pelo usuário"
    const val ORIGIN_BOOT = "Retomada após reinício do aparelho"
    private const val ORIGIN_APP_REQUEST = "Solicitação do aplicativo"
    private const val ORIGIN_APP_FOREGROUND_RESTORE = "Retomada ao abrir o aplicativo"
}
