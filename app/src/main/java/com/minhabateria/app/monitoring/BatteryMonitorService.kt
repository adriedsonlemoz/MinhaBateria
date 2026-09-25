package com.minhabateria.app.monitoring

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.minhabateria.app.battery.BatteryMonitor
import com.minhabateria.app.battery.BatteryStatusReader
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.charts.ChartSampleRepository
import com.minhabateria.app.history.HistoryRecorder

class BatteryMonitorService : Service() {
    private lateinit var monitor: BatteryMonitor
    private lateinit var preferences: MonitorPreferences
    private lateinit var notification: MonitoringNotification
    private lateinit var sessionStore: ContinuousSessionStore
    private var foregroundStarted = false
    private var lastNotificationUpdateMs = 0L

    override fun onCreate() {
        super.onCreate()
        preferences = MonitorPreferences(this)
        notification = MonitoringNotification(this)
        sessionStore = ContinuousSessionStore(this)
        val chargingSession = ChargingSession(sessionStore.load())
        val historyRecorder = HistoryRecorder(this)
        monitor = BatteryMonitor(
            reader = BatteryStatusReader(this),
            session = chargingSession,
            onUpdate = { info, session ->
                ChartSampleRepository.record(this, info)
                sessionStore.save(chargingSession.savedState())
                MonitoringStateStore.publish(true, info, session)
                updateNotificationIfNeeded(info)
            },
            onSessionCompleted = { source, completed ->
                historyRecorder.record(completed, source)
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopMonitoring()
            return START_NOT_STICKY
        }

        preferences.setMonitoringRequested(true)
        startInForeground()
        MonitoringStateStore.publish(running = true)
        monitor.start()
        return START_STICKY
    }

    override fun onDestroy() {
        monitor.stop()
        ChartSampleRepository.flush(this)
        MonitoringStateStore.publish(running = false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startInForeground() {
        if (foregroundStarted) return
        val initialNotification = notification.build(MonitoringStateStore.current().info)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                MonitoringNotification.NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(MonitoringNotification.NOTIFICATION_ID, initialNotification)
        }
        foregroundStarted = true
    }

    private fun updateNotificationIfNeeded(info: com.minhabateria.app.battery.BatteryInfo) {
        val now = System.currentTimeMillis()
        if (now - lastNotificationUpdateMs < NOTIFICATION_UPDATE_MS) return
        lastNotificationUpdateMs = now
        notification.update(info)
    }

    private fun stopMonitoring() {
        preferences.setMonitoringRequested(false)
        monitor.stop()
        sessionStore.clear()
        MonitoringStateStore.publish(running = false)
        if (foregroundStarted) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            foregroundStarted = false
        }
        stopSelf()
    }

    companion object {
        const val ACTION_START = "com.minhabateria.app.action.START_MONITORING"
        const val ACTION_STOP = "com.minhabateria.app.action.STOP_MONITORING"
        private const val NOTIFICATION_UPDATE_MS = 5_000L
    }
}
