package com.minhabateria.app.audio

import com.minhabateria.app.R

enum class VoiceAlertGroup(val label: String) {
    CHARGING("Carregamento"),
    BATTERY("Bateria"),
    TEMPERATURE("Temperatura"),
    SESSION("Sessão e monitoramento")
}

enum class VoiceAlertEvent(
    val label: String,
    val group: VoiceAlertGroup,
    val rawResId: Int,
    val priority: Int,
    val cooldownMs: Long = 60_000L
) {
    SOURCE_CONNECTED("Fonte conectada", VoiceAlertGroup.CHARGING, R.raw.voice_source_connected, 1, 8_000L),
    SOURCE_DISCONNECTED("Fonte desconectada", VoiceAlertGroup.CHARGING, R.raw.voice_source_disconnected, 1, 8_000L),
    CHARGING_STARTED("Carregamento iniciado", VoiceAlertGroup.CHARGING, R.raw.voice_charging_started, 1, 8_000L),
    CHARGING_INTERRUPTED("Carregamento interrompido", VoiceAlertGroup.CHARGING, R.raw.voice_charging_interrupted, 2, 15_000L),
    CHARGING_RESTORED("Carregamento restabelecido", VoiceAlertGroup.CHARGING, R.raw.voice_charging_restored, 2, 15_000L),
    CHARGE_SLOW("Carga lenta", VoiceAlertGroup.CHARGING, R.raw.voice_charge_slow, 2, 120_000L),
    SOURCE_LOW_POWER("Pouca energia", VoiceAlertGroup.CHARGING, R.raw.voice_source_low_power, 2, 120_000L),
    CHARGE_UNSTABLE("Carga instável", VoiceAlertGroup.CHARGING, R.raw.voice_charge_unstable, 2, 120_000L),
    CONNECTION_OSCILLATING("Conexão oscilando", VoiceAlertGroup.CHARGING, R.raw.voice_connection_oscillating, 2, 120_000L),
    CHARGE_NORMAL("Carga normalizada", VoiceAlertGroup.CHARGING, R.raw.voice_charge_normal, 1, 30_000L),
    BATTERY_80("Bateria em 80%", VoiceAlertGroup.BATTERY, R.raw.voice_battery_80, 1),
    BATTERY_90("Bateria em 90%", VoiceAlertGroup.BATTERY, R.raw.voice_battery_90, 1),
    BATTERY_FULL("Bateria totalmente carregada", VoiceAlertGroup.BATTERY, R.raw.voice_battery_full, 1),
    BATTERY_20("Bateria baixa em 20%", VoiceAlertGroup.BATTERY, R.raw.voice_battery_20, 2),
    BATTERY_10("Bateria crítica em 10%", VoiceAlertGroup.BATTERY, R.raw.voice_battery_10, 3),
    BATTERY_5("Bateria extremamente baixa em 5%", VoiceAlertGroup.BATTERY, R.raw.voice_battery_5, 4),
    DISCHARGING_CONNECTED("Descarga mesmo conectado", VoiceAlertGroup.BATTERY, R.raw.voice_discharging_connected, 2, 120_000L),
    TEMPERATURE_WARM("Aparelho ficando quente", VoiceAlertGroup.TEMPERATURE, R.raw.voice_temperature_warm, 2, 120_000L),
    TEMPERATURE_CRITICAL("Temperatura muito alta", VoiceAlertGroup.TEMPERATURE, R.raw.voice_temperature_critical, 5, 60_000L),
    TEMPERATURE_NORMAL("Temperatura normalizada", VoiceAlertGroup.TEMPERATURE, R.raw.voice_temperature_normal, 1, 30_000L),
    SESSION_STARTED("Nova sessão iniciada", VoiceAlertGroup.SESSION, R.raw.voice_session_started, 1, 10_000L),
    SESSION_ENDED("Sessão encerrada", VoiceAlertGroup.SESSION, R.raw.voice_session_ended, 1, 10_000L),
    MONITORING_STARTED("Monitoramento iniciado", VoiceAlertGroup.SESSION, R.raw.voice_monitoring_started, 1, 10_000L),
    MONITORING_STOPPED("Monitoramento encerrado", VoiceAlertGroup.SESSION, R.raw.voice_monitoring_stopped, 1, 10_000L)
}
