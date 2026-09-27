package com.minhabateria.app.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.battery.ChargingSource
import com.minhabateria.app.calculation.BatteryRateEstimator
import com.minhabateria.app.charging.ChargeTimeEstimator
import com.minhabateria.app.charging.ChargeTimeFormatter
import com.minhabateria.app.discharge.ActiveDischarge
import com.minhabateria.app.discharge.DischargeFormatter
import com.minhabateria.app.interpretation.ChargeConditionInterpreter
import com.minhabateria.app.session.SessionFormatter
import com.minhabateria.app.source.EnergySourceProfile
import com.minhabateria.app.source.EnergySourceType
import com.minhabateria.app.source.SourceProfileNameBuilder
import com.minhabateria.app.utils.BatteryFormatter
import com.minhabateria.app.utils.TimeFormatter
import java.util.Date
import kotlin.math.roundToInt

class MainScreenRenderer(private val activity: Activity) {
    private val gauge = activity.findViewById<BatteryGaugeView>(R.id.batteryGauge)
    private val status = text(R.id.statusText)
    private val estimateText = text(R.id.mainEstimateText)
    private val sourceIcon = text(R.id.sourceTypeIcon)
    private val profileSource = text(R.id.profileSourceValue)
    private val detectedSource = text(R.id.detectedSourceValue)
    private val conditionTitle = text(R.id.mainConditionTitle)
    private val conditionMessage = text(R.id.mainConditionMessage)
    private val powerLabel = text(R.id.powerLabel)
    private val power = text(R.id.powerValue)
    private val powerHint = text(R.id.powerHint)
    private val currentLabel = text(R.id.currentLabel)
    private val current = text(R.id.currentValue)
    private val currentHint = text(R.id.currentHint)
    private val temperature = text(R.id.temperatureValue)
    private val temperatureHint = text(R.id.temperatureHint)
    private val energyLabel = text(R.id.energyLabel)
    private val energy = text(R.id.energyValue)
    private val energyHint = text(R.id.energyHint)
    private val voltage = text(R.id.voltageValue)
    private val charge = text(R.id.chargeValue)
    private val elapsed = text(R.id.elapsedValue)
    private val technicalNote = text(R.id.mainTechnicalNote)
    private val powerCard = activity.findViewById<View>(R.id.powerCard)
    private val currentCard = activity.findViewById<View>(R.id.currentCard)
    private val temperatureCard = activity.findViewById<View>(R.id.temperatureCard)
    private val energyCard = activity.findViewById<View>(R.id.energyCard)
    private var sourceProfile: EnergySourceProfile? = null

    fun render(
        info: BatteryInfo,
        session: ChargingSession.Snapshot,
        discharge: ActiveDischarge? = null
    ) {
        val estimate = ChargeTimeEstimator.estimate(info, session)
        renderGaugeAndStatus(info, session.reachedFull, estimate, discharge)
        renderSourceDetection(info)
        renderCondition(info, session)
        renderQuickMetrics(info, session, discharge)
        renderTechnicalDetails(info, session)
    }

    fun renderSourceProfile(profile: EnergySourceProfile?) {
        sourceProfile = profile
        sourceIcon.text = profile?.type?.icon() ?: "⚡"
        profileSource.text = profile?.let(::profileLabel) ?: "Fonte física não informada"
        profileSource.setTextColor(
            activity.getColor(if (profile == null) R.color.value_unavailable else R.color.text_primary)
        )
        if (profile == null) {
            detectedSource.text = "Toque para informar a fonte usada"
            detectedSource.setTextColor(activity.getColor(R.color.text_secondary))
        }
    }

