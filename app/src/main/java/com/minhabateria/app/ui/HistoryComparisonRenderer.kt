package com.minhabateria.app.ui

import android.app.Activity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.history.HistoryComparisonFormatter
import com.minhabateria.app.history.HistoryEntry

class HistoryComparisonRenderer(private val activity: Activity) {
    private val inflater = LayoutInflater.from(activity)
    private val rows = activity.findViewById<LinearLayout>(R.id.comparisonRows)

    fun render(first: HistoryEntry, second: HistoryEntry) {
        activity.findViewById<TextView>(R.id.comparisonATitle).text =
            HistoryComparisonFormatter.sessionTitle(first)
        activity.findViewById<TextView>(R.id.comparisonASubtitle).text =
            HistoryComparisonFormatter.sessionSubtitle(first)
        activity.findViewById<TextView>(R.id.comparisonBTitle).text =
            HistoryComparisonFormatter.sessionTitle(second)
        activity.findViewById<TextView>(R.id.comparisonBSubtitle).text =
            HistoryComparisonFormatter.sessionSubtitle(second)

        rows.removeAllViews()
        HistoryComparisonFormatter.rows(first, second).forEach { row ->
            val view = inflater.inflate(R.layout.history_comparison_row, rows, false)
            view.findViewById<TextView>(R.id.comparisonRowLabel).text = row.label
            view.findViewById<TextView>(R.id.comparisonRowA).text = row.first
            view.findViewById<TextView>(R.id.comparisonRowB).text = row.second
            view.findViewById<TextView>(R.id.comparisonRowDifference).text = row.difference
            rows.addView(view)
        }
    }
}
