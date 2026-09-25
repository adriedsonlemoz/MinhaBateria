package com.minhabateria.app.session

import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import java.util.Locale

object SessionInsightBuilder {
    data class Insight(
        val sessionState: String,
        val headline: String,
        val powerContext: String,
        val stability: String,
        val detail: String
    )

    fun build(
        info: BatteryInfo?,
        session: ChargingSession.Snapshot?,
        nominalPowerW: Double?
    ): Insight {
        if (session?.elapsedMs == null) {
            return Insight(
                sessionState = "Aguardando conexão",
                headline = "Conecte uma fonte para iniciar uma nova sessão.",
                powerContext = "Sem comparação disponível",
                stability = "Estabilidade: aguardando amostras",
                detail = "A sessão começa ao conectar e termina ao desconectar a fonte."
            )
        }

        val state = if (session.reachedFull) "Carga completa" else "Sessão em andamento"
        val headline = summaryLine(info, session)
        val powerContext = powerContext(info?.powerW, nominalPowerW)
        val stability = stabilityLabel(session.powerVariationRatio)
        val detail = if (session.reachedFull) {
            "Os valores foram congelados ao atingir 100%; a sessão será encerrada ao desconectar."
        } else {
            "Classificações são interpretações das leituras observadas no aparelho, não medições da saída da fonte."
        }
        return Insight(state, headline, powerContext, stability, detail)
    }

    private fun summaryLine(info: BatteryInfo?, session: ChargingSession.Snapshot): String {
        val range = SessionFormatter.batteryRange(session.startPercent, session.currentPercent)
        val duration = SessionFormatter.duration(session.elapsedMs)
        return when {
            range != "Indisponível" -> "Bateria $range • $duration de sessão"
            info?.isCharging == true -> "Carga em andamento • $duration de sessão"
            else -> "Sessão em andamento • $duration"
        }
    }

    private fun powerContext(currentPowerW: Double?, nominalPowerW: Double?): String {
        if (currentPowerW == null || nominalPowerW == null || nominalPowerW <= 0.0) {
            return "Potência: sem referência suficiente"
        }
        val ratio = (currentPowerW / nominalPowerW).coerceAtLeast(0.0)
        val percent = (ratio * 100.0).toInt()
        val label = when {
            ratio >= 0.70 -> "alta frente à referência"
            ratio >= 0.35 -> "moderada frente à referência"
            else -> "baixa frente à referência"
        }
        return "Potência observada $label • $percent% da referência configurada"
    }

    private fun stabilityLabel(variationRatio: Double?): String {
        if (variationRatio == null) return "Estabilidade: aguardando amostras"
        val label = when {
            variationRatio <= 0.08 -> "estável"
            variationRatio <= 0.20 -> "variação moderada"
            else -> "oscilando"
        }
        val variation = String.format(Locale.getDefault(), "%.0f%%", variationRatio * 100.0)
        return "Estabilidade: $label • oscilação relativa aproximada $variation"
    }
}
