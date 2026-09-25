package com.minhabateria.app.ui

import android.app.Activity
import android.app.AlertDialog

object MeasurementHelpDialog {
    fun show(activity: Activity) {
        val message = """
Velocidade de carga (W)
Mostra quanta potência está sendo observada na bateria agora. É calculada com tensão × corrente do aparelho e não mede diretamente a saída do carregador.

Corrente (mA)
É o fluxo elétrico observado pelo Android na bateria naquele instante.

Tempo
É a duração da sessão desde que a fonte foi conectada.

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
