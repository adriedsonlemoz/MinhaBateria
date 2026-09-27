package com.minhabateria.app.source

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.minhabateria.app.R
import com.minhabateria.app.monitoring.SourceConnectionState
import com.minhabateria.app.ui.SystemBars

class SourceProfileActivity : Activity() {
    private lateinit var store: SourceProfileStore
    private lateinit var typeButton: Button
    private lateinit var brandInput: EditText
    private lateinit var modelInput: EditText
    private lateinit var powerInput: EditText
    private lateinit var customNameInput: EditText
    private lateinit var outputsInput: EditText
    private lateinit var technologyInput: EditText
    private lateinit var portInput: EditText
    private lateinit var cableInput: EditText
    private lateinit var capacityInput: EditText
    private lateinit var voltageInput: EditText
    private lateinit var currentInput: EditText
    private lateinit var controllerInput: EditText
    private lateinit var sourceDataHint: TextView
    private lateinit var powerHelp: TextView
    private lateinit var namePreview: TextView
    private lateinit var previewDetails: TextView
    private lateinit var advancedCount: TextView
    private lateinit var advancedToggle: TextView
    private lateinit var advancedContainer: View
    private lateinit var suggestionAdvancedContainer: View
    private lateinit var electricalDetails: View
    private lateinit var cableDetails: View
    private lateinit var powerBankDetails: View
    private lateinit var solarDetails: View
    private lateinit var suggestions: SourceProfileSuggestionBinder

