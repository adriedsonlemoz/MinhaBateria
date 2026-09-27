package com.minhabateria.app.charts

import com.minhabateria.app.interpretation.ChargeConditionInterpreter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

object ChartInsightBuilder {
    data class Insight(
        val level: ChargeConditionInterpreter.Level,
        val state: String,
        val context: String,
        val explanation: String
    )

    fun build(metric: ChartMetric, samples: List<ChartSample>): Insight {
        val values = samples.mapNotNull { metric.value(it)?.takeIf(Double::isFinite) }
        if (values.isEmpty()) {
            return Insight(
                ChargeConditionInterpreter.Level.INFO,
                "Aguardando dados",
                "Sem amostras válidas",
                "O gráfico será interpretado assim que houver leituras válidas neste período."
            )
        }
        if (values.size < MIN_INTERPRETATION_SAMPLES) {
            return Insight(
                ChargeConditionInterpreter.Level.INFO,
                "Coletando dados",
                "${values.size} de $MIN_INTERPRETATION_SAMPLES amostras mínimas",
                "O valor atual já pode ser exibido, mas ainda faltam pontos para interpretar tendência ou estabilidade."
            )
        }
        return when (metric) {
            ChartMetric.BATTERY -> battery(values)
            ChartMetric.CURRENT -> current(values)
            ChartMetric.POWER -> power(values)
            ChartMetric.TEMPERATURE -> temperature(values)
        }
    }

    private fun battery(values: List<Double>): Insight {
        val delta = values.last() - values.first()
        return when {
            abs(delta) < 0.5 -> Insight(
                ChargeConditionInterpreter.Level.GOOD,
                "Bateria estável",
                "Variação: 0%",
                "Não houve alteração perceptível da porcentagem no período analisado."
            )
            delta > 0.0 -> Insight(
                ChargeConditionInterpreter.Level.GOOD,
                "Bateria subindo",
                "Variação: ${signedPercent(delta)}",
                "A porcentagem da bateria aumentou durante o período analisado."
            )
            else -> Insight(
                ChargeConditionInterpreter.Level.ATTENTION,
                "Bateria diminuindo",
                "Variação: ${signedPercent(delta)}",
                "A porcentagem caiu durante o período analisado. Se o aparelho estava conectado, o consumo pode ter superado a energia recebida."
            )
        }
    }

    private fun current(values: List<Double>): Insight {
        val average = values.average()
        val hasPositive = values.any { it > 50.0 }
        val hasNegative = values.any { it < -50.0 }
        val ratio = relativeSpread(values, floor = 250.0)
        val context = "Média: ${formatCurrent(average)}"
        return when {
            hasPositive && hasNegative -> Insight(
                ChargeConditionInterpreter.Level.ELEVATED,
                "Corrente alternando",
                context,
                "A leitura passou entre entrada e saída de energia. Quando a média fica negativa, o celular consumiu mais energia do que recebeu durante parte do período."
            )
            ratio >= 0.80 -> Insight(
                ChargeConditionInterpreter.Level.ELEVATED,
                "Corrente oscilando",
                context,
                if (average < 0.0) {
                    "A corrente variou bastante e a média ficou negativa, indicando consumo líquido de bateria no período."
                } else {
                    "A quantidade de energia entrando no aparelho variou bastante durante o período."
                }
            )
            average < -50.0 -> Insight(
                ChargeConditionInterpreter.Level.INFO,
                "Consumo da bateria",
                context,
                "A média ficou negativa: o aparelho consumiu mais energia do que recebeu no período analisado. Isso é esperado quando ele está fora da fonte."
            )
            else -> Insight(
                ChargeConditionInterpreter.Level.GOOD,
                "Corrente estável",
                context,
                "A corrente permaneceu relativamente consistente no período analisado."
            )
        }
    }

    private fun power(values: List<Double>): Insight {
        val average = values.average()
        val peak = values.maxOrNull() ?: average
        val ratio = relativeSpread(values, floor = 0.5)
        val context = "Média ${formatPower(average)} • Pico ${formatPower(peak)}"
        return when {
            ratio >= 1.0 -> Insight(
                ChargeConditionInterpreter.Level.CRITICAL,
                "Carga instável",
                context,
                "Houve grandes oscilações de potência. Cabo, fonte, temperatura ou condições de um painel solar podem influenciar esse comportamento."
            )
            ratio >= 0.45 -> Insight(
                ChargeConditionInterpreter.Level.ELEVATED,
                "Potência oscilando",
                context,
                "A potência mudou bastante durante o período. Vale observar se a variação coincide com calor, movimento do cabo ou mudança de luz."
            )
            average <= 1.0 -> Insight(
                ChargeConditionInterpreter.Level.ATTENTION,
                "Potência baixa",
                context,
                "A potência média observada no aparelho está baixa. Isso não prova falha na fonte, mas pode explicar uma carga lenta."
            )
            else -> Insight(
                ChargeConditionInterpreter.Level.GOOD,
                "Potência estável",
                context,
                "A potência permaneceu relativamente constante no período analisado."
            )
        }
    }

    private fun temperature(values: List<Double>): Insight {
        val latest = values.last()
        val min = values.minOrNull() ?: latest
        val max = values.maxOrNull() ?: latest
        val range = "Faixa observada: ${formatTemp(min)}–${formatTemp(max)}"
        return when {
            max >= 45.0 -> Insight(
                ChargeConditionInterpreter.Level.CRITICAL,
                "Aparelho muito quente",
                range,
                "A temperatura ficou elevada e pode fazer o próprio aparelho limitar a velocidade de carregamento."
            )
            max >= 42.0 -> Insight(
                ChargeConditionInterpreter.Level.ELEVATED,
                "Aparelho quente",
                range,
                "A temperatura ficou alta durante o período. O próprio celular pode reduzir a carga para se proteger."
            )
            max >= 38.0 -> Insight(
                ChargeConditionInterpreter.Level.ATTENTION,
                "Temperatura moderada",
                range,
                "O aparelho está morno. Continue observando se a temperatura subir durante a carga."
            )
            else -> Insight(
                ChargeConditionInterpreter.Level.GOOD,
                "Temperatura normal",
                range,
                "A temperatura observada permaneceu em uma faixa confortável para o aparelho."
            )
        }
    }

    private fun relativeSpread(values: List<Double>, floor: Double): Double {
        if (values.size < 3) return 0.0
        val min = values.minOrNull() ?: return 0.0
        val maxValue = values.maxOrNull() ?: return 0.0
        val base = max(abs(values.average()), floor)
        return abs(maxValue - min) / base
    }

    private fun signedPercent(value: Double): String = when {
        value > 0.0 -> "+${value.toInt()}%"
        value < 0.0 -> "${value.toInt()}%"
        else -> "0%"
    }

    private fun formatCurrent(value: Double): String = String.format(Locale.getDefault(), "%.0f mA", value)
    private fun formatPower(value: Double): String = String.format(Locale.getDefault(), "%.2f W", value)
    private fun formatTemp(value: Double): String {
        val rounded = kotlin.math.round(value)
        val pattern = if (abs(value - rounded) < 0.05) "%.0f °C" else "%.1f °C"
        return String.format(Locale.getDefault(), pattern, value)
    }

    private const val MIN_INTERPRETATION_SAMPLES = 3
}
