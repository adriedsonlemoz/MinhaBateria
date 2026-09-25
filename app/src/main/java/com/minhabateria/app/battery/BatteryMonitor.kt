package com.minhabateria.app.battery

import android.os.Handler
import android.os.Looper

class BatteryMonitor(
    private val reader: BatteryStatusReader,
    private val session: ChargingSession,
    private val onUpdate: (BatteryInfo, ChargingSession.Snapshot) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private val updateTask = object : Runnable {
        override fun run() {
            if (!running) return
            val info = reader.read()
            val snapshot = session.update(info)
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
