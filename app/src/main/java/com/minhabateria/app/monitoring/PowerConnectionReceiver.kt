package com.minhabateria.app.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = SourceConnectionState(context)
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> state.onPowerConnected()
            Intent.ACTION_POWER_DISCONNECTED -> state.onPowerDisconnected()
        }
    }
}
