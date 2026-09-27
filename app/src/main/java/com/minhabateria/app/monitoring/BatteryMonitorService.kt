package com.minhabateria.app.monitoring

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.minhabateria.app.audio.VoiceAlertCoordinator
import com.minhabateria.app.audio.VoiceAlerts
import com.minhabateria.app.battery.BatteryMonitor
import com.minhabateria.app.battery.BatteryStatusReader
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.charts.ChartSampleRepository
import com.minhabateria.app.discharge.DischargeRecorder
import com.minhabateria.app.history.HistoryRecorder

class BatteryMonitorService : Service() {
    private lateinit var monitor: BatteryMonitor
    private lateinit var preferences: MonitorPreferences
    private lateinit var notification: MonitoringNotification
    private lateinit var sessionStore: ContinuousSessionStore
    private lateinit var dischargeRecorder: DischargeRecorder
    private lateinit var startIssueStore: MonitoringStartIssueStore
    private lateinit var sourceConnectionState: SourceConnectionState
    private lateinit var voiceAlerts: VoiceAlertCoordinator
    private var monitoringAudioAnnounced = false
    private var foregroundStarted = false
    private var lastNotificationUpdateMs = 0L

    override fun onCreate() {
        super.onCreate()
        preferences = MonitorPreferences(this)
        notification = MonitoringNotification(this)
        sessionStore = ContinuousSessionStore(this)
        dischargeRecorder = DischargeRecorder(this)
        startIssueStore = MonitoringStartIssueStore(this)
        sourceConnectionState = SourceConnectionState(this)
        voiceAlerts = VoiceAlertCoordinator(VoiceAlerts.runtime(this))
        val chargingSession = ChargingSession(sessionStore.load())
        val historyRecorder = HistoryRecorder(this)
        monitor = BatteryMonitor(
            reader = BatteryStatusReader(this),
            session = chargingSession,
            onUpdate = { info, session ->
                sourceConnectionState.observePlugged(info.isPlugged, info.source)
                ChartSampleRepository.record(this, info)
                dischargeRecorder.update(info)
                sessionStore.save(chargingSession.savedState())
                MonitoringStateStore.publish(true, info, session, dischargeRecorder.current())
                voiceAlerts.update(info, session)
                updateNotificationIfNeeded(info, session)
            },
            onSessionCompleted = { source, completed ->
                historyRecorder.record(completed, source)
                voiceAlerts.sessionEnded()
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopMonitoring()
            return START_NOT_STICKY
        }

        val automaticRestore = intent == null
        if (automaticRestore) {
            if (!preferences.isMonitoringRequested()) {
                stopSelfResult(startId)
                return START_NOT_STICKY
            }
        } else {
            preferences.setMonitoringRequested(true)
        }

        val origin = if (automaticRestore) {
            ORIGIN_SYSTEM_RESTORE
        } else {
            intent?.getStringExtra(EXTRA_START_ORIGIN).orEmpty().ifBlank { ORIGIN_SERVICE_REQUEST }
        }
        if (!startInForeground(origin)) {
            MonitoringStateStore.publish(running = false)
            stopSelfResult(startId)
            return START_NOT_STICKY
        }

        MonitoringStateStore.publish(running = true)
        if (!monitoringAudioAnnounced) {
            voiceAlerts.monitoringStarted()
            monitoringAudioAnnounced = true
        }
        monitor.start()
        return START_STICKY
    }

    override fun onDestroy() {
        monitor.stop()
        ChartSampleRepository.flush(this)
        if (!preferences.isMonitoringRequested()) sessionStore.clear()
        MonitoringStateStore.publish(running = false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startInForeground(origin: String): Boolean {
        if (foregroundStarted) return true
        val state = MonitoringStateStore.current()
        val initialNotification = notification.build(state.info, state.session, dischargeRecorder.current())
        return try {
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
            true
        } catch (error: RuntimeException) {
            if (!ForegroundServiceStartGuard.isRecoverable(error)) throw error
            startIssueStore.record(origin, error)
            false
        }
    }

    private fun updateNotificationIfNeeded(
        info: com.minhabateria.app.battery.BatteryInfo,
        session: ChargingSession.Snapshot
    ) {
        val now = System.currentTimeMillis()
        if (now - lastNotificationUpdateMs < NOTIFICATION_UPDATE_MS) return
        lastNotificationUpdateMs = now
        notification.update(info, session, dischargeRecorder.current())
    }

    private fun stopMonitoring() {
        if (monitoringAudioAnnounced) voiceAlerts.monitoringStopped()
        monitoringAudioAnnounced = false
        preferences.setMonitoringRequested(false)
        monitor.stop()
        sessionStore.clear()
        MonitoringStateStore.publish(running = false, discharge = null)
        if (foregroundStarted) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            foregroundStarted = false
        }
        stopSelf()
    }

    companion object {
        const val ACTION_START = "com.minhabateria.app.action.START_MONITORING"
        const val ACTION_STOP = "com.minhabateria.app.action.STOP_MONITORING"
        const val EXTRA_START_ORIGIN = "com.minhabateria.app.extra.START_ORIGIN"
        private const val NOTIFICATION_UPDATE_MS = 5_000L
        private const val ORIGIN_SYSTEM_RESTORE = "Restauração automática do serviço pelo Android"
        private const val ORIGIN_SERVICE_REQUEST = "Inicialização do serviço"
    }
}
