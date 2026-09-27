package com.minhabateria.app.settings

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.minhabateria.app.R
import com.minhabateria.app.audio.VoiceAlertEvent
import com.minhabateria.app.audio.VoiceAlertGroup
import com.minhabateria.app.audio.VoiceAlertPreferences
import com.minhabateria.app.audio.VoiceAlerts
import com.minhabateria.app.ui.SystemBars

class AudioSettingsActivity : Activity() {
    private lateinit var preferences: VoiceAlertPreferences
    private lateinit var groupsContainer: LinearLayout
    private val rowStatus = mutableMapOf<VoiceAlertEvent, TextView>()
    private val expandedGroups = mutableSetOf<VoiceAlertGroup>()
    private var pendingEvent: VoiceAlertEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_settings)
        SystemBars.apply(this, findViewById(R.id.audioSettingsRoot))
        preferences = VoiceAlertPreferences(this)
        groupsContainer = findViewById(R.id.voiceGroupsContainer)
        pendingEvent = savedInstanceState?.getString(STATE_PENDING_EVENT)?.let(::eventByName)
        savedInstanceState?.getStringArrayList(STATE_EXPANDED)?.mapNotNull(::groupByName)?.let(expandedGroups::addAll)
        if (expandedGroups.isEmpty()) expandedGroups.add(VoiceAlertGroup.CHARGING)
        bindGeneralControls()
        renderGroups()
        findViewById<View>(R.id.audioBackButton).setOnClickListener { finish() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_PENDING_EVENT, pendingEvent?.name)
        outState.putStringArrayList(STATE_EXPANDED, ArrayList(expandedGroups.map { it.name }))
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_AUDIO || resultCode != RESULT_OK) return
        val event = pendingEvent ?: return
        val uri = data?.data ?: return
        val flags = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
        runCatching {
            contentResolver.takePersistableUriPermission(uri, flags)
            preferences.setCustomUri(event, uri)
        }.onSuccess {
            updateRowStatus(event)
            Toast.makeText(this, "Áudio personalizado salvo.", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(this, "Não foi possível manter acesso a esse arquivo.", Toast.LENGTH_LONG).show()
        }
        pendingEvent = null
    }

    private fun bindGeneralControls() {
        findViewById<CheckBox>(R.id.voiceAlertsEnabled).apply {
            isChecked = preferences.isEnabled()
            setOnCheckedChangeListener { _, checked -> preferences.setEnabled(checked) }
        }
        findViewById<CheckBox>(R.id.voiceBackgroundEnabled).apply {
            isChecked = preferences.allowBackground()
            setOnCheckedChangeListener { _, checked -> preferences.setAllowBackground(checked) }
        }
        val value = findViewById<TextView>(R.id.voiceVolumeValue)
        findViewById<SeekBar>(R.id.voiceVolumeSeek).apply {
            progress = preferences.volume()
            value.text = "$progress%"
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    value.text = "$progress%"
                    if (fromUser) preferences.setVolume(progress)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }
    }

    private fun renderGroups() {
        groupsContainer.removeAllViews()
        rowStatus.clear()
        VoiceAlertGroup.entries.forEach { group -> groupsContainer.addView(createGroup(group)) }
    }

    private fun createGroup(group: VoiceAlertGroup): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_card)
            setPadding(dp(14), dp(10), dp(14), dp(10))
        }
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.bottomMargin = dp(8)
        card.layoutParams = params

        val header = TextView(this).apply {
            text = headerText(group)
            setTextColor(getColor(R.color.text_primary))
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(4))
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = if (group in expandedGroups) View.VISIBLE else View.GONE
        }
        header.setOnClickListener {
            val expanding = body.visibility != View.VISIBLE
            if (expanding) expandedGroups.add(group) else expandedGroups.remove(group)
            header.text = headerText(group)
            if (expanding) {
                body.visibility = View.VISIBLE
                body.alpha = 0f
                body.animate().alpha(1f).setDuration(140L).start()
            } else {
                body.animate().alpha(0f).setDuration(120L).withEndAction {
                    body.visibility = View.GONE
                    body.alpha = 1f
                }.start()
            }
        }
        card.addView(header)
        VoiceAlertEvent.entries.filter { it.group == group }.forEach { body.addView(createEventRow(it)) }
        card.addView(body)
        return card
    }

    private fun createEventRow(event: VoiceAlertEvent): View {
        val row = LayoutInflater.from(this).inflate(R.layout.audio_event_row, groupsContainer, false)
        val enabled = row.findViewById<CheckBox>(R.id.audioEventEnabled)
        val status = row.findViewById<TextView>(R.id.audioEventStatus)
        enabled.text = event.label
        enabled.isChecked = preferences.isEventEnabled(event)
        enabled.setOnCheckedChangeListener { _, checked -> preferences.setEventEnabled(event, checked) }
        rowStatus[event] = status
        updateRowStatus(event)
        row.findViewById<Button>(R.id.audioEventTest).setOnClickListener { VoiceAlerts.runtime(this).test(event) }
        row.findViewById<Button>(R.id.audioEventChange).setOnClickListener { showAudioChoice(event) }
        return row
    }

    private fun showAudioChoice(event: VoiceAlertEvent) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_audio_choice, null)
        dialog.setContentView(view)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.66f)
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        view.findViewById<TextView>(R.id.audioChoiceTitle).text = event.label
        view.findViewById<TextView>(R.id.audioChoiceStatus).text =
            if (preferences.customUri(event) == null) "Áudio padrão" else "Áudio personalizado"
        view.findViewById<Button>(R.id.audioChoiceDefault).apply {
            text = if (preferences.customUri(event) == null) "Usando áudio padrão" else "Restaurar padrão"
            isEnabled = preferences.customUri(event) != null
            setOnClickListener {
                preferences.clearCustomUri(event)
                updateRowStatus(event)
                dialog.dismiss()
            }
        }
        view.findViewById<Button>(R.id.audioChoiceFile).setOnClickListener {
            pendingEvent = event
            dialog.dismiss()
            openAudioPicker()
        }
        view.findViewById<Button>(R.id.audioChoiceTest).setOnClickListener { VoiceAlerts.runtime(this).test(event) }
        view.findViewById<Button>(R.id.audioChoiceClose).setOnClickListener { dialog.dismiss() }
        dialog.setOnShowListener {
            dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.90f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(150L).start()
        }
        dialog.show()
    }

    private fun openAudioPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        runCatching { startActivityForResult(intent, REQUEST_AUDIO) }.onFailure {
            Toast.makeText(this, "Nenhum seletor de áudio disponível.", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateRowStatus(event: VoiceAlertEvent) {
        rowStatus[event]?.apply {
            val custom = preferences.customUri(event) != null
            text = if (custom) "Personalizado" else "Padrão"
            setTextColor(getColor(if (custom) R.color.accent_green else R.color.text_muted))
        }
    }

    private fun headerText(group: VoiceAlertGroup): String =
        "${group.label}  ${if (group in expandedGroups) "⌃" else "⌄"}"

    private fun eventByName(name: String): VoiceAlertEvent? = VoiceAlertEvent.entries.firstOrNull { it.name == name }
    private fun groupByName(name: String): VoiceAlertGroup? = VoiceAlertGroup.entries.firstOrNull { it.name == name }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private companion object {
        const val REQUEST_AUDIO = 3101
        const val STATE_PENDING_EVENT = "pending_audio_event"
        const val STATE_EXPANDED = "expanded_audio_groups"
    }
}
