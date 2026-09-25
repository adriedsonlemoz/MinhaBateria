package com.minhabateria.app.measurement

object MeasurementCatalog {
    val batteryPercent = MeasurementDefinition(
        MeasurementOrigin.SYSTEM,
        "Porcentagem informada pelo sistema Android."
    )
    val chargingStatus = MeasurementDefinition(
        MeasurementOrigin.SYSTEM,
        "Estado de carga informado pelo sistema Android."
    )
    val detectedSource = MeasurementDefinition(
        MeasurementOrigin.SYSTEM,
        "Conexão de energia informada pelo sistema Android."
    )
    val voltage = MeasurementDefinition(
        MeasurementOrigin.SYSTEM,
        "Tensão da bateria informada pelo sistema Android."
    )
    val current = MeasurementDefinition(
        MeasurementOrigin.SYSTEM,
        "Corrente observada na bateria pelo BatteryManager, quando suportada."
    )
    val temperature = MeasurementDefinition(
        MeasurementOrigin.SYSTEM,
        "Temperatura da bateria informada pelo sistema Android."
    )
    val power = MeasurementDefinition(
        MeasurementOrigin.CALCULATED,
        "Potência calculada a partir de tensão e corrente disponíveis."
    )
    val elapsedTime = MeasurementDefinition(
        MeasurementOrigin.CALCULATED,
        "Tempo calculado a partir dos instantes da sessão."
    )
    val peakCurrent = MeasurementDefinition(
        MeasurementOrigin.CALCULATED,
        "Maior corrente válida observada durante a sessão."
    )
    val peakPower = MeasurementDefinition(
        MeasurementOrigin.CALCULATED,
        "Maior potência calculada durante a sessão."
    )
}
