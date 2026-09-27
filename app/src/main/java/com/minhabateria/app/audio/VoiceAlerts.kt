package com.minhabateria.app.audio

import android.content.Context
import com.minhabateria.app.MinhaBateriaApplication

object VoiceAlerts {
    fun runtime(context: Context): VoiceAlertRuntime =
        (context.applicationContext as MinhaBateriaApplication).voiceAlertRuntime
}
