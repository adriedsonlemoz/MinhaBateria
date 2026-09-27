package com.minhabateria.app.interpretation

import com.minhabateria.app.battery.BatteryInfo
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.source.EnergySourceProfile
import kotlin.math.abs

object ChargeConditionInterpreter {
    enum class Level { GOOD, INFO, ATTENTION, ELEVATED, CRITICAL }

    data class Result(
        val level: Level,
        val title: String,
        val explanation: String
    )

    fun interpret(
        info: BatteryInfo?,
        session: ChargingSession.Snapshot?,
        profile: EnergySourceProfile?
    ): Result {
        if (info == null) {
            return Result(
                Level.INFO,
                "Aguardando leitura",
                "O aplicativo ainda não recebeu dados suficientes da bateria."
            )
        }

        val temperature = info.temperatureC ?: session?.maxTemperatureC
        if (temperature != null && temperature >= VERY_HOT_C) {
            return Result(
                Level.CRITICAL,
                "Aparelho muito quente",
                "A temperatura está elevada e pode fazer o próprio aparelho reduzir a velocidade de carga. Vale deixar o celular ventilar e evitar sol direto."
            )
        }
        if (temperature != null && temperature >= HOT_C) {
            return Result(
                Level.ELEVATED,
                "Aparelho quente",
                "A temperatura está alta. Isso pode reduzir a velocidade de carregamento mesmo quando a fonte está funcionando normalmente."
            )
        }

        if (info.isPlugged == false) {
            return when {
                info.currentMa?.let { it < -50.0 } == true -> Result(
                    Level.INFO,
                    "Usando a bateria",
                    "O aparelho está consumindo energia da própria bateria neste momento."
                )
                else -> Result(
                    Level.INFO,
                    "Sem fonte conectada",
                    "Conecte uma fonte para avaliar a qualidade do carregamento."
                )
            }
        }

        if (info.isPlugged == true && info.isCharging == false) {
            return Result(
                Level.ATTENTION,
                "Carga pausada",
                "A fonte está conectada, mas o Android não informa carregamento ativo agora. Isso pode ocorrer por limite de bateria, temperatura ou gerenciamento do próprio aparelho."
            )
        }

        if (info.isCharging != true) {
            return Result(
                Level.INFO,
                "Estado indefinido",
                "O Android ainda não confirmou se a bateria está carregando."
            )
        }

        val current = info.currentMa
        if (current != null && current < -NEGATIVE_CURRENT_TOLERANCE_MA) {
            return Result(
                Level.CRITICAL,
                "Possível perda de carga",
                "Mesmo conectado, o aparelho está consumindo mais energia do que a leitura indica estar recebendo agora. Verifique cabo, fonte, temperatura e consumo do celular."
            )
        }

        val variation = session?.powerVariationRatio
        if (variation != null && variation >= VERY_UNSTABLE_RATIO) {
            return Result(
                Level.CRITICAL,
                "Carga muito instável",
                "A potência variou muito durante a sessão. Cabo, fonte, temperatura ou, em painel solar, mudanças de luz podem influenciar."
            )
        }
        if (variation != null && variation >= UNSTABLE_RATIO) {
            return Result(
                Level.ELEVATED,
                "Carga oscilando",
                "A quantidade de energia recebida mudou bastante durante a sessão. Vale observar cabo, fonte e condições de uso."
            )
        }

        val enoughSession = (session?.chargingTimeMs ?: 0L) >= MIN_SESSION_FOR_SPEED_MS
        val observedPower = session?.averagePowerW ?: info.powerW
        if (enoughSession && observedPower != null && observedPower in 0.0..SLOW_POWER_W) {
            return Result(
                Level.ATTENTION,
                "Carga lenta",
                "O aparelho está recebendo pouca potência no período observado. Isso não confirma defeito, mas vale verificar cabo, fonte, temperatura e uso intenso do celular."
            )
        }

        val nominal = profile?.nominalPowerW
        if (enoughSession && observedPower != null && nominal != null && nominal > 0.0) {
            val ratio = observedPower / nominal
            if (ratio < LOW_REFERENCE_RATIO && abs(nominal - observedPower) >= 3.0) {
                return Result(
                    Level.ATTENTION,
                    "Abaixo da referência configurada",
                    "A potência observada no aparelho está bem abaixo do valor informado para a fonte. A leitura do Android mede o que chega ao celular e não a saída total da fonte."
                )
            }
        }

        if (variation == null) {
            return Result(
                Level.INFO,
                "Carregando",
                "A carga está ativa. Ainda faltam amostras para dizer se ela está estável."
            )
        }

        return Result(
            Level.GOOD,
            "Carga normal",
            "O celular está recebendo energia de forma relativamente estável no período observado."
        )
    }


