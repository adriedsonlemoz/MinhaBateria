package com.minhabateria.app.ui

import android.app.Activity
import android.app.AlertDialog

object MeasurementHelpDialog {
    fun show(activity: Activity) {
        val message = """
Velocidade de carga (W)
Com uma fonte conectada, mostra a potência realmente observada na bateria naquele instante. É calculada com tensão × corrente do aparelho e não representa diretamente a saída nominal do carregador.

Velocidade de descarga (%/h)
Fora da tomada, este mesmo card passa a mostrar quanto da bateria está caindo por hora. Só aparece depois de pelo menos 3 minutos e 1% de queda real; antes disso o app mostra que ainda está calculando.

Corrente (mA)
É a leitura bruta informada pelo Android. Valor positivo significa corrente entrando na bateria e valor negativo significa corrente saindo. O app não troca o sinal para fazê-lo combinar com o estado de carga.

Autonomia
Fora da tomada, é uma projeção baseada exclusivamente no ritmo real da sessão de descarga. Enquanto a amostra ainda não for suficiente, aparece “Calculando autonomia…”.

Tempo até 100%
É uma previsão aproximada. O app usa a estimativa do Android quando disponível; caso contrário, só estima pelo ritmo da sessão depois de ter dados suficientes.

Energia recebida (Wh)
É a energia acumulada estimada ao integrar as leituras válidas durante a sessão.

Carga acumulada (mAh)
É a quantidade de carga elétrica estimada que entrou nesta sessão. Não é a capacidade total da bateria.

Tensão (V)
É a tensão da bateria informada pelo Android.

Temperatura (°C)
É a temperatura da bateria informada pelo sistema.
        """.trimIndent()

        AlertDialog.Builder(activity)
            .setTitle("Entenda os dados")
            .setMessage(message)
            .setPositiveButton("Entendi", null)
            .show()
    }
}
