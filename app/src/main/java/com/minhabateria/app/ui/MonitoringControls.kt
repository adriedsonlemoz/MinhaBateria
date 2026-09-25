package com.minhabateria.app.ui

import android.app.Activity
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.minhabateria.app.R

class MonitoringControls(activity: Activity) {
    private val status = activity.findViewById<TextView>(R.id.monitoringStateValue)
    private val startButton = activity.findViewById<Button>(R.id.startMonitoringButton)
    private val stopButton = activity.findViewById<Button>(R.id.stopMonitoringButton)
    private val resumeAfterBoot = activity.findViewById<CheckBox>(R.id.resumeAfterBootCheck)

    fun setResumeAfterBoot(enabled: Boolean) {
        resumeAfterBoot.isChecked = enabled
    }

    fun setActions(
        onStart: () -> Unit,
        onStop: () -> Unit,
        onResumeAfterBootChanged: (Boolean) -> Unit
    ) {
        startButton.setOnClickListener { onStart() }
        stopButton.setOnClickListener { onStop() }
        resumeAfterBoot.setOnCheckedChangeListener { _, checked ->
            onResumeAfterBootChanged(checked)
        }
    }

    fun render(requested: Boolean, running: Boolean) {
        status.text = when {
            running -> "Ativo"
            requested -> "Iniciando…"
            else -> "Inativo"
        }
        status.setTextColor(
            status.context.getColor(if (running) R.color.accent_green else R.color.accent_blue)
        )
        startButton.isEnabled = !requested
        stopButton.isEnabled = requested
        startButton.alpha = if (startButton.isEnabled) 1f else 0.45f
        stopButton.alpha = if (stopButton.isEnabled) 1f else 0.45f
    }
}