    fun interpretSession(
        session: ChargingSession.Snapshot?,
        profile: EnergySourceProfile?
    ): Result {
        if (session?.elapsedMs == null) {
            return Result(
                Level.INFO,
                "Aguardando dados",
                "A sessão ainda não possui medições suficientes para uma interpretação."
            )
        }

        val temperature = session.maxTemperatureC
        if (temperature != null && temperature >= VERY_HOT_C) {
            return Result(
                Level.CRITICAL,
                "Celular muito quente",
                "A sessão registrou temperatura elevada. Isso pode ter feito o próprio aparelho reduzir a velocidade de carregamento."
            )
        }
        if (temperature != null && temperature >= HOT_C) {
            return Result(
                Level.ELEVATED,
                "Celular quente",
                "A sessão atingiu temperatura alta. O calor pode reduzir a velocidade de carregamento mesmo com a fonte funcionando normalmente."
            )
        }

        val variation = session.powerVariationRatio
        if (variation != null && variation >= VERY_UNSTABLE_RATIO) {
            return Result(
                Level.CRITICAL,
                "Carga muito instável",
                "A potência mudou muito ao longo da sessão. Cabo, fonte, temperatura ou, em painel solar, mudanças de luz podem influenciar."
            )
        }
        if (variation != null && variation >= UNSTABLE_RATIO) {
            return Result(
                Level.ELEVATED,
                "Carga oscilando",
                "A potência variou bastante durante a sessão. Vale observar cabo, fonte, temperatura e condições de uso."
            )
        }

        val enoughSession = (session.chargingTimeMs ?: 0L) >= MIN_SESSION_FOR_SPEED_MS
        val observedPower = session.averagePowerW
        if (enoughSession && observedPower != null && observedPower in 0.0..SLOW_POWER_W) {
            return Result(
                Level.ATTENTION,
                "Carga lenta",
                "A potência média observada no aparelho foi baixa nesta sessão. Isso não confirma defeito, mas pode explicar um carregamento demorado."
            )
        }

        val nominal = profile?.nominalPowerW
        if (enoughSession && observedPower != null && nominal != null && nominal > 0.0) {
            val ratio = observedPower / nominal
            if (ratio < LOW_REFERENCE_RATIO && abs(nominal - observedPower) >= 3.0) {
                return Result(
                    Level.ATTENTION,
                    "Abaixo da referência configurada",
                    "A potência média observada no aparelho ficou bem abaixo da referência informada. Isso não mede diretamente a saída total da fonte."
                )
            }
        }

        if (variation == null) {
            return Result(
                Level.INFO,
                "Sessão registrada",
                "Há dados da sessão, mas ainda não existem amostras suficientes de potência para avaliar a estabilidade com segurança."
            )
        }

        return Result(
            Level.GOOD,
            "Carga estável",
            "A energia permaneceu relativamente constante durante a sessão observada."
        )
    }

    fun temperatureLabel(celsius: Double?): String = when {
        celsius == null -> "Indisponível"
        celsius >= VERY_HOT_C -> "Muito quente"
        celsius >= HOT_C -> "Quente"
        celsius >= WARM_C -> "Morna"
        else -> "Normal"
    }

    private const val WARM_C = 38.0
    private const val HOT_C = 42.0
    private const val VERY_HOT_C = 45.0
    private const val NEGATIVE_CURRENT_TOLERANCE_MA = 50.0
    private const val SLOW_POWER_W = 1.0
    private const val LOW_REFERENCE_RATIO = 0.20
    private const val UNSTABLE_RATIO = 0.25
    private const val VERY_UNSTABLE_RATIO = 0.45
    private const val MIN_SESSION_FOR_SPEED_MS = 2 * 60 * 1000L
}
