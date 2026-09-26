package com.minhabateria.app.monitoring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.minhabateria.app.MainActivity
import com.minhabateria.app.R
import com.minhabateria.app.battery.BatteryCurrentReader
import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.discharge.ActiveDischarge

class MonitoringNotification(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val currentReader = BatteryCurrentReader(context)

    init {
        createChannel()
    }

    fun build(
        info: BatteryInfo?,
        session: ChargingSession.Snapshot? = null,
        discharge: ActiveDischarge? = null
    ): Notification {
        val openApp = PendingIntent.getActivity(
            context,
            10,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopService = PendingIntent.getService(
            context,
            11,
            Intent(context, BatteryMonitorService::class.java)
                .setAction(BatteryMonitorService.ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = info?.percent?.let { "Minha Bateria — $it%" } ?: "Minha Bateria"
        val content = info?.let {
            MonitoringNotificationFormatter.format(
                info = it,
                session = session,
                discharge = discharge,
                instantCurrentMa = currentReader.readSignedMa(it)
            )
        }
        val summary = content?.summary ?: "Monitoramento contínuo ativo"
        val expanded = content?.expanded ?: summary

        return Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(summary)
            .setStyle(Notification.BigTextStyle().bigText(expanded))
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(Notification.Action.Builder(R.drawable.ic_notification, "Parar", stopService).build())
            .build()
    }

    fun update(
        info: BatteryInfo?,
        session: ChargingSession.Snapshot? = null,
        discharge: ActiveDischarge? = null
    ) {
        manager.notify(NOTIFICATION_ID, build(info, session, discharge))
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Monitoramento da bateria",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mostra o monitoramento contínuo solicitado pelo usuário."
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_ID = 1201
        private const val CHANNEL_ID = "battery_monitoring"
    }
}
