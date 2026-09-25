package com.minhabateria.app.charts

import java.util.ArrayDeque

class ChartSampleBuffer(
    private val sampleIntervalMs: Long = 10_000L,
    private val maxAgeMs: Long = 60 * 60_000L
) {
    private val samples = ArrayDeque<ChartSample>()

    fun record(sample: ChartSample): Boolean {
        val last = samples.lastOrNull()
        if (last != null && sample.timestampMs >= last.timestampMs) {
            if (sample.timestampMs - last.timestampMs < sampleIntervalMs) return false
        }
        if (last != null && sample.timestampMs < last.timestampMs) samples.clear()
        samples.addLast(sample)
        prune(sample.timestampMs)
        return true
    }

    fun replace(items: List<ChartSample>, nowMs: Long) {
        samples.clear()
        items.sortedBy { it.timestampMs }.forEach { sample ->
            if (sample.timestampMs >= nowMs - maxAgeMs) samples.addLast(sample)
        }
    }

    fun snapshot(nowMs: Long, rangeMs: Long): List<ChartSample> {
        prune(nowMs)
        val cutoff = nowMs - rangeMs
        return samples.filter { it.timestampMs >= cutoff }
    }

    fun all(nowMs: Long): List<ChartSample> {
        prune(nowMs)
        return samples.toList()
    }

    private fun prune(nowMs: Long) {
        val cutoff = nowMs - maxAgeMs
        while (samples.isNotEmpty() && samples.first().timestampMs < cutoff) {
            samples.removeFirst()
        }
    }
}
