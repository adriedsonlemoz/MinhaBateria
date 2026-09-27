package com.minhabateria.app.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.history.HistoryComparisonFormatter
import com.minhabateria.app.history.HistoryEntry

class HistoryComparisonRenderer(private val activity: Activity) {
    private val inflater = LayoutInflater.from(activity)
    private val quickRows = activity.findViewById<LinearLayout>(R.id.comparisonQuickRows)
    private val technicalRows = activity.findViewById<LinearLayout>(R.id.comparisonTechnicalRows)

    fun render(first: HistoryEntry, second: HistoryEntry) {
        renderSession(first, isFirst = true)
        renderSession(second, isFirst = false)
        renderSummary(first, second)
        renderQuickRows(HistoryComparisonFormatter.quickRows(first, second))
        renderTechnicalRows(HistoryComparisonFormatter.technicalRows(first, second))
        bindExpandableSections()
    }

    private fun renderSession(entry: HistoryEntry, isFirst: Boolean) {
        val titleId = if (isFirst) R.id.comparisonATitle else R.id.comparisonBTitle
        val dateId = if (isFirst) R.id.comparisonADate else R.id.comparisonBDate
        val sourceId = if (isFirst) R.id.comparisonASource else R.id.comparisonBSource
        val iconId = if (isFirst) R.id.comparisonAIcon else R.id.comparisonBIcon

        text(titleId).text = HistoryComparisonFormatter.sessionTitle(entry)
        text(dateId).text = HistoryComparisonFormatter.sessionDate(entry)
        text(sourceId).text = HistoryComparisonFormatter.sessionSource(entry)
        activity.findViewById<ImageView>(iconId).setImageResource(sourceIcon(entry.profileType))
    }

    private fun renderSummary(first: HistoryEntry, second: HistoryEntry) {
        val summary = HistoryComparisonFormatter.summary(first, second)
        text(R.id.comparisonSummaryTitle).text = summary.title
        text(R.id.comparisonSummaryBadge).text = summary.badge
        text(R.id.comparisonSummaryChargeValue).text = summary.chargeValue
        text(R.id.comparisonSummaryChargeCaption).text = summary.chargeCaption
        text(R.id.comparisonSummaryEnergyValue).text = summary.energyValue
        text(R.id.comparisonSummaryEnergyCaption).text = summary.energyCaption
        text(R.id.comparisonSummaryDuration).text = summary.durationText
        text(R.id.comparisonSummaryPower).text = summary.powerText
        text(R.id.comparisonSummaryContext).text = summary.contextText
        text(R.id.comparisonMeaningBody).text = summary.explanation
    }

    private fun renderQuickRows(rows: List<HistoryComparisonFormatter.Row>) {
        quickRows.removeAllViews()
        rows.chunked(2).forEach { pair ->
            val line = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            }
            pair.forEachIndexed { index, row ->
                val card = inflater.inflate(R.layout.history_comparison_metric, line, false)
                card.layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    if (index == 0) marginEnd = dp(4) else marginStart = dp(4)
                }
                bindQuickRow(card, row)
                line.addView(card)
            }
            quickRows.addView(line)
        }
    }

    private fun bindQuickRow(view: View, row: HistoryComparisonFormatter.Row) {
        view.findViewById<TextView>(R.id.comparisonMetricLabel).text = row.label
        view.findViewById<TextView>(R.id.comparisonMetricA).text = row.first
        view.findViewById<TextView>(R.id.comparisonMetricB).text = row.second
        view.findViewById<TextView>(R.id.comparisonMetricDifference).text = row.difference
        view.findViewById<ProgressBar>(R.id.comparisonMetricBarA).progress = row.firstRatio
        view.findViewById<ProgressBar>(R.id.comparisonMetricBarB).progress = row.secondRatio
    }

    private fun renderTechnicalRows(rows: List<HistoryComparisonFormatter.Row>) {
        technicalRows.removeAllViews()
        rows.forEach { row ->
            val view = inflater.inflate(R.layout.history_comparison_row, technicalRows, false)
            view.findViewById<TextView>(R.id.comparisonRowLabel).text = row.label
            view.findViewById<TextView>(R.id.comparisonRowA).text = row.first
            view.findViewById<TextView>(R.id.comparisonRowB).text = row.second
            view.findViewById<TextView>(R.id.comparisonRowDifference).text = row.difference
            technicalRows.addView(view)
        }
    }

    private fun bindExpandableSections() {
        val meaningCard = activity.findViewById<View>(R.id.comparisonMeaningCard)
        val meaningBody = activity.findViewById<View>(R.id.comparisonMeaningBody)
        val meaningArrow = text(R.id.comparisonMeaningArrow)
        meaningCard.setOnClickListener {
            val show = meaningBody.visibility != View.VISIBLE
            meaningBody.visibility = if (show) View.VISIBLE else View.GONE
            meaningArrow.text = if (show) "⌃" else "⌄"
        }

        val technicalToggle = activity.findViewById<View>(R.id.comparisonTechnicalToggle)
        val technicalArrow = text(R.id.comparisonTechnicalArrow)
        technicalToggle.setOnClickListener {
            val show = technicalRows.visibility != View.VISIBLE
            technicalRows.visibility = if (show) View.VISIBLE else View.GONE
            technicalArrow.text = if (show) "⌄" else "›"
        }
    }

    private fun sourceIcon(profileType: String?): Int = when {
        profileType?.contains("solar", ignoreCase = true) == true -> R.drawable.ic_source_solar
        profileType?.contains("power bank", ignoreCase = true) == true -> R.drawable.ic_source_battery
        profileType?.contains("carregador", ignoreCase = true) == true -> R.drawable.ic_session_plug
        else -> R.drawable.ic_status_charging
    }

    private fun text(id: Int): TextView = activity.findViewById(id)

    private fun dp(value: Int): Int = (value * activity.resources.displayMetrics.density).toInt()
}
