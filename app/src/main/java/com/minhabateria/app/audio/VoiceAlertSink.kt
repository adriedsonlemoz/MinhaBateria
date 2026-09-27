package com.minhabateria.app.audio

interface VoiceAlertSink {
    fun play(event: VoiceAlertEvent, force: Boolean = false)
}
