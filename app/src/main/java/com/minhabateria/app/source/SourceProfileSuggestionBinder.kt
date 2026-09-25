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
    private val capacityInput: EditText,
    private val onChanged: () -> Unit
) {
    private val brandButton = button(R.id.suggestBrandButton)
    private val modelButton = button(R.id.suggestModelButton)
    private val powerButton = button(R.id.suggestPowerButton)
    private val technologyButton = button(R.id.suggestTechnologyButton)
    private val portButton = button(R.id.suggestPortButton)
    private val outputsButton = button(R.id.suggestOutputsButton)
    private val techPortRow = activity.findViewById<View>(R.id.suggestTechPortRow)
    private var currentType: EnergySourceType? = null

    init {
        brandButton.setOnClickListener { chooseBrand() }
        modelButton.setOnClickListener { chooseModel() }
        powerButton.setOnClickListener { choosePower() }
        technologyButton.setOnClickListener { chooseTechnology() }
        portButton.setOnClickListener { choosePort() }
        outputsButton.setOnClickListener { chooseOutputs() }
    }

    fun updateForType(type: EnergySourceType?) {
        currentType = type
        val electrical = type == EnergySourceType.CHARGER || type == EnergySourceType.POWER_BANK
        techPortRow.visibility = if (electrical) View.VISIBLE else View.GONE
        outputsButton.visibility = if (electrical) View.VISIBLE else View.GONE
        modelButton.text = when (type) {
            EnergySourceType.SOLAR_PANEL -> "Modelo / preset do painel"
            EnergySourceType.POWER_BANK -> "Modelo / capacidade"
            else -> "Modelo / preset"
        }
        syncLabels()
    }

    fun syncLabels() {
        brandButton.text = brandInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { "Marca: $it" } ?: "Escolher marca"
        modelButton.text = modelInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { "Modelo: $it" }
            ?: when (currentType) {
                EnergySourceType.SOLAR_PANEL -> "Modelo / preset do painel"
                EnergySourceType.POWER_BANK -> "Modelo / capacidade"
                else -> "Modelo / preset"
            }
        powerButton.text = powerInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { "Potência: $it W" } ?: "Escolher potência"
        technologyButton.text = technologyInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { "Protocolo: $it" } ?: "Escolher protocolo"
        portButton.text = portInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { "Porta: $it" } ?: "Escolher porta"
        outputsButton.text = outputsInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { "Saída: ${it.lineSequence().first()}" } ?: "Escolher saída comum"
    }

    private fun chooseBrand() {
        val type = currentType ?: return toast("Selecione primeiro o tipo da fonte.")
        choose("Escolha a marca", SourcePresetCatalog.brands(type)) { value ->
            if (value == "Outra") {
                brandInput.requestFocus()
                toast("Digite a marca no campo abaixo.")
            } else if (value == "Sem marca / genérico") {
                brandInput.setText("")
            } else {
                brandInput.setText(value)
            }
            modelInput.setText("")
            syncLabels()
            onChanged()
        }
    }

    private fun chooseModel() {
        val type = currentType ?: return toast("Selecione primeiro o tipo da fonte.")
        val presets = SourcePresetCatalog.models(type, brandInput.text.toString().trim())
        if (presets.isEmpty()) {
            modelInput.requestFocus()
            return toast("Não há modelo pronto para esta marca. Digite somente se quiser.")
        }
        choose("Escolha um modelo ou preset", presets.map { it.label } + "Outro / digitar") { label ->
            if (label == "Outro / digitar") {
                modelInput.requestFocus()
                return@choose
            }
            presets.firstOrNull { it.label == label }?.let(::applyPreset)
        }
    }

    private fun choosePower() {
        val type = currentType ?: return toast("Selecione primeiro o tipo da fonte.")
        val values = SourcePresetCatalog.powers(type)
        val labels = values.map { formatNumber(it) + " W" } + "Outro valor"
        choose("Escolha a potência", labels) { label ->
            if (label == "Outro valor") {
                powerInput.requestFocus()
                return@choose
            }
            val index = labels.indexOf(label)
            if (index in values.indices) powerInput.setText(formatNumber(values[index]))
            syncLabels()
            onChanged()
        }
    }

    private fun chooseTechnology() {
        val type = currentType ?: return
        choose("Escolha o protocolo", SourcePresetCatalog.technologies(type)) { value ->
            if (value == "Outra") technologyInput.requestFocus() else technologyInput.setText(value)
            syncLabels()
        }
    }

    private fun choosePort() {
        val type = currentType ?: return
        choose("Escolha a porta", SourcePresetCatalog.ports(type)) { value ->
            if (value == "Outra") portInput.requestFocus() else portInput.setText(value)
            syncLabels()
        }
    }

    private fun chooseOutputs() {
        val type = currentType ?: return
        choose("Escolha uma saída da etiqueta", SourcePresetCatalog.outputs(type)) { value ->
            if (value == "Outra") outputsInput.requestFocus() else outputsInput.setText(value)
            syncLabels()
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
        syncLabels()
        if (preset.model == null) modelButton.text = "Preset: ${preset.label}"
        onChanged()
    }

    private fun choose(title: String, values: List<String>, onSelected: (String) -> Unit) {
        if (values.isEmpty()) return toast("Não há sugestões para este campo.")
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setItems(values.toTypedArray()) { _, which -> onSelected(values[which]) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun button(id: Int): Button = activity.findViewById(id)
    private fun toast(message: String) = Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    private fun formatNumber(value: Double): String = value.toString().removeSuffix(".0").replace('.', ',')
}