    private fun renderGaugeAndStatus(
        info: BatteryInfo,
        reachedFull: Boolean,
        estimate: ChargeTimeEstimator.Estimate?,
        discharge: ActiveDischarge?
    ) {
        val full = info.percent == 100 || reachedFull
        gauge.setBattery(info.percent, info.isCharging == true, null)
        status.text = when {
            full -> "Carga completa"
            info.isCharging == true -> "Carregando"
            info.isPlugged == true -> "Conectado • carga pausada"
            info.isPlugged == false -> "Usando a bateria"
            else -> "Status da bateria"
        }
        val active = info.isCharging == true || full
        status.setTextColor(activity.getColor(if (active) R.color.accent_green else R.color.text_primary))
        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (active) R.drawable.ic_status_charging else R.drawable.ic_status_idle,
            0,
            0,
            0
        )
        status.compoundDrawablePadding = 7
        status.background = statusBackground(active)
        estimateText.text = estimateLabel(info, full, estimate, discharge)
    }

    private fun estimateLabel(
        info: BatteryInfo,
        full: Boolean,
        estimate: ChargeTimeEstimator.Estimate?,
        discharge: ActiveDischarge?
    ): String = when {
        full -> "A bateria chegou a 100%."
        info.isCharging == true && estimate != null -> {
            val target = Date(System.currentTimeMillis() + estimate.remainingMs)
            val time = DateFormat.getTimeFormat(activity).format(target)
            val source = when (estimate.source) {
                ChargeTimeEstimator.Source.SYSTEM -> "estimativa do Android"
                ChargeTimeEstimator.Source.SESSION -> "estimativa pelo ritmo observado"
            }
            "Previsão de 100% por volta das $time • $source"
        }
        info.isCharging == true -> "Estimativa em preparação • precisamos de mais dados desta carga"
        info.isPlugged == false -> BatteryRateEstimator.dischargeRemainingMs(discharge)?.let {
            "Autonomia aproximada: ${ChargeTimeFormatter.compact(it)}"
        } ?: "Autonomia em preparação • precisamos observar o ritmo de descarga"
        info.isPlugged == true -> "A fonte está conectada, mas a carga está pausada."
        else -> "Aguardando informações da bateria."
    }

    private fun renderSourceDetection(info: BatteryInfo) {
        val detection = detectedConnection(info.source)
        detectedSource.text = when {
            sourceProfile != null && detection != null -> "Fonte informada por você • Android: alimentação via $detection"
            sourceProfile != null -> "Fonte informada por você • Android não informou o tipo da conexão"
            detection != null -> "Sem perfil físico • Android: alimentação via $detection"
            else -> "Toque para informar a fonte usada"
        }
        detectedSource.setTextColor(activity.getColor(R.color.text_secondary))
    }

    private fun renderCondition(info: BatteryInfo, session: ChargingSession.Snapshot) {
        val result = ChargeConditionInterpreter.interpret(info, session, sourceProfile)
        conditionTitle.text = "${levelIcon(result.level)}  ${result.title}"
        conditionTitle.setTextColor(activity.getColor(levelColor(result.level)))
        conditionMessage.text = result.explanation
    }

    private fun renderQuickMetrics(
        info: BatteryInfo,
        session: ChargingSession.Snapshot,
        discharge: ActiveDischarge?
    ) {
        if (info.isPlugged == false) {
            val rate = BatteryRateEstimator.dischargePercentPerHour(discharge)
            powerLabel.text = "Ritmo de descarga"
            power.text = rate?.let(DischargeFormatter::rate) ?: "Calculando…"
            power.setTextColor(activity.getColor(if (rate == null) R.color.value_unavailable else R.color.accent_orange))
            powerHint.text = if (rate == null) "Precisamos observar mais a descarga" else "Queda aproximada por hora"
            powerCard.alpha = if (rate == null) 0.72f else 1f
        } else {
            powerLabel.text = "Potência agora"
            power.text = BatteryFormatter.power(info.powerW)
            power.setTextColor(activity.getColor(if (info.powerW == null) R.color.value_unavailable else R.color.accent_blue))
            powerHint.text = powerContext(info.powerW, sourceProfile?.nominalPowerW)
            powerCard.alpha = if (info.powerW == null) 0.72f else 1f
        }

        currentLabel.text = if (info.isPlugged == false) "Corrente da bateria" else "Corrente agora"
        val currentMa = info.currentMa
        current.text = BatteryFormatter.signedCurrent(currentMa)
        current.setTextColor(
            activity.getColor(
                when {
                    currentMa == null -> R.color.value_unavailable
                    currentMa < -50.0 && info.isPlugged == true -> R.color.accent_red
                    currentMa < -50.0 -> R.color.accent_blue
                    currentMa > 50.0 -> R.color.accent_green
                    else -> R.color.text_primary
                }
            )
        )
        currentHint.text = when {
            currentMa == null -> "Leitura indisponível"
            currentMa < -50.0 && info.isPlugged == true -> "Consumo líquido mesmo conectado"
            currentMa < -50.0 -> "Energia saindo da bateria"
            currentMa > 50.0 -> "Energia entrando no aparelho"
            else -> "Fluxo próximo de zero"
        }
        currentCard.alpha = if (currentMa == null) 0.72f else 1f

        temperature.text = BatteryFormatter.temperature(info.temperatureC)
        temperature.setTextColor(activity.getColor(temperatureColor(info.temperatureC)))
        temperatureHint.text = ChargeConditionInterpreter.temperatureLabel(info.temperatureC)
        temperatureCard.alpha = if (info.temperatureC == null) 0.72f else 1f

        if (info.isPlugged == false) {
            energyLabel.text = "Queda da bateria"
            energy.text = discharge?.let { "${it.dropPercent}%" } ?: "—"
            energy.setTextColor(activity.getColor(if (discharge == null) R.color.value_unavailable else R.color.text_primary))
            energyHint.text = discharge?.let { "Em ${TimeFormatter.elapsed(it.durationMs)}" } ?: "Aguardando período de descarga"
            energyCard.alpha = if (discharge == null) 0.72f else 1f
        } else {
            energyLabel.text = "Energia recebida"
            energy.text = SessionFormatter.energy(session.energyWh)
            energy.setTextColor(activity.getColor(if (session.energyWh == null) R.color.value_unavailable else R.color.text_primary))
            energyHint.text = session.elapsedMs?.let { "Nesta sessão • ${TimeFormatter.elapsed(it)}" } ?: "Aguardando sessão"
            energyCard.alpha = if (session.energyWh == null) 0.72f else 1f
        }
    }

    private fun renderTechnicalDetails(info: BatteryInfo, session: ChargingSession.Snapshot) {
        voltage.text = BatteryFormatter.voltage(info.voltageMv)
        charge.text = SessionFormatter.charge(session.chargeMah)
        elapsed.text = session.elapsedMs?.let(TimeFormatter::elapsed) ?: "—"
        technicalNote.text = buildString {
            append("Medido pelo Android: tensão, corrente e temperatura. Energia e mAh são cálculos a partir das leituras válidas.")
            detectedConnection(info.source)?.let { append(" Conexão informada pelo sistema: $it.") }
        }
    }

    private fun powerContext(powerW: Double?, nominalW: Double?): String {
        if (powerW == null) return "Leitura indisponível"
        if (nominalW == null || nominalW <= 0.0) return "Observada no aparelho"
        val percent = ((powerW / nominalW) * 100.0).roundToInt().coerceAtLeast(0)
        return "≈$percent% da referência configurada"
    }

    private fun profileLabel(profile: EnergySourceProfile): String {
        val identity = listOfNotNull(profile.brand?.clean(), profile.model?.clean()).joinToString(" ").ifBlank {
            profile.type.label
        }
        val power = profile.nominalPowerW?.takeIf { it > 0.0 }?.let(SourceProfileNameBuilder::formatPower)
        return if (power == null || identity.contains(power, ignoreCase = true)) identity else "$identity • $power"
    }

    private fun detectedConnection(source: ChargingSource): String? = when (source) {
        ChargingSource.AC -> "conexão AC"
        ChargingSource.USB -> "USB"
        ChargingSource.WIRELESS -> "carregamento sem fio"
        ChargingSource.BATTERY -> "uso da bateria"
        ChargingSource.UNKNOWN -> null
    }

    private fun EnergySourceType.icon(): String = when (this) {
        EnergySourceType.SOLAR_PANEL -> "☀"
        EnergySourceType.CHARGER -> "🔌"
        EnergySourceType.POWER_BANK -> "🔋"
        EnergySourceType.OTHER -> "⚡"
    }

    private fun temperatureColor(celsius: Double?): Int = when {
        celsius == null -> R.color.value_unavailable
        celsius >= 45.0 -> R.color.accent_red
        celsius >= 42.0 -> R.color.accent_orange
        celsius >= 38.0 -> R.color.accent_orange
        else -> R.color.accent_green
    }

    private fun levelIcon(level: ChargeConditionInterpreter.Level): String = when (level) {
        ChargeConditionInterpreter.Level.GOOD -> "●"
        ChargeConditionInterpreter.Level.INFO -> "●"
        ChargeConditionInterpreter.Level.ATTENTION -> "●"
        ChargeConditionInterpreter.Level.ELEVATED -> "●"
        ChargeConditionInterpreter.Level.CRITICAL -> "●"
    }

    private fun levelColor(level: ChargeConditionInterpreter.Level): Int = when (level) {
        ChargeConditionInterpreter.Level.GOOD -> R.color.accent_green
        ChargeConditionInterpreter.Level.INFO -> R.color.accent_blue
        ChargeConditionInterpreter.Level.ATTENTION -> R.color.accent_orange
        ChargeConditionInterpreter.Level.ELEVATED -> R.color.accent_orange
        ChargeConditionInterpreter.Level.CRITICAL -> R.color.accent_red
    }

    private fun statusBackground(active: Boolean): GradientDrawable {
        val colors = if (active) {
            intArrayOf(Color.rgb(16, 61, 42), Color.rgb(9, 39, 28))
        } else {
            intArrayOf(Color.rgb(31, 47, 61), Color.rgb(22, 35, 47))
        }
        return GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f
            setStroke(1, activity.getColor(if (active) R.color.accent_green_dark else R.color.status_idle_border))
        }
    }

    private fun String.clean(): String? = trim().takeIf { it.isNotBlank() && !it.equals("Outra", true) }
    private fun text(id: Int): TextView = activity.findViewById(id)
}
