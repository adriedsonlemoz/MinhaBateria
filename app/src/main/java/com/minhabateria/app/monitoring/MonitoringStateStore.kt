package com.minhabateria.app.monitoring

import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import java.util.concurrent.CopyOnWriteArraySet

object MonitoringStateStore {
    private val listeners = CopyOnWriteArraySet<(MonitoringState) -> Unit>()

    @Volatile
    private var state = MonitoringState()

    fun current(): MonitoringState = state

    fun addListener(listener: (MonitoringState) -> Unit) {
        listeners.add(listener)
        listener(state)
    }

    fun removeListener(listener: (MonitoringState) -> Unit) {
        listeners.remove(listener)
    }

    fun publish(
        running: Boolean,
        info: BatteryInfo? = state.info,
        session: ChargingSession.Snapshot? = state.session
    ) {
        state = MonitoringState(running, info, session)
        listeners.forEach { it(state) }
    }
}
