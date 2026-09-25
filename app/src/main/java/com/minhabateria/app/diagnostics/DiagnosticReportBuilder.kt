package com.minhabateria.app.diagnostics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSource
import com.minhabateria.app.charts.ChartRange
import com.minhabateria.app.charging.ChargeTimeFormatter
import com.minhabateria.app.charts.ChartSampleRepository
import com.minhabateria.app.history.HistoryStore
import com.minhabateria.app.monitoring.ContinuousSessionStore
import com.minhabateria.app.monitoring.MonitorPreferences
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.session.SessionFormatter
import com.minhabateria.app.settings.AppVersionInfo
import com.minhabateria.app.source.SourceProfileFormatter
import com.minhabateria.app.source.SourceProfileStore
import java.text.DateFormat
import java.util.Date
import java.util.Locale

object DiagnosticReportBuilder {
    fun build(context: Context, state: MonitoringState): String {
        val appContext = context.applicationContext
        val preferences = MonitorPreferences(appContext)
        val profile = SourceProfileStore(appContext).getProfile()
        val historyCount = HistoryStore(appContext).entries().size
        val chartCount = ChartSampleRepository.samples(appContext, ChartRange.MINUTES_60).size
        val savedSession = ContinuousSessionStore(appContext).load()

        return buildString {
            appendLine("MINHA BATERIA — DIAGNÓSTICO")
            appendLine("Gerado: ${DateFormat.getDateTimeInstance().format(Date())}")
            appendLine()
            appendLine("APLICATIVO")
            appendLine("Versão: ${AppVersionInfo.fullVersion(appContext)}")
            appendLine("Dispositivo: ${deviceName()}")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine()
            appendLine("MONITORAMENTO")
            appendLine("Solicitado: ${yesNo(preferences.isMonitoringRequested())}")
            appendLine("Serviço ativo: ${yesNo(state.running)}")
            appendLine("Retomar após reiniciar: ${yesNo(preferences.shouldResumeAfterBoot())}")
            appendLine("Notificações: ${notificationPermission(context)}")
            appendLine("Sessão contínua persistida: ${yesNo(savedSession?.active == true)}")
            appendLine()
            appendLine("FONTE CONFIGURADA")
            appendLine(profile?.let(SourceProfileFormatter::settingsSummary) ?: "Nenhum perfil configurado")
            appendLine()
            appendLine("LEITURA ATUAL")
            appendBattery(state.info)
            appendLine()
            appendLine("SESSÃO ATUAL")
            appendSession(state)
            appendLine()
            appendLine("ARMAZENAMENTO")
            appendLine("Sessões no Histórico: $historyCount / 100")
            appendLine("Amostras de gráficos (últimos 60 min): $chartCount")
            appendLine()
            appendLine("OBSERVAÇÕES")
            appendLine("• Corrente é a leitura observada pelo Android na bateria quando disponível.")
            appendLine("• Potência é calculada a partir de tensão × corrente.")
            appendLine("• Wh, mAh e médias da sessão são estimados por integração temporal.")
            appendLine("• Valores indisponíveis não são substituídos por zero.")
        }.trimEnd()
    }

    private fun StringBuilder.appendBattery(info: BatteryInfo?) {
        if (info == null) {
            appendLine("Sem leitura disponível.")
            return
        }
        appendLine("Bateria: ${info.percent?.let { "$it%" } ?: "Indisponível"}")
        appendLine("Fonte conectada: ${triState(info.isPlugged)}")
        appendLine("Carregando: ${triState(info.isCharging)}")
        appendLine("Conexão detectada: ${sourceName(info.source)}")
        appendLine("Tensão: ${info.voltageMv?.let { String.format(Locale.getDefault(), "%.2f V", it / 1000.0) } ?: "Indisponível"}")
        appendLine("Corrente: ${SessionFormatter.current(info.currentMa)}")
        appendLine("Potência: ${SessionFormatter.power(info.powerW)}")
        appendLine("Temperatura: ${SessionFormatter.temperature(info.temperatureC)}")
        appendLine("Tempo restante informado pelo Android: ${info.chargeTimeRemainingMs?.let(ChargeTimeFormatter::compact) ?: "Indisponível"}")
    }

    private fun StringBuilder.appendSession(state: MonitoringState) {
        val session = state.session
        if (session?.elapsedMs == null) {
            appendLine("Nenhuma sessão ativa.")
            return
        }
        appendLine("Duração: ${SessionFormatter.duration(session.elapsedMs)}")
        appendLine("Tempo carregando: ${SessionFormatter.duration(session.chargingTimeMs)}")
        appendLine("Energia estimada: ${SessionFormatter.energy(session.energyWh)}")
        appendLine("Carga estimada: ${SessionFormatter.charge(session.chargeMah)}")
        appendLine("Potência média: ${SessionFormatter.power(session.averagePowerW)}")
        appendLine("Pico de potência: ${SessionFormatter.power(session.peakPowerW)}")
        appendLine("Bateria: ${SessionFormatter.batteryRange(session.startPercent, session.currentPercent)}")
        appendLine("Interrupções: ${session.interruptions}")
        appendLine("Carga completa: ${yesNo(session.reachedFull)}")
    }

    private fun deviceName(): String = listOf(Build.MANUFACTURER, Build.MODEL)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.ROOT) }
        .joinToString(" ")
        .ifBlank { "Indisponível" }

    private fun notificationPermission(context: Context): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return "Não exigida nesta versão do Android"
        return if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            "Concedida"
        } else {
            "Não concedida"
        }
    }

    private fun triState(value: Boolean?): String = when (value) {
        true -> "Sim"
        false -> "Não"
        null -> "Indisponível"
    }

    private fun yesNo(value: Boolean): String = if (value) "Sim" else "Não"

    private fun sourceName(source: ChargingSource): String = when (source) {
        ChargingSource.AC -> "Tomada / AC"
        ChargingSource.USB -> "USB"
        ChargingSource.WIRELESS -> "Sem fio"
        ChargingSource.BATTERY -> "Bateria"
        ChargingSource.UNKNOWN -> "Indisponível"
    }
}
