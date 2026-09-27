package com.minhabateria.app.source

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.minhabateria.app.R

class SourceProfileSuggestionBinder(
    private val activity: Activity,
    private val brandInput: EditText,
    private val modelInput: EditText,
    private val powerInput: EditText,
    private val outputsInput: EditText,
    private val technologyInput: EditText,
    private val portInput: EditText,
    private val cableInput: EditText,
    private val capacityInput: EditText,
    private val voltageInput: EditText,
    private val currentInput: EditText,
    private val controllerInput: EditText,
    private val onChanged: () -> Unit
) {
    private val brandButton = button(R.id.suggestBrandButton)
    private val modelButton = button(R.id.suggestModelButton)
    private val powerButton = button(R.id.suggestPowerButton)
    private val technologyButton = button(R.id.suggestTechnologyButton)
    private val portButton = button(R.id.suggestPortButton)
    private val outputsButton = button(R.id.suggestOutputsButton)
    private val capacityButton = button(R.id.suggestCapacityButton)
    private val voltageButton = button(R.id.suggestVoltageButton)
    private val currentButton = button(R.id.suggestCurrentButton)
    private val controllerButton = button(R.id.suggestControllerButton)
    private val cableButton = button(R.id.suggestCableButton)
    private val techPortRow = activity.findViewById<View>(R.id.suggestTechPortRow)
    private val outputsRow = activity.findViewById<View>(R.id.suggestOutputsRow)
    private val capacityRow = activity.findViewById<View>(R.id.suggestCapacityRow)
    private val solarRow = activity.findViewById<View>(R.id.suggestSolarRow)
    private val controllerRow = activity.findViewById<View>(R.id.suggestControllerRow)
    private val cableRow = activity.findViewById<View>(R.id.suggestCableRow)
    private var currentType: EnergySourceType? = null

    init {
        brandButton.setOnClickListener { chooseBrand() }
        modelButton.setOnClickListener { chooseModel() }
        powerButton.setOnClickListener { choosePower() }
        technologyButton.setOnClickListener { chooseTechnology() }
        portButton.setOnClickListener { choosePort() }
        outputsButton.setOnClickListener { chooseOutputs() }
        capacityButton.setOnClickListener { chooseCapacity() }
        voltageButton.setOnClickListener { chooseVoltage() }
        currentButton.setOnClickListener { chooseCurrent() }
        controllerButton.setOnClickListener { chooseController() }
        cableButton.setOnClickListener { chooseCable() }
    }

    fun updateForType(type: EnergySourceType?) {
        currentType = type
        val chargerLike = type == EnergySourceType.CHARGER || type == EnergySourceType.POWER_BANK
        techPortRow.visibility = visible(chargerLike)
        outputsRow.visibility = visible(chargerLike)
        capacityRow.visibility = visible(type == EnergySourceType.POWER_BANK)
        solarRow.visibility = visible(type == EnergySourceType.SOLAR_PANEL)
        controllerRow.visibility = visible(type == EnergySourceType.SOLAR_PANEL)
        cableRow.visibility = visible(type == EnergySourceType.CHARGER)
        modelButton.text = when (type) {
            EnergySourceType.SOLAR_PANEL -> "Modelo / preset do painel"
            EnergySourceType.POWER_BANK -> "Modelo / capacidade"
            EnergySourceType.CHARGER -> "Modelo / preset"
            EnergySourceType.OTHER -> "Preset genérico"
            null -> "Modelo / preset"
        }
        syncLabels()
    }

    fun syncLabels() {
        brandButton.text = valueLabel("Marca", brandInput.text.toString(), "Escolher marca")
        modelButton.text = valueLabel(
            "Modelo",
            modelInput.text.toString(),
            when (currentType) {
                EnergySourceType.SOLAR_PANEL -> "Modelo / preset do painel"
                EnergySourceType.POWER_BANK -> "Modelo / capacidade"
                EnergySourceType.OTHER -> "Preset genérico"
                else -> "Modelo / preset"
            }
        )
        powerButton.text = powerInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { "Potência: $it W" }
            ?: "Escolher potência"
        technologyButton.text = valueLabel("Protocolo", technologyInput.text.toString(), "Escolher protocolo")
        portButton.text = valueLabel("Porta", portInput.text.toString(), "Escolher porta")
        outputsButton.text = outputsInput.text.toString().trim().takeIf { it.isNotEmpty() }
            ?.let { "Saída: ${it.lineSequence().first()}" } ?: "Escolher saída comum"
        capacityButton.text = capacityInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { "Capacidade: $it mAh" }
            ?: "Escolher capacidade"
        voltageButton.text = voltageInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { "Tensão: $it V" }
            ?: "Escolher tensão"
        currentButton.text = currentInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { "Corrente: $it A" }
            ?: "Escolher corrente"
        controllerButton.text = valueLabel("Controlador", controllerInput.text.toString(), "Escolher controlador")
        cableButton.text = valueLabel("Cabo", cableInput.text.toString(), "Escolher cabo")
    }

    private fun chooseBrand() {
        val type = currentType ?: return toast("Selecione primeiro o tipo da fonte.")
        choose("Escolha a marca", SourcePresetCatalog.brands(type)) { value ->
            when (value) {
                "Outra" -> brandInput.requestFocus()
                "Sem marca / genérico" -> brandInput.setText("")
                else -> brandInput.setText(value)
            }
            if (value != "Outra") modelInput.setText("")
            changed()
        }
    }

    private fun chooseModel() {
        val type = currentType ?: return toast("Selecione primeiro o tipo da fonte.")
        val presets = SourcePresetCatalog.models(type, brandInput.text.toString().trim())
        val labels = presets.map { it.label } + "Outro / digitar"
        choose("Escolha um modelo ou preset", labels) { label ->
            if (label == "Outro / digitar") {
                modelInput.requestFocus()
                toast("Digite apenas se a etiqueta for diferente das opções.")
            } else {
                presets.firstOrNull { it.label == label }?.let(::applyPreset)
            }
        }
    }

    private fun choosePower() {
        val type = currentType ?: return toast("Selecione primeiro o tipo da fonte.")
        val values = SourcePresetCatalog.powers(type)
        chooseNumeric("Escolha a potência", values, "W") { powerInput.setText(formatNumber(it)) }
    }

    private fun chooseTechnology() {
        val type = currentType ?: return
        chooseText("Escolha o protocolo", SourcePresetCatalog.technologies(type), technologyInput)
    }

    private fun choosePort() {
        val type = currentType ?: return
        chooseText("Escolha a porta", SourcePresetCatalog.ports(type), portInput)
    }

    private fun chooseOutputs() {
        val type = currentType ?: return
        chooseText("Escolha uma saída da etiqueta", SourcePresetCatalog.outputs(type), outputsInput)
    }

    private fun chooseCapacity() {
        val values = SourcePresetCatalog.capacities()
        choose("Escolha a capacidade", values.map { formatCapacity(it) } + "Outro valor") { label ->
            if (label == "Outro valor") {
                capacityInput.requestFocus()
            } else {
                val index = values.map { formatCapacity(it) }.indexOf(label)
                if (index in values.indices) capacityInput.setText(values[index].toString())
                changed()
            }
        }
    }

    private fun chooseVoltage() = chooseNumeric(
        "Escolha a tensão nominal",
        SourcePresetCatalog.voltages(),
        "V"
    ) { voltageInput.setText(formatNumber(it)) }

    private fun chooseCurrent() = chooseNumeric(
        "Escolha a corrente nominal",
        SourcePresetCatalog.currents(),
        "A"
    ) { currentInput.setText(formatNumber(it)) }

    private fun chooseController() = chooseText(
        "Escolha o controlador/conversor",
        SourcePresetCatalog.controllers(),
        controllerInput
    )

    private fun chooseCable() = chooseText(
        "Escolha o cabo",
        SourcePresetCatalog.cables(),
        cableInput
    )

    private fun chooseText(title: String, values: List<String>, target: EditText) {
        choose(title, values) { value ->
            if (value == "Outra" || value == "Outro") {
                target.requestFocus()
                toast("Digite apenas se nenhuma opção corresponder à etiqueta.")
            } else {
                target.setText(value)
                changed()
            }
        }
    }

    private fun chooseNumeric(title: String, values: List<Double>, unit: String, apply: (Double) -> Unit) {
        val labels = values.map { "${formatNumber(it)} $unit" } + "Outro valor"
        choose(title, labels) { label ->
            if (label == "Outro valor") {
                when (unit) {
                    "W" -> powerInput.requestFocus()
                    "V" -> voltageInput.requestFocus()
                    else -> currentInput.requestFocus()
                }
            } else {
                val index = labels.indexOf(label)
                if (index in values.indices) apply(values[index])
                changed()
            }
        }
    }

    private fun applyPreset(preset: SourcePreset) {
        preset.brand?.let(brandInput::setText)
        preset.model?.let(modelInput::setText)
        preset.powerW?.let { powerInput.setText(formatNumber(it)) }
        preset.technology?.let(technologyInput::setText)
        preset.portType?.let(portInput::setText)
        preset.outputs?.let(outputsInput::setText)
        preset.capacityMah?.let { capacityInput.setText(it.toString()) }
        preset.ratedVoltageV?.let { voltageInput.setText(formatNumber(it)) }
        preset.ratedCurrentA?.let { currentInput.setText(formatNumber(it)) }
        preset.controllerInfo?.let(controllerInput::setText)
        preset.cableInfo?.let(cableInput::setText)
        if (preset.model == null) modelButton.text = "Preset: ${preset.label}"
        changed()
    }

    private fun choose(title: String, values: List<String>, onSelected: (String) -> Unit) {
        if (values.isEmpty()) return toast("Não há sugestões para este campo.")
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setItems(values.toTypedArray()) { _, which -> onSelected(values[which]) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun changed() {
        syncLabels()
        onChanged()
    }

    private fun valueLabel(prefix: String, value: String, empty: String): String =
        value.trim().takeIf { it.isNotEmpty() }?.let { "$prefix: $it" } ?: empty

    private fun button(id: Int): Button = activity.findViewById(id)
    private fun visible(condition: Boolean): Int = if (condition) View.VISIBLE else View.GONE
    private fun toast(message: String) = Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    private fun formatNumber(value: Double): String = value.toString().removeSuffix(".0").replace('.', ',')
    private fun formatCapacity(value: Int): String = "${value / 1_000}.${(value % 1_000).toString().padStart(3, '0')} mAh"
}
