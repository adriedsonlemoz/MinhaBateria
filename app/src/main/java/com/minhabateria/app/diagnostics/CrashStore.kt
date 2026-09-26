package com.minhabateria.app.diagnostics

import android.content.Context
import java.io.File

object CrashStore {
    private const val DIRECTORY = "crash_reports"
    private const val MAX_REPORTS = 10
    private const val PREFS_NAME = "crash_report_state"
    private const val KEY_LAST_NOTIFIED = "last_notified_report"

    data class StoredReport(
        val file: File,
        val text: String,
        val timestampMs: Long
    )

    fun save(context: Context, report: String): File? = runCatching {
        val directory = File(context.filesDir, DIRECTORY).apply { mkdirs() }
        val now = System.currentTimeMillis()
        val file = File(directory, "crash-$now.txt")
        file.writeText(report.take(64_000), Charsets.UTF_8)
        prune(directory)
        file
    }.getOrNull()

    fun recent(context: Context, limit: Int = MAX_REPORTS): List<StoredReport> {
        val directory = File(context.filesDir, DIRECTORY)
        if (!directory.isDirectory) return emptyList()
        return directory.listFiles { file -> file.isFile && file.extension == "txt" }
            ?.sortedByDescending { it.lastModified() }
            ?.take(limit)
            ?.mapNotNull { file ->
                runCatching {
                    StoredReport(file, file.readText(Charsets.UTF_8), file.lastModified())
                }.getOrNull()
            }
            .orEmpty()
    }

    fun latest(context: Context): StoredReport? = recent(context, 1).firstOrNull()

    fun count(context: Context): Int {
        val directory = File(context.filesDir, DIRECTORY)
        return directory.listFiles { file -> file.isFile && file.extension == "txt" }?.size ?: 0
    }

    fun consumeNewCrashNotice(context: Context): Boolean {
        val latest = latest(context) ?: return false
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notified = preferences.getLong(KEY_LAST_NOTIFIED, 0L)
        if (latest.timestampMs <= notified) return false
        preferences.edit().putLong(KEY_LAST_NOTIFIED, latest.timestampMs).apply()
        return true
    }

    fun clear(context: Context) {
        val directory = File(context.filesDir, DIRECTORY)
        directory.listFiles()?.forEach { runCatching { it.delete() } }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LAST_NOTIFIED)
            .apply()
    }

    private fun prune(directory: File) {
        directory.listFiles { file -> file.isFile && file.extension == "txt" }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(MAX_REPORTS)
            ?.forEach { runCatching { it.delete() } }
    }
}
