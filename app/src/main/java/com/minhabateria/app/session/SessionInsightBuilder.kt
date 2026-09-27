package com.minhabateria.app.session

import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.interpretation.ChargeConditionInterpreter
import com.minhabateria.app.source.EnergySourceProfile
import java.util.Locale

object SessionInsightBuilder {
    data class Insight(
        val sessionState: String,
        val headline: String,
        val rhythm: String,
        val diagnosis: String,
        val detail: String,
        val level: ChargeConditionInterpreter.Level
    )

    fun build(
        info: BatteryInfo?,
        session: ChargingSession.Snapshot?,
        profile: EnergySourceProfile?
    ): Insight {
        if (session?.elapsedMs == null) {
            return Insight(
                sessionState = "Aguardando sessão",
                headline = "Conecte uma fonte para começar a medir.",
                rhythm = "Ritmo: aguardando evolução da bateria",
                diagnosis = "Aguardando dados",
                detail = "A sessão começa quando uma fonte é conectada e usa somente leituras observadas no aparelho.",
                level = ChargeConditionInterpreter.Level.INFO
            )
        }

        val state = when {
            session.reachedFull -> "Sessão concluída em 100%"
            info?.isCharging == true -> "Sessão em andamento"
            info?.isPlugged == true -> "Sessão pausada"
            else -> "Última sessão"
        }
        val condition = ChargeConditionInterpreter.interpretSession(session, profile)
        return Insight(
            sessionState = state,
            headline = progressLine(session),
            rhythm = rhythmLine(session),
            diagnosis = condition.title,
            detail = condition.explanation,
            level = condition.level
        )
    }

    private fun progressLine(session: ChargingSession.Snapshot): String {
        val start = session.startPercent
        val current = session.currentPercent
        val elapsed = SessionFormatter.duration(session.elapsedMs)
        return when {
            start != null && current != null -> {
                val gain = current - start
                val signed = if (gain > 0) "+$gain%" else "$gain%"
                "$start% → $current%   •   $signed em $elapsed"
            }
            else -> "${SessionFormatter.duration(session.elapsedMs)} de observação"
        }
    }

    private fun rhythmLine(session: ChargingSession.Snapshot): String {
        val gain = session.gainPercent ?: return "Ritmo: aguardando mudança de porcentagem"
        val timeMs = session.chargingTimeMs ?: session.elapsedMs ?: return "Ritmo: aguardando tempo suficiente"
        if (timeMs < MIN_RATE_TIME_MS || gain == 0) return "Ritmo: coletando mais dados"
        val perHour = gain / (timeMs / 3_600_000.0)
        if (!perHour.isFinite()) return "Ritmo: aguardando dados válidos"
        val formatted = String.format(Locale.getDefault(), "%+.1f%%/h", perHour)
        return "Ritmo aproximado: $formatted"
    }

    private const val MIN_RATE_TIME_MS = 5 * 60 * 1000L
}
