package com.minhabateria.app.battery

import android.os.Handler
import android.os.Looper

class BatteryMonitor(
    private val reader: BatteryStatusReader,
    private val session: ChargingSession,
    private val onUpdate: (BatteryInfo, ChargingSession.Snapshot) -> Unit,
    private val onSessionCompleted: (ChargingSource?, ChargingSession.CompletedSession) -> Unit = { _, _ -> }
) {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private var lastConnectedSource: ChargingSource? = null

    private val updateTask = object : Runnable {
        override fun run() {
            if (!running) return
            val info = reader.read()
            if (info.isPlugged == true || (info.isPlugged == null && info.isCharging == true)) {
                if (info.source != ChargingSource.BATTERY) lastConnectedSource = info.source
            }
            val snapshot = session.update(info)
            session.takeCompletedSession()?.let { completed ->
                onSessionCompleted(lastConnectedSource, completed)
                lastConnectedSource = null
            }
            onUpdate(info, snapshot)
            handler.postDelayed(this, UPDATE_INTERVAL_MS)
        }
    }

    fun start() {
        if (running) return
        running = true
        handler.post(updateTask)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(updateTask)
    }

    private companion object {
        const val UPDATE_INTERVAL_MS = 1_000L
    }
}
