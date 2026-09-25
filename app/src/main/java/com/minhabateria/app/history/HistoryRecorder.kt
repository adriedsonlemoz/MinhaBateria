package com.minhabateria.app.history

import android.content.Context
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.battery.ChargingSource
import com.minhabateria.app.source.SourceProfileStore

class HistoryRecorder(context: Context) {
    private val appContext = context.applicationContext
    private val store = HistoryStore(appContext)
    private val sourceStore = SourceProfileStore(appContext)

    fun record(completed: ChargingSession.CompletedSession, detectedSource: ChargingSource?) {
        val snapshot = completed.snapshot
        if ((snapshot.elapsedMs ?: 0L) <= 0L) return
        val profile = sourceStore.getProfile()
        store.add(
            HistoryEntry(
                id = "${completed.startedAtMs}-${completed.endedAtMs}",
                startedAtMs = completed.startedAtMs,
                endedAtMs = completed.endedAtMs,
                profileName = profile?.name,
                profileType = profile?.type?.label,
                nominalPowerW = profile?.nominalPowerW,
                detectedSource = detectedSource?.displayName(),
                elapsedMs = snapshot.elapsedMs,
                chargingTimeMs = snapshot.chargingTimeMs,
                energyWh = snapshot.energyWh,
                chargeMah = snapshot.chargeMah,
                averagePowerW = snapshot.averagePowerW,
                averageCurrentMa = snapshot.averageCurrentMa,
                averageVoltageV = snapshot.averageVoltageV,
                minPowerW = snapshot.minPowerW,
                maxPowerW = snapshot.maxPowerW,
                minCurrentMa = snapshot.minCurrentMa,
                maxCurrentMa = snapshot.maxCurrentMa,
                minVoltageV = snapshot.minVoltageV,
                maxVoltageV = snapshot.maxVoltageV,
                averageTemperatureC = snapshot.averageTemperatureC,
                maxTemperatureC = snapshot.maxTemperatureC,
                powerVariationRatio = snapshot.powerVariationRatio,
                interruptions = snapshot.interruptions,
                startPercent = snapshot.startPercent,
                endPercent = snapshot.currentPercent,
                gainPercent = snapshot.gainPercent,
                reachedFull = snapshot.reachedFull
            )
        )
    }

    private fun ChargingSource.displayName(): String = when (this) {
        ChargingSource.AC -> "Tomada / AC"
        ChargingSource.USB -> "USB"
        ChargingSource.WIRELESS -> "Sem fio"
        ChargingSource.BATTERY -> "Bateria"
        ChargingSource.UNKNOWN -> "Desconhecida"
    }
}
