package com.minhabateria.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import com.minhabateria.app.history.HistoryEntry
import com.minhabateria.app.history.HistoryFormatter
import com.minhabateria.app.history.HistoryStore
import com.minhabateria.app.ui.HistoryComparisonRenderer
import com.minhabateria.app.ui.SystemBars

class HistoryComparisonActivity : Activity() {
    private lateinit var store: HistoryStore
    private lateinit var first: HistoryEntry
    private lateinit var second: HistoryEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_comparison)
        SystemBars.apply(this, findViewById(R.id.historyComparisonRoot))
        findViewById<android.view.View>(R.id.comparisonBack).setOnClickListener { finish() }
        store = HistoryStore(this)

        val firstId = intent.getStringExtra(EXTRA_FIRST_ID)
        val secondId = intent.getStringExtra(EXTRA_SECOND_ID)
        val entries = store.entries()
        val loadedFirst = entries.firstOrNull { it.id == firstId }
        val loadedSecond = entries.firstOrNull { it.id == secondId }

        if (loadedFirst == null || loadedSecond == null || loadedFirst.id == loadedSecond.id) {
            Toast.makeText(this, "Não foi possível carregar as duas sessões.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        first = loadedFirst
        second = loadedSecond

        HistoryComparisonRenderer(this).render(first, second)
        findViewById<android.view.View>(R.id.comparisonADelete).setOnClickListener { confirmDelete(first) }
        findViewById<android.view.View>(R.id.comparisonBDelete).setOnClickListener { confirmDelete(second) }
    }

    private fun confirmDelete(entry: HistoryEntry) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Excluir sessão?")
            .setMessage(
                "${HistoryFormatter.title(entry)}\n${HistoryFormatter.dateTime(entry.endedAtMs)}\n\n" +
                    "Esta sessão e os dados registrados nela serão apagados permanentemente."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                if (store.delete(entry.id)) {
                    setResult(RESULT_OK)
                    Toast.makeText(this, "Sessão excluída. Selecione outra sessão para comparar.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Não foi possível excluir a sessão.", Toast.LENGTH_SHORT).show()
                }
            }
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.accent_red))
        }
        dialog.show()
    }

    companion object {
        const val EXTRA_FIRST_ID = "history_first_id"
        const val EXTRA_SECOND_ID = "history_second_id"
    }
}
