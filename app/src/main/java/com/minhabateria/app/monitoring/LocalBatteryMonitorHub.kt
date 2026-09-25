package com.minhabateria.app.monitoring

import android.content.Context
import com.minhabateria.app.battery.BatteryMonitor
import com.minhabateria.app.battery.BatteryStatusReader
import com.minhabateria.app.battery.ChargingSession

object LocalBatteryMonitorHub {
    private val clients = mutableSetOf<Any>()
    private val session = ChargingSession()
    private var monitor: BatteryMonitor? = null

    fun attach(client: Any, context: Context) {
        clients.add(client)
        ensureMonitor(context.applicationContext)
        sync(context)
    }

    fun detach(client: Any) {
        clients.remove(client)
        if (clients.isEmpty()) monitor?.stop()
    }

    fun sync(context: Context) {
        val shouldRun = clients.isNotEmpty() && !MonitorPreferences(context).isMonitoringRequested()
        if (shouldRun) monitor?.start() else monitor?.stop()
    }

    private fun ensureMonitor(context: Context) {
        if (monitor != null) return
        monitor = BatteryMonitor(
            reader = BatteryStatusReader(context),
            session = session,
            onUpdate = { info, snapshot ->
                MonitoringStateStore.publish(
                    running = MonitoringStateStore.current().running,
                    info = info,
                    session = snapshot
                )
            }
        )
    }
}
