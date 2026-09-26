package com.minhabateria.app.discharge

import android.content.Context
import com.minhabateria.app.battery.BatteryInfo

class DischargeRecorder(context: Context) {
    private val store = DischargeStore(context)
    private var active = store.active()
    private var lastPersistedAtMs = active?.lastObservedAtMs ?: 0L
    private var observedRevision = store.activeRevision()

    fun update(info: BatteryInfo, nowMs: Long = System.currentTimeMillis()) {
        val revision = store.activeRevision()
        if (revision != observedRevision) {
            active = store.active()
            observedRevision = revision
        }
        val percent = info.percent ?: return
        val unplugged = info.isPlugged == false || (info.isPlugged == null && info.isCharging == false)
        if (!unplugged) {
            finish(nowMs)
            return
        }

        val current = active
        if (current == null || nowMs - current.lastObservedAtMs > MAX_OBSERVATION_GAP_MS) {
            active = ActiveDischarge(nowMs, percent, nowMs, percent)
            store.saveActive(active!!)
            lastPersistedAtMs = nowMs
            return
        }

        val updated = current.copy(lastObservedAtMs = nowMs, currentPercent = percent)
        active = updated
        val percentChanged = updated.currentPercent != current.currentPercent
        if (percentChanged || nowMs - lastPersistedAtMs >= PERSIST_INTERVAL_MS) {
            store.saveActive(updated)
            lastPersistedAtMs = nowMs
        }
    }

    fun current(): ActiveDischarge? = active

    fun reset(info: BatteryInfo?, nowMs: Long = System.currentTimeMillis()) {
        val percent = info?.percent
        val unplugged = info?.isPlugged == false || (info?.isPlugged == null && info?.isCharging == false)
        active = if (unplugged && percent != null) {
            ActiveDischarge(nowMs, percent, nowMs, percent).also(store::resetActive)
        } else {
            store.resetActive(null)
            null
        }
        observedRevision = store.activeRevision()
        lastPersistedAtMs = nowMs
    }

    private fun finish(nowMs: Long) {
        val current = active ?: return
        if (current.dropPercent > 0 && current.durationMs >= MIN_COMPLETED_DURATION_MS) {
            store.add(
                CompletedDischarge(
                    id = "${current.startedAtMs}-$nowMs",
                    startedAtMs = current.startedAtMs,
                    endedAtMs = nowMs,
                    startPercent = current.startPercent,
                    endPercent = current.currentPercent
                )
            )
        }
        active = null
        store.clearActive()
    }

    private companion object {
        const val PERSIST_INTERVAL_MS = 60_000L
        const val MAX_OBSERVATION_GAP_MS = 5 * 60_000L
        const val MIN_COMPLETED_DURATION_MS = 60_000L
    }
}
