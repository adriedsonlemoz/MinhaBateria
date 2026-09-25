package com.minhabateria.app

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.minhabateria.app.history.HistoryStore
import com.minhabateria.app.ui.HistoryComparisonRenderer
import com.minhabateria.app.ui.SystemBars

class HistoryComparisonActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_comparison)
        SystemBars.apply(this, findViewById(R.id.historyComparisonRoot))
        findViewById<android.view.View>(R.id.comparisonBack).setOnClickListener { finish() }

        val firstId = intent.getStringExtra(EXTRA_FIRST_ID)
        val secondId = intent.getStringExtra(EXTRA_SECOND_ID)
        val entries = HistoryStore(this).entries()
        val first = entries.firstOrNull { it.id == firstId }
        val second = entries.firstOrNull { it.id == secondId }

        if (first == null || second == null || first.id == second.id) {
            Toast.makeText(this, "Não foi possível carregar as duas sessões.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        HistoryComparisonRenderer(this).render(first, second)
    }

    companion object {
        const val EXTRA_FIRST_ID = "history_first_id"
        const val EXTRA_SECOND_ID = "history_second_id"
    }
}
