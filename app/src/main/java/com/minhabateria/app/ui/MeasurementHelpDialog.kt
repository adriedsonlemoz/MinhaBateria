package com.minhabateria.app.ui

import android.app.Activity
import android.app.AlertDialog

object MeasurementHelpDialog {
    enum class Metric { POWER, DISCHARGE, CURRENT, TEMPERATURE, ENERGY, BATTERY_DROP }

    fun show(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Entenda os dados")
            .setMessage(fullHelp())
            .setPositiveButton("Entendi", null)
            .show()
    }

    fun showMetric(activity: Activity, metric: Metric) {
        val (title, message) = when (metric) {
            Metric.POWER -> "Potência (W)" to
                "W (watts) mostra quanta potência o aparelho está recebendo naquele momento. No Minha Bateria, esse valor é calculado com tensão × corrente informadas pelo Android e não mede diretamente a saída total da fonte."
            Metric.DISCHARGE -> "Ritmo de descarga (%/h)" to
                "Mostra quanto da bateria está caindo por hora com base na sessão real de descarga. O valor só aparece depois de tempo e queda suficientes para evitar uma estimativa enganosa."
            Metric.CURRENT -> "Corrente (mA)" to
                "mA mostra o fluxo de corrente informado pelo Android. Valor positivo indica entrada na bateria; valor negativo indica saída. O app preserva o sinal recebido e não o inverte para combinar com o estado de carga."
            Metric.TEMPERATURE -> "Temperatura (°C)" to
                "É a temperatura da bateria informada pelo sistema. Calor elevado pode fazer o próprio aparelho reduzir a velocidade de carregamento."
            Metric.ENERGY -> "Energia recebida (Wh)" to
                "Wh representa energia acumulada ao longo do tempo. No app, é uma estimativa calculada a partir das leituras válidas da sessão; não é uma medição direta na tomada ou na saída da fonte."
            Metric.BATTERY_DROP -> "Queda da bateria" to
                "Mostra quantos pontos percentuais a bateria perdeu desde o início da sessão atual de descarga. O tempo abaixo do valor indica por quanto tempo essa queda foi observada."
        }
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Entendi", null)
            .show()
    }

    private fun fullHelp(): String = """
Potência (W, watts)
Mostra quanta potência o aparelho está recebendo naquele momento. É calculada com tensão × corrente informadas pelo Android e não representa diretamente a saída nominal da fonte.

Velocidade de descarga (%/h)
Fora da tomada, mostra quanto da bateria está caindo por hora. Só aparece depois de pelo menos 3 minutos e 1% de queda real; antes disso o app informa que ainda está calculando.

Corrente (mA)
É a leitura bruta informada pelo Android. Valor positivo significa corrente entrando na bateria e valor negativo significa corrente saindo. O app não troca o sinal para fazê-lo combinar com o estado de carga.

Autonomia
Fora da tomada, é uma projeção baseada exclusivamente no ritmo real da sessão de descarga. Enquanto a amostra ainda não for suficiente, aparece que a autonomia está em preparação.

Tempo até 100%
É uma previsão aproximada. O app usa a estimativa do Android quando disponível; caso contrário, só estima pelo ritmo da sessão depois de ter dados suficientes.

Energia recebida (Wh)
Wh representa a energia recebida ao longo do tempo. No app, é uma estimativa calculada ao integrar as leituras válidas da sessão.

Carga acumulada (mAh)
mAh ajuda a representar a quantidade de carga acumulada. Aqui mostra uma estimativa do que entrou nesta sessão, não a capacidade total da bateria.

Tensão (V)
É a tensão da bateria informada pelo Android.

Temperatura (°C)
É a temperatura da bateria informada pelo sistema.
        """.trimIndent()
}
