package com.minhabateria.app.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.settings.AppVersionInfo
import java.io.PrintWriter
import java.io.StringWriter
import java.text.DateFormat
import java.util.Date
import java.util.Locale

object CrashReportBuilder {
    fun build(context: Context, thread: Thread, throwable: Throwable): String {
        val state = runCatching { MonitoringStateStore.current() }.getOrNull()
        val info = state?.info
        val runtime = Runtime.getRuntime()
        val memoryInfo = ActivityManager.MemoryInfo()
        runCatching {
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memoryInfo)
        }
        return buildString {
            appendLine("MINHA BATERIA — FALHA CAPTURADA")
            appendLine("Data: ${DateFormat.getDateTimeInstance().format(Date())}")
            appendLine("Versão: ${runCatching { AppVersionInfo.fullVersion(context) }.getOrDefault("Indisponível")}")
            appendLine("Tela: ${CrashRuntime.currentScreen}")
            appendLine("Thread: ${thread.name}")
            appendLine("Exceção: ${throwable.javaClass.name}")
            appendLine("Mensagem: ${throwable.message ?: "Sem mensagem"}")
            appendLine()
            appendLine("DISPOSITIVO")
            appendLine("Modelo: ${deviceName()}")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Tempo desde abertura: ${formatDuration(SystemClock.elapsedRealtime() - CrashRuntime.startedAtElapsedMs)}")
            appendLine()
            appendLine("MEMÓRIA E ARMAZENAMENTO")
            appendLine("Heap usado: ${mb(runtime.totalMemory() - runtime.freeMemory())}")
            appendLine("Heap máximo: ${mb(runtime.maxMemory())}")
            appendLine("RAM disponível: ${mb(memoryInfo.availMem)}")
            appendLine("RAM baixa: ${if (memoryInfo.lowMemory) "Sim" else "Não"}")
            appendLine("Armazenamento livre do app: ${mb(context.filesDir.usableSpace)}")
            appendLine()
            appendLine("ESTADO DA BATERIA")
            appendLine("Bateria: ${info?.percent?.let { "$it%" } ?: "Indisponível"}")
            appendLine("Fonte conectada: ${triState(info?.isPlugged)}")
            appendLine("Carregando: ${triState(info?.isCharging)}")
            appendLine("Tensão: ${info?.voltageMv?.let { String.format(Locale.getDefault(), "%.2f V", it / 1000.0) } ?: "Indisponível"}")
            appendLine("Corrente: ${info?.currentMa?.let { String.format(Locale.getDefault(), "%.0f mA", it) } ?: "Indisponível"}")
            appendLine("Temperatura: ${info?.temperatureC?.let { String.format(Locale.getDefault(), "%.1f °C", it) } ?: "Indisponível"}")
            appendLine()
            appendLine("STACK TRACE")
            append(stackTrace(throwable))
        }.trimEnd()
    }

    private fun stackTrace(throwable: Throwable): String {
        val writer = StringWriter()
        throwable.printStackTrace(PrintWriter(writer))
        return writer.toString().take(40_000)
    }

    private fun deviceName(): String = listOf(Build.MANUFACTURER, Build.MODEL)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.ROOT) }
        .joinToString(" ")
        .ifBlank { "Indisponível" }

    private fun mb(bytes: Long): String = String.format(Locale.getDefault(), "%.1f MB", bytes / 1024.0 / 1024.0)

    private fun formatDuration(ms: Long): String {
        val seconds = (ms.coerceAtLeast(0L) / 1000L)
        val minutes = seconds / 60L
        return if (minutes > 0) "$minutes min ${seconds % 60L} s" else "$seconds s"
    }

    private fun triState(value: Boolean?): String = when (value) {
        true -> "Sim"
        false -> "Não"
        null -> "Indisponível"
    }
}
