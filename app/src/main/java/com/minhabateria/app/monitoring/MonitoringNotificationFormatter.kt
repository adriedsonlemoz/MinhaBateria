package com.minhabateria.app.monitoring

import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.calculation.BatteryRateEstimator
import com.minhabateria.app.charging.ChargeTimeEstimator
import com.minhabateria.app.charging.ChargeTimeFormatter
import com.minhabateria.app.discharge.ActiveDischarge
import java.util.Locale

object MonitoringNotificationFormatter {
    data class Content(
        val summary: String,
        val expanded: String
    )

    fun format(
        info: BatteryInfo,
        session: ChargingSession.Snapshot?,
        discharge: ActiveDischarge?,
        instantCurrentMa: Double?
    ): Content {
        val summary = when {
            isFull(info, session) -> join("Carga completa", percent(info), temperature(info))
            info.isCharging == true -> chargingSummary(info, session)
            info.isPlugged == false -> dischargeSummary(info, discharge)
            info.isPlugged == true -> join("Conectado • carga pausada", temperature(info))
            else -> join("Monitoramento ativo", percent(info), temperature(info))
        }
        val details = buildDetails(info, session, discharge, instantCurrentMa)
        return Content(summary, if (details.isEmpty()) summary else "$summary\n$details")
    }

    private fun chargingSummary(info: BatteryInfo, session: ChargingSession.Snapshot?): String {
        val estimate = session?.let { ChargeTimeEstimator.estimate(info, it) }
        val estimateText = estimate?.let {
            if (it.remainingMs == 0L) "Carga completa"
            else "100% em ~${ChargeTimeFormatter.compact(it.remainingMs)}"
        } ?: "Calculando tempo até 100%…"
        return join(estimateText, chargeRate(session), temperature(info))
    }

    private fun dischargeSummary(info: BatteryInfo, discharge: ActiveDischarge?): String {
        val rate = BatteryRateEstimator.dischargePercentPerHour(discharge)
        val remaining = BatteryRateEstimator.dischargeRemainingMs(discharge)?.let(ChargeTimeFormatter::compact)
        val temp = info.temperatureC
        return when {
            temp != null && temp >= HIGH_TEMPERATURE_C ->
                join("⚠ Temperatura alta", formatTemperature(temp), remaining?.let { "Restam ~$it" })
            rate != null && rate >= HIGH_DISCHARGE_RATE_PERCENT_PER_HOUR ->
                join("⚠ Consumo elevado", formatRate(rate), remaining?.let { "Restam ~$it" })
            remaining != null -> join("Restam ~$remaining", rate?.let(::formatRate), temperature(info))
            else -> join("Calculando autonomia…", percent(info), temperature(info))
        }
    }

    private fun buildDetails(
        info: BatteryInfo,
        session: ChargingSession.Snapshot?,
        discharge: ActiveDischarge?,
        instantCurrentMa: Double?
    ): String {
        val lines = mutableListOf<String>()
        when {
            info.isCharging == true || isFull(info, session) -> {
                session?.elapsedMs?.let { elapsed ->
                    val parts = mutableListOf("Sessão: ${compactElapsed(elapsed)}")
                    session.gainPercent?.takeIf { it != 0 }?.let { parts += signedPercent(it) }
                    BatteryRateEstimator.chargingPercentPerHour(session)?.let { parts += "média ${signedRate(it)}" }
                    lines += parts.joinToString(" • ")
                }
            }
            info.isPlugged == false -> {
                discharge?.let {
                    val parts = mutableListOf("Sessão: ${compactElapsed(it.durationMs)}")
                    if (it.dropPercent > 0) parts += "-${it.dropPercent}%"
                    BatteryRateEstimator.dischargePercentPerHour(it)?.let { rate -> parts += "média ${formatRate(rate)}" }
                    lines += parts.joinToString(" • ")
                }
            }
        }
        instantCurrentMa?.let { lines += "Corrente agora: ${signedCurrent(it)}" }
        return lines.joinToString("\n")
    }

    private fun chargeRate(session: ChargingSession.Snapshot?): String? =
        BatteryRateEstimator.chargingPercentPerHour(session)?.let(::signedRate)

    private fun isFull(info: BatteryInfo, session: ChargingSession.Snapshot?): Boolean =
        info.percent == 100 || session?.reachedFull == true

    private fun percent(info: BatteryInfo): String? = info.percent?.let { "$it%" }

    private fun temperature(info: BatteryInfo): String? = info.temperatureC?.let(::formatTemperature)

    private fun formatTemperature(value: Double): String =
        String.format(Locale("pt", "BR"), "%.1f °C", value)

    private fun formatRate(value: Double): String =
        String.format(Locale("pt", "BR"), "%.1f%%/h", value)

    private fun signedRate(value: Double): String =
        String.format(Locale("pt", "BR"), "+%.1f%%/h", value)

    private fun signedCurrent(value: Double): String =
        if (value == 0.0) "0 mA" else String.format(Locale("pt", "BR"), "%+.0f mA", value)

    private fun signedPercent(value: Int): String = if (value > 0) "+$value%" else "$value%"

    private fun compactElapsed(milliseconds: Long): String {
        val totalMinutes = (milliseconds / 60_000L).coerceAtLeast(0L)
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return when {
            hours <= 0L -> "$minutes min"
            minutes == 0L -> "$hours h"
            else -> "$hours h ${minutes} min"
        }
    }

    private fun join(vararg parts: String?): String =
        parts.filterNot { it.isNullOrBlank() }.joinToString(" • ")

    private const val HIGH_DISCHARGE_RATE_PERCENT_PER_HOUR = 15.0
    private const val HIGH_TEMPERATURE_C = 43.0
}
