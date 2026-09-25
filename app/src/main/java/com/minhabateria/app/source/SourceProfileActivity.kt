package com.minhabateria.app.source

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.minhabateria.app.R
import com.minhabateria.app.ui.SystemBars

class SourceProfileActivity : Activity() {
    private lateinit var store: SourceProfileStore
    private lateinit var typeGroup: RadioGroup
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
    private lateinit var electricalDetails: View
    private lateinit var cableDetails: View
    private lateinit var powerBankDetails: View
    private lateinit var solarDetails: View
    private lateinit var suggestions: SourceProfileSuggestionBinder
    private var initialSetup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source_profile)
        SystemBars.apply(this, findViewById(R.id.sourceProfileRoot))
        store = SourceProfileStore(this)
        initialSetup = intent.getBooleanExtra(EXTRA_INITIAL_SETUP, false)
        bindViews()
        bindActions()
        loadExistingProfile()
        updateFormForType()
        updateNamePreview()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() = cancel()

    private fun bindViews() {
        typeGroup = findViewById(R.id.sourceTypeGroup)
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
        electricalDetails = findViewById(R.id.electricalDetails)
        cableDetails = findViewById(R.id.cableDetails)
        powerBankDetails = findViewById(R.id.powerBankDetails)
        solarDetails = findViewById(R.id.solarDetails)
        suggestions = SourceProfileSuggestionBinder(
            this, brandInput, modelInput, powerInput, outputsInput, technologyInput,
            portInput, capacityInput
        ) { updateNamePreview() }
    }

    private fun bindActions() {
        findViewById<ImageButton>(R.id.sourceProfileBackButton).setOnClickListener { cancel() }
        findViewById<Button>(R.id.cancelSourceProfileButton).apply {
            text = if (initialSetup) "Agora não" else "Cancelar"
            setOnClickListener { cancel() }
        }
        findViewById<Button>(R.id.saveSourceProfileButton).setOnClickListener { save() }
        typeGroup.setOnCheckedChangeListener { _, _ -> updateFormForType() }
        listOf(brandInput, modelInput, powerInput, customNameInput).forEach { it.addTextChangedListener(nameWatcher) }
    }

    private fun loadExistingProfile() {
        val profile = store.getProfile() ?: return
        typeGroup.check(radioId(profile.type))
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

    private fun save() {
        val type = selectedType() ?: return toast("Selecione o tipo da fonte.")
        val power = powerInput.decimalValue()
        if (type == EnergySourceType.SOLAR_PANEL && (power == null || power <= 0.0)) {
            powerInput.error = "Informe a potência nominal indicada no painel"
            return
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
        store.save(
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
                controllerInfo = controllerInput.cleanText().takeIf { type == EnergySourceType.SOLAR_PANEL }
            )
        )
        toast("Perfil salvo como $name")
        setResult(RESULT_OK)
        finish()
    }

    private fun updateFormForType() {
        val type = selectedType()
        electricalDetails.visibility = if (type == EnergySourceType.SOLAR_PANEL || type == null) View.GONE else View.VISIBLE
        cableDetails.visibility = if (type == EnergySourceType.CHARGER) View.VISIBLE else View.GONE
        powerBankDetails.visibility = if (type == EnergySourceType.POWER_BANK) View.VISIBLE else View.GONE
        solarDetails.visibility = if (type == EnergySourceType.SOLAR_PANEL) View.VISIBLE else View.GONE
        suggestions.updateForType(type)
        sourceDataHint.text = when (type) {
            EnergySourceType.CHARGER -> "Escolha sugestões de marca, modelo, W, protocolo e saída. Digite somente se sua etiqueta tiver algo diferente."
            EnergySourceType.POWER_BANK -> "Use os seletores para marca, capacidade, potência, protocolo e saída; ajuste manualmente apenas o que faltar."
            EnergySourceType.SOLAR_PANEL -> "Escolha marca e potência sugeridas do painel; tensão, corrente e controlador continuam opcionais."
            EnergySourceType.OTHER -> "Preencha somente os dados que você conhece. O nome será montado automaticamente."
            null -> "Escolha o tipo da fonte para receber orientação de preenchimento."
        }
        powerHelp.text = if (type == EnergySourceType.SOLAR_PANEL) {
            "Obrigatória para painel solar. Informe o valor nominal da etiqueta."
        } else {
            "Opcional, mas recomendada. Informe a potência máxima declarada pela fonte."
        }
        updateNamePreview()
    }

    private fun updateNamePreview() {
        val type = selectedType() ?: return
        namePreview.text = SourceProfileNameBuilder.build(
            type,
            customNameInput.cleanText(),
            brandInput.cleanText(),
            modelInput.cleanText(),
            powerInput.decimalValue()
        )
    }

    private fun cancel() {
        if (initialSetup) store.dismissInitialSetup()
        finish()
    }

    private fun selectedType(): EnergySourceType? = when (typeGroup.checkedRadioButtonId) {
        R.id.sourceTypeSolar -> EnergySourceType.SOLAR_PANEL
        R.id.sourceTypeCharger -> EnergySourceType.CHARGER
        R.id.sourceTypePowerBank -> EnergySourceType.POWER_BANK
        R.id.sourceTypeOther -> EnergySourceType.OTHER
        else -> null
    }

    private fun radioId(type: EnergySourceType): Int = when (type) {
        EnergySourceType.SOLAR_PANEL -> R.id.sourceTypeSolar
        EnergySourceType.CHARGER -> R.id.sourceTypeCharger
        EnergySourceType.POWER_BANK -> R.id.sourceTypePowerBank
        EnergySourceType.OTHER -> R.id.sourceTypeOther
    }

    private fun EditText.cleanText(): String? = text.toString().trim().takeIf { it.isNotBlank() }
    private fun EditText.decimalValue(): Double? = text.toString().trim().replace(',', '.').toDoubleOrNull()
    private fun EditText.hasText(): Boolean = text.toString().isNotBlank()
    private fun Double?.asInput(): String = this?.toString()?.removeSuffix(".0").orEmpty()
    private fun invalid(view: EditText) { view.error = "Confira o valor informado" }
    private fun toast(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }

    private val nameWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = updateNamePreview()
        override fun afterTextChanged(s: Editable?) = Unit
    }

    companion object {
        const val EXTRA_INITIAL_SETUP = "initial_setup"
    }
}
