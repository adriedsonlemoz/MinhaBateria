package com.minhabateria.app.source

import android.app.Activity
import android.os.Bundle
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
    private lateinit var nameInput: EditText
    private lateinit var powerInput: EditText
    private lateinit var powerHelp: TextView
    private var initialSetup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source_profile)
        SystemBars.apply(this, findViewById(R.id.sourceProfileRoot))

        store = SourceProfileStore(this)
        initialSetup = intent.getBooleanExtra(EXTRA_INITIAL_SETUP, false)
        typeGroup = findViewById(R.id.sourceTypeGroup)
        nameInput = findViewById(R.id.sourceNameInput)
        powerInput = findViewById(R.id.sourcePowerInput)
        powerHelp = findViewById(R.id.sourcePowerHelp)

        findViewById<ImageButton>(R.id.sourceProfileBackButton).setOnClickListener { cancel() }
        findViewById<Button>(R.id.cancelSourceProfileButton).apply {
            text = if (initialSetup) "Agora não" else "Cancelar"
            setOnClickListener { cancel() }
        }
        findViewById<Button>(R.id.saveSourceProfileButton).setOnClickListener { save() }

        typeGroup.setOnCheckedChangeListener { _, _ -> updatePowerHelp() }
        loadExistingProfile()
        updatePowerHelp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        cancel()
    }

    private fun loadExistingProfile() {
        val profile = store.getProfile() ?: return
        typeGroup.check(radioId(profile.type))
        nameInput.setText(profile.name)
        profile.nominalPowerW?.let {
            powerInput.setText(it.toString().removeSuffix(".0"))
        }
    }

    private fun save() {
        val type = selectedType() ?: run {
            Toast.makeText(this, "Selecione o tipo da fonte.", Toast.LENGTH_SHORT).show()
            return
        }
        val name = nameInput.text.toString().trim()
        if (name.isBlank()) {
            nameInput.error = "Informe um nome para o perfil"
            return
        }
        val power = powerInput.text.toString().trim().replace(',', '.').toDoubleOrNull()
        if (type == EnergySourceType.SOLAR_PANEL && (power == null || power <= 0.0)) {
            powerInput.error = "Informe a potência nominal do painel"
            return
        }
        if (power != null && power <= 0.0) {
            powerInput.error = "Use um valor maior que zero"
            return
        }
        store.save(EnergySourceProfile(type, name, power))
        Toast.makeText(this, "Perfil da fonte salvo.", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    private fun cancel() {
        if (initialSetup) store.dismissInitialSetup()
        finish()
    }

    private fun updatePowerHelp() {
        powerHelp.text = if (selectedType() == EnergySourceType.SOLAR_PANEL) {
            "Obrigatória para painel solar. Ex.: 8 W, 20 W ou 30 W."
        } else {
            "Opcional para carregador, power bank ou outra fonte."
        }
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

    companion object {
        const val EXTRA_INITIAL_SETUP = "initial_setup"
    }
}
