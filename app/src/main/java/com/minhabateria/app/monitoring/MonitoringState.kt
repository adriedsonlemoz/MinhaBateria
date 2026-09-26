package com.minhabateria.app.monitoring

import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.discharge.ActiveDischarge

data class MonitoringState(
    val running: Boolean = false,
    val info: BatteryInfo? = null,
    val session: ChargingSession.Snapshot? = null,
    val discharge: ActiveDischarge? = null
)
