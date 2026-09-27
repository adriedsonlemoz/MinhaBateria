package com.minhabateria.app.source

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import com.minhabateria.app.monitoring.SourceConnectionState

object SourceConnectionPromptController {
    private var dialogShowing = false
    private val supportedScreens = setOf(
        "MainActivity", "ChartsActivity", "SessionActivity", "HistoryActivity", "DischargeRateActivity"
    )

    fun maybeShow(activity: Activity) {
        if (activity.javaClass.simpleName !in supportedScreens || activity.isFinishing || activity.isDestroyed) return
        val state = SourceConnectionState(activity)
        if (!state.shouldPrompt() || dialogShowing) return
        state.markPromptShown()
        val store = SourceProfileStore(activity)
        val recent = store.recentProfile()
        if (recent == null) {
            showProfiles(activity, store, state)
            return
        }

        dialogShowing = true
        val transport = state.detectedTransportLabel()?.let {
            "\n\nAndroid: alimentação conectada via $it. Isso não identifica fisicamente a fonte."
        }.orEmpty()
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Fonte conectada")
            .setMessage(
                "Qual fonte você está usando?\n\nÚltima fonte utilizada:\n${profileLabel(recent)}$transport"
            )
            .setPositiveButton("Usar esta fonte") { _, _ ->
                store.selectProfile(recent.id)?.let(state::assignProfile)
            }
            .setNeutralButton("Escolher outra", null)
            .setNegativeButton("Agora não", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                dialog.dismiss()
                showProfiles(activity, store, state)
            }
        }
        dialog.setOnDismissListener { dialogShowing = false }
        dialog.show()
    }

    private fun showProfiles(activity: Activity, store: SourceProfileStore, state: SourceConnectionState) {
        if (activity.isFinishing || activity.isDestroyed) return
        val profiles = store.profiles()
        val labels = profiles.map(::profileLabel) + "＋ Adicionar outra fonte"
        dialogShowing = true
        AlertDialog.Builder(activity)
            .setTitle("Fonte conectada")
            .setMessage("Escolha a fonte física que você está usando. O Android detecta a alimentação, mas não identifica o painel, power bank ou modelo específico.")
            .setItems(labels.toTypedArray()) { _, which ->
                if (which < profiles.size) {
                    store.selectProfile(profiles[which].id)?.let(state::assignProfile)
                } else {
                    activity.startActivity(
                        Intent(activity, SourceProfileActivity::class.java)
                            .putExtra(SourceProfileActivity.EXTRA_NEW_PROFILE, true)
                            .putExtra(SourceProfileActivity.EXTRA_ASSIGN_TO_CONNECTED_SESSION, true)
                    )
                }
            }
            .setNegativeButton("Agora não", null)
            .setOnDismissListener { dialogShowing = false }
            .show()
    }

    private fun profileLabel(profile: EnergySourceProfile): String =
        "${typeIcon(profile.type)} ${profile.name}"

    private fun typeIcon(type: EnergySourceType): String = when (type) {
        EnergySourceType.SOLAR_PANEL -> "☀️"
        EnergySourceType.CHARGER -> "🔌"
        EnergySourceType.POWER_BANK -> "🔋"
        EnergySourceType.OTHER -> "⚡"
    }
}
