package com.minhabateria.app.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings

class BatteryUsageRepository(private val context: Context) {
    data class AppUsage(
        val packageName: String,
        val label: String,
        val foregroundMs: Long,
        val sharePercent: Int,
        val icon: Drawable?
    )

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun topApps(windowMs: Long = DEFAULT_WINDOW_MS, limit: Int = 5): List<AppUsage> {
        if (!hasUsageAccess()) return emptyList()
        val end = System.currentTimeMillis()
        val begin = end - windowMs
        val durations = foregroundDurations(begin, end)
        val packageManager = context.packageManager
        val candidates = durations.asSequence()
            .filter { it.value >= MIN_FOREGROUND_MS }
            .mapNotNull { (packageName, foregroundMs) ->
                val appInfo = runCatching {
                    packageManager.getApplicationInfo(packageName, 0)
                }.getOrNull() ?: return@mapNotNull null
                val launchable = packageManager.getLaunchIntentForPackage(packageName) != null
                if (!launchable && packageName != context.packageName) return@mapNotNull null
                Triple(packageName, foregroundMs, appInfo)
            }
            .sortedByDescending { it.second }
            .toList()
        val total = candidates.sumOf { it.second }.coerceAtLeast(1L)
        return candidates.take(limit).map { (packageName, foregroundMs, appInfo) ->
            AppUsage(
                packageName = packageName,
                label = packageManager.getApplicationLabel(appInfo).toString(),
                foregroundMs = foregroundMs,
                sharePercent = ((foregroundMs * 100.0) / total).toInt().coerceIn(1, 100),
                icon = runCatching { packageManager.getApplicationIcon(appInfo) }.getOrNull()
            )
        }
    }

    fun openUsageAccessSettings() {
        val targeted = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val generic = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(targeted) }
            .recoverCatching { context.startActivity(generic) }
    }

    private fun foregroundDurations(begin: Long, end: Long): Map<String, Long> {
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val durations = mutableMapOf<String, Long>()
        val activeSince = mutableMapOf<String, Long>()
        val events = runCatching { manager.queryEvents(begin, end) }.getOrNull() ?: return emptyMap()
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                UsageEvents.Event.ACTIVITY_RESUMED -> activeSince.putIfAbsent(packageName, event.timeStamp)

                UsageEvents.Event.MOVE_TO_BACKGROUND,
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val startedAt = activeSince.remove(packageName) ?: continue
                    val elapsed = (event.timeStamp - startedAt).coerceAtLeast(0L)
                    durations[packageName] = (durations[packageName] ?: 0L) + elapsed
                }
            }
        }
        activeSince.forEach { (packageName, startedAt) ->
            durations[packageName] = (durations[packageName] ?: 0L) + (end - startedAt).coerceAtLeast(0L)
        }
        return durations
    }

    companion object {
        const val DEFAULT_WINDOW_MS = 6 * 60 * 60 * 1000L
        private const val MIN_FOREGROUND_MS = 30_000L
    }
}
