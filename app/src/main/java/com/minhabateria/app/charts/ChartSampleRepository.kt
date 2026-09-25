package com.minhabateria.app.charts

import android.content.Context
import com.minhabateria.app.battery.BatteryInfo

object ChartSampleRepository {
    private const val PREFS = "chart_samples"
    private const val KEY_DATA = "data"
    private const val PERSIST_INTERVAL_MS = 60_000L
    private val buffer = ChartSampleBuffer()
    private var initialized = false
    private var lastPersistMs = 0L

    @Synchronized
    fun record(context: Context, info: BatteryInfo, nowMs: Long = System.currentTimeMillis()) {
        ensureLoaded(context, nowMs)
        val sample = ChartSample(
            timestampMs = nowMs,
            powerW = info.powerW,
            currentMa = info.currentMa,
            temperatureC = info.temperatureC,
            batteryPercent = info.percent
        )
        if (!buffer.record(sample)) return
        if (nowMs - lastPersistMs >= PERSIST_INTERVAL_MS) persist(context, nowMs)
    }

    @Synchronized
    fun samples(
        context: Context,
        range: ChartRange,
        nowMs: Long = System.currentTimeMillis()
    ): List<ChartSample> {
        ensureLoaded(context, nowMs)
        return buffer.snapshot(nowMs, range.durationMs)
    }

    @Synchronized
    fun flush(context: Context, nowMs: Long = System.currentTimeMillis()) {
        ensureLoaded(context, nowMs)
        persist(context, nowMs)
    }

    private fun ensureLoaded(context: Context, nowMs: Long) {
        if (initialized) return
        val encoded = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_DATA, null)
        val loaded = encoded.orEmpty().lineSequence().mapNotNull(::decode).toList()
        buffer.replace(loaded, nowMs)
        initialized = true
        lastPersistMs = nowMs
    }

    private fun persist(context: Context, nowMs: Long) {
        val encoded = buffer.all(nowMs).joinToString("\n", transform = ::encode)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DATA, encoded)
            .apply()
        lastPersistMs = nowMs
    }

    private fun encode(sample: ChartSample): String = listOf(
        sample.timestampMs.toString(),
        sample.powerW?.toString().orEmpty(),
        sample.currentMa?.toString().orEmpty(),
        sample.temperatureC?.toString().orEmpty(),
        sample.batteryPercent?.toString().orEmpty()
    ).joinToString("|")

    private fun decode(line: String): ChartSample? {
        val parts = line.split('|')
        if (parts.size != 5) return null
        val timestamp = parts[0].toLongOrNull() ?: return null
        return ChartSample(
            timestampMs = timestamp,
            powerW = parts[1].toDoubleOrNull(),
            currentMa = parts[2].toDoubleOrNull(),
            temperatureC = parts[3].toDoubleOrNull(),
            batteryPercent = parts[4].toIntOrNull()
        )
    }
}