    private var initialSetup = false
    private var createNew = false
    private var assignToConnectedSession = false
    private var advancedVisible = false
    private var selectedType: EnergySourceType = EnergySourceType.CHARGER
    private var editingProfileId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source_profile)
        SystemBars.apply(this, findViewById(R.id.sourceProfileRoot))
        store = SourceProfileStore(this)
        initialSetup = intent.getBooleanExtra(EXTRA_INITIAL_SETUP, false)
        createNew = intent.getBooleanExtra(EXTRA_NEW_PROFILE, false)
        assignToConnectedSession = intent.getBooleanExtra(EXTRA_ASSIGN_TO_CONNECTED_SESSION, false)
        bindViews()
        bindActions()
        loadExistingProfile()
        updateFormForType()
        showAdvanced(false)
        updateNamePreview()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() = cancel()

    private fun bindViews() {
        typeButton = findViewById(R.id.sourceTypeButton)
        brandInput = findViewById(R.id.sourceBrandInput)
        modelInput = findViewById(R.id.sourceModelInput)
        powerInput = findViewById(R.id.sourcePowerInput)
        customNameInput = findViewById(R.id.sourceCustomNameInput)
        outputsInput = findViewById(R.id.sourceOutputsInput)
        technologyInput = findViewById(R.id.sourceTechnologyInput)
        portInput = findViewById(R.id.sourcePortInput)
        cableInput = findViewById(R.id.sourceCableInput)
        capacityInput = findViewById(R.id.sourceCapacityInput)
        voltageInput = findViewById(R.id.sourceVoltageInput)
        currentInput = findViewById(R.id.sourceCurrentInput)
        controllerInput = findViewById(R.id.sourceControllerInput)
        sourceDataHint = findViewById(R.id.sourceDataHint)
        powerHelp = findViewById(R.id.sourcePowerHelp)
        namePreview = findViewById(R.id.sourceNamePreview)
        previewDetails = findViewById(R.id.sourceProfilePreviewDetails)
        advancedCount = findViewById(R.id.sourceAdvancedCount)
        advancedToggle = findViewById(R.id.sourceAdvancedToggle)
        advancedContainer = findViewById(R.id.sourceAdvancedContainer)
        suggestionAdvancedContainer = findViewById(R.id.suggestAdvancedContainer)
        electricalDetails = findViewById(R.id.electricalDetails)
        cableDetails = findViewById(R.id.cableDetails)
        powerBankDetails = findViewById(R.id.powerBankDetails)
        solarDetails = findViewById(R.id.solarDetails)
        suggestions = SourceProfileSuggestionBinder(
            this, brandInput, modelInput, powerInput, outputsInput, technologyInput,
            portInput, cableInput, capacityInput, voltageInput, currentInput, controllerInput,
            onChanged = { updateNamePreview() },
            onManualEntryRequested = { showAdvanced(true) }
        )
    }

    private fun bindActions() {
        findViewById<ImageButton>(R.id.sourceProfileBackButton).setOnClickListener { cancel() }
        findViewById<Button>(R.id.cancelSourceProfileButton).apply {
            text = if (initialSetup) "Agora não" else "Cancelar"
            setOnClickListener { cancel() }
        }
        findViewById<Button>(R.id.saveSourceProfileButton).setOnClickListener { save() }
        typeButton.setOnClickListener { chooseType() }
        advancedToggle.setOnClickListener { showAdvanced(!advancedVisible) }
        listOf(
            brandInput, modelInput, powerInput, customNameInput, outputsInput, technologyInput,
            portInput, cableInput, capacityInput, voltageInput, currentInput, controllerInput
        ).forEach { it.addTextChangedListener(formWatcher) }
    }

    private fun loadExistingProfile() {
        if (createNew) {
            selectedType = EnergySourceType.CHARGER
            return
        }
        val profile = store.getProfile() ?: return
        editingProfileId = profile.id
        selectedType = profile.type
        brandInput.setText(profile.brand.orEmpty())
        modelInput.setText(profile.model.orEmpty())
        powerInput.setText(profile.nominalPowerW.asInput())
        outputsInput.setText(profile.labelOutputs.orEmpty())
        technologyInput.setText(profile.technology.orEmpty())
        portInput.setText(profile.portType.orEmpty())
        cableInput.setText(profile.cableInfo.orEmpty())
        capacityInput.setText(profile.capacityMah?.toString().orEmpty())
        voltageInput.setText(profile.ratedVoltageV.asInput())
        currentInput.setText(profile.ratedCurrentA.asInput())
        controllerInput.setText(profile.controllerInfo.orEmpty())
        val generated = SourceProfileNameBuilder.build(profile.type, null, profile.brand, profile.model, profile.nominalPowerW)
        if (profile.name != generated) customNameInput.setText(profile.name)
    }

    private fun chooseType() {
        val values = EnergySourceType.entries.toTypedArray()
        val labels = values.map(::typeLabel).toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Tipo de fonte")
            .setSingleChoiceItems(labels, values.indexOf(selectedType)) { dialog, which ->
                val newType = values[which]
                if (newType != selectedType) {
                    selectedType = newType
                    clearTypeSpecificFields()
                    updateFormForType()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun save() {
        val type = selectedType
        val power = powerInput.decimalValue()
        if (type == EnergySourceType.SOLAR_PANEL && (power == null || power <= 0.0)) {
            return toast("Escolha a potência indicada no painel.")
        }
        if (powerInput.hasText() && (power == null || power <= 0.0)) return invalid(powerInput)
        val capacity = capacityInput.text.toString().trim().toIntOrNull()
        if (type == EnergySourceType.POWER_BANK && capacityInput.hasText() && (capacity == null || capacity <= 0)) {
            return invalid(capacityInput)
        }
        val voltage = voltageInput.decimalValue()
        val current = currentInput.decimalValue()
        if (type == EnergySourceType.SOLAR_PANEL && voltageInput.hasText() && (voltage == null || voltage <= 0.0)) {
            return invalid(voltageInput)
        }
        if (type == EnergySourceType.SOLAR_PANEL && currentInput.hasText() && (current == null || current <= 0.0)) {
            return invalid(currentInput)
        }

        val brand = brandInput.cleanText()
        val model = modelInput.cleanText()
        val name = SourceProfileNameBuilder.build(type, customNameInput.cleanText(), brand, model, power)
        val saved = store.save(
            EnergySourceProfile(
                type = type,
                name = name,
                nominalPowerW = power,
                brand = brand,
                model = model,
                labelOutputs = outputsInput.cleanText().takeUnless { type == EnergySourceType.SOLAR_PANEL },
                technology = technologyInput.cleanText().takeUnless { type == EnergySourceType.SOLAR_PANEL },
                portType = portInput.cleanText().takeUnless { type == EnergySourceType.SOLAR_PANEL },
                cableInfo = cableInput.cleanText().takeIf { type == EnergySourceType.CHARGER },
                capacityMah = capacity.takeIf { type == EnergySourceType.POWER_BANK },
                ratedVoltageV = voltage.takeIf { type == EnergySourceType.SOLAR_PANEL },
                ratedCurrentA = current.takeIf { type == EnergySourceType.SOLAR_PANEL },
                controllerInfo = controllerInput.cleanText().takeIf { type == EnergySourceType.SOLAR_PANEL },
                id = editingProfileId
            )
        )
        if (assignToConnectedSession) SourceConnectionState(this).assignProfile(saved)
        toast("Perfil salvo como $name")
        setResult(RESULT_OK)
        finish()
    }

    private fun updateFormForType() {
        typeButton.text = typeLabel(selectedType) + "  ▾"
        electricalDetails.visibility = visible(selectedType == EnergySourceType.CHARGER || selectedType == EnergySourceType.POWER_BANK)
        cableDetails.visibility = visible(selectedType == EnergySourceType.CHARGER)
        powerBankDetails.visibility = visible(selectedType == EnergySourceType.POWER_BANK)
        solarDetails.visibility = visible(selectedType == EnergySourceType.SOLAR_PANEL)
        suggestions.updateForType(selectedType)
        sourceDataHint.text = when (selectedType) {
            EnergySourceType.CHARGER -> "Escolha marca, modelo e potência. Protocolos, porta e cabo ficam nos dados técnicos."
            EnergySourceType.POWER_BANK -> "Escolha marca, modelo, potência e capacidade. O restante é opcional."
            EnergySourceType.SOLAR_PANEL -> "Escolha painel e potência nominal. Tensão, corrente e controlador ficam nos dados técnicos."
            EnergySourceType.OTHER -> "Informe somente o que você realmente sabe sobre a fonte."
        }
        powerHelp.text = if (selectedType == EnergySourceType.SOLAR_PANEL) {
            "No painel solar, use a potência nominal indicada na etiqueta."
        } else {
            "Use a potência máxima declarada pela fonte, se conhecida."
        }
        updateNamePreview()
    }

    private fun clearTypeSpecificFields() {
        outputsInput.setText("")
        technologyInput.setText("")
        portInput.setText("")
        cableInput.setText("")
        capacityInput.setText("")
        voltageInput.setText("")
        currentInput.setText("")
        controllerInput.setText("")
    }

    private fun showAdvanced(show: Boolean) {
        advancedVisible = show
        advancedContainer.visibility = visible(show)
        suggestionAdvancedContainer.visibility = visible(show)
        advancedToggle.text = if (show) "⚙  Ocultar dados técnicos  ‹" else "⚙  Dados técnicos da etiqueta  ›"
        advancedToggle.setTextColor(getColor(if (show) R.color.text_primary else R.color.text_secondary))
    }

    private fun updateNamePreview() {
        val power = powerInput.decimalValue()
        namePreview.text = "${typeIcon(selectedType)} " + SourceProfileNameBuilder.build(
            selectedType,
            customNameInput.cleanText(),
            brandInput.cleanText(),
            modelInput.cleanText(),
            power
        )
        previewDetails.text = buildBasicPreview(selectedType, power)
        advancedCount.text = "Dados técnicos opcionais configurados: ${advancedFieldCount(selectedType)}"
        suggestions.syncLabels()
    }

    private fun buildBasicPreview(type: EnergySourceType, power: Double?): String {
        val parts = mutableListOf<String>()
        brandInput.cleanText()?.let(parts::add)
        modelInput.cleanText()?.let(parts::add)
        power?.takeIf { it > 0.0 }?.let { parts += SourceProfileNameBuilder.formatPower(it) }
        if (type == EnergySourceType.POWER_BANK) {
            capacityInput.text.toString().trim().toIntOrNull()?.let { parts += formatCapacity(it) }
        }
        return parts.distinct().ifEmpty { listOf(type.label) }.joinToString(" • ")
    }

    private fun advancedFieldCount(type: EnergySourceType): Int = when (type) {
        EnergySourceType.CHARGER -> listOf(outputsInput, technologyInput, portInput, cableInput).count { it.hasText() }
        EnergySourceType.POWER_BANK -> listOf(outputsInput, technologyInput, portInput).count { it.hasText() }
        EnergySourceType.SOLAR_PANEL -> listOf(voltageInput, currentInput, controllerInput).count { it.hasText() }
        EnergySourceType.OTHER -> 0
    }

    private fun cancel() {
        if (initialSetup) store.dismissInitialSetup()
        finish()
    }

    private fun typeLabel(type: EnergySourceType): String = "${typeIcon(type)} ${type.label}"

    private fun typeIcon(type: EnergySourceType): String = when (type) {
        EnergySourceType.SOLAR_PANEL -> "☀️"
        EnergySourceType.CHARGER -> "🔌"
        EnergySourceType.POWER_BANK -> "🔋"
        EnergySourceType.OTHER -> "⚡"
    }

    private fun EditText.cleanText(): String? = text.toString().trim().takeIf { it.isNotBlank() }
    private fun EditText.decimalValue(): Double? = text.toString().trim().replace(',', '.').toDoubleOrNull()
    private fun EditText.hasText(): Boolean = text.toString().isNotBlank()
    private fun Double?.asInput(): String = this?.toString()?.removeSuffix(".0")?.replace('.', ',').orEmpty()
    private fun invalid(view: EditText) { showAdvanced(true); view.error = "Confira o valor informado"; view.requestFocus() }
    private fun toast(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    private fun visible(condition: Boolean): Int = if (condition) View.VISIBLE else View.GONE
    private fun formatCapacity(value: Int): String = "${value / 1_000}.${(value % 1_000).toString().padStart(3, '0')} mAh"

    private val formWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) = updateNamePreview()
    }

    companion object {
        const val EXTRA_INITIAL_SETUP = "source_initial_setup"
        const val EXTRA_NEW_PROFILE = "source_new_profile"
        const val EXTRA_ASSIGN_TO_CONNECTED_SESSION = "source_assign_to_connected_session"
    }
}
