package com.minhabateria.app.source

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.minhabateria.app.R
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
        } else {
            showRecent(activity, store, state, recent)
        }
    }

    private fun showRecent(
        activity: Activity,
        store: SourceProfileStore,
        state: SourceConnectionState,
        recent: EnergySourceProfile
    ) {
        val dialog = buildDialog(activity, R.layout.dialog_source_connected)
        dialogShowing = true
        val view = dialog.window?.decorView ?: return
        view.findViewById<TextView>(R.id.sourceDialogType).text = "${typeIcon(recent.type)} ${recent.type.label}"
        view.findViewById<TextView>(R.id.sourceDialogName).text = recent.name
        view.findViewById<TextView>(R.id.sourceDialogPower).text =
            recent.nominalPowerW?.let { SourceProfileNameBuilder.formatPower(it) } ?: "Potência não informada"
        view.findViewById<TextView>(R.id.sourceDialogTransport).text =
            state.detectedTransportLabel()?.let {
                "Android: alimentação conectada via $it. Isso não identifica fisicamente a fonte."
            } ?: "O Android detectou alimentação, mas não identifica fisicamente a fonte."

        view.findViewById<Button>(R.id.sourceDialogUse).setOnClickListener {
            store.selectProfile(recent.id)?.let(state::assignProfile)
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.sourceDialogChoose).setOnClickListener {
            dialog.dismiss()
            showProfiles(activity, store, state)
        }
        view.findViewById<Button>(R.id.sourceDialogLater).setOnClickListener { dialog.dismiss() }
        dialog.setOnDismissListener { dialogShowing = false }
        showAnimated(activity, dialog)
    }

    private fun showProfiles(activity: Activity, store: SourceProfileStore, state: SourceConnectionState) {
        if (activity.isFinishing || activity.isDestroyed) return
        val dialog = buildDialog(activity, R.layout.dialog_source_picker)
        dialogShowing = true
        val view = dialog.window?.decorView ?: return
        val list = view.findViewById<LinearLayout>(R.id.sourcePickerList)
        store.profiles().forEach { profile ->
            val button = Button(activity).apply {
                text = "${typeIcon(profile.type)} ${profile.name}"
                setAllCaps(false)
                setTextColor(activity.getColor(R.color.text_primary))
                textSize = 13f
                setBackgroundResource(R.drawable.bg_secondary_button)
                setOnClickListener {
                    store.selectProfile(profile.id)?.let(state::assignProfile)
                    dialog.dismiss()
                }
            }
            list.addView(button, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 46)).apply {
                bottomMargin = dp(activity, 7)
            })
        }
        view.findViewById<Button>(R.id.sourcePickerAdd).setOnClickListener {
            dialog.dismiss()
            activity.startActivity(
                Intent(activity, SourceProfileActivity::class.java)
                    .putExtra(SourceProfileActivity.EXTRA_NEW_PROFILE, true)
                    .putExtra(SourceProfileActivity.EXTRA_ASSIGN_TO_CONNECTED_SESSION, true)
            )
        }
        view.findViewById<Button>(R.id.sourcePickerCancel).setOnClickListener { dialog.dismiss() }
        dialog.setOnDismissListener { dialogShowing = false }
        showAnimated(activity, dialog)
    }

    private fun buildDialog(activity: Activity, layout: Int): Dialog = Dialog(activity).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(LayoutInflater.from(activity).inflate(layout, null))
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.66f)
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    private fun showAnimated(activity: Activity, dialog: Dialog) {
        dialog.setOnShowListener {
            dialog.window?.setLayout(
                (activity.resources.displayMetrics.widthPixels * 0.90f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val content = dialog.window?.decorView ?: return@setOnShowListener
            content.alpha = 0f
            content.translationY = 16f
            content.animate().alpha(1f).translationY(0f).setDuration(160L).start()
        }
        dialog.show()
    }

    private fun dp(activity: Activity, value: Int): Int =
        (value * activity.resources.displayMetrics.density).toInt()

    private fun typeIcon(type: EnergySourceType): String = when (type) {
        EnergySourceType.SOLAR_PANEL -> "☀️"
        EnergySourceType.CHARGER -> "🔌"
        EnergySourceType.POWER_BANK -> "🔋"
        EnergySourceType.OTHER -> "⚡"
    }
}
