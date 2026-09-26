package com.minhabateria.app.ui

import android.app.Activity
import android.graphics.PorterDuff
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.minhabateria.app.R

enum class BottomTab { NOW, CHARTS, SESSION, DISCHARGE, HISTORY }

class BottomTabsBinder(private val activity: Activity) {
    fun bind(
        active: BottomTab,
        openNow: () -> Unit,
        openCharts: () -> Unit,
        openSession: () -> Unit,
        openDischarge: () -> Unit,
        openHistory: () -> Unit
    ) {
        val tabs = listOf(
            TabViews(R.id.tabNow, R.id.tabNowIcon, R.id.tabNowLabel, BottomTab.NOW),
            TabViews(R.id.tabCharts, R.id.tabChartsIcon, R.id.tabChartsLabel, BottomTab.CHARTS),
            TabViews(R.id.tabSession, R.id.tabSessionIcon, R.id.tabSessionLabel, BottomTab.SESSION),
            TabViews(R.id.tabDischarge, R.id.tabDischargeIcon, R.id.tabDischargeLabel, BottomTab.DISCHARGE),
            TabViews(R.id.tabHistory, R.id.tabHistoryIcon, R.id.tabHistoryLabel, BottomTab.HISTORY)
        )
        tabs.forEach { style(it, it.tab == active) }
        bindClick(R.id.tabNow, active, BottomTab.NOW, openNow)
        bindClick(R.id.tabCharts, active, BottomTab.CHARTS, openCharts)
        bindClick(R.id.tabSession, active, BottomTab.SESSION, openSession)
        bindClick(R.id.tabDischarge, active, BottomTab.DISCHARGE, openDischarge)
        bindClick(R.id.tabHistory, active, BottomTab.HISTORY, openHistory)
    }

    private fun bindClick(containerId: Int, active: BottomTab, tab: BottomTab, action: () -> Unit) {
        activity.findViewById<LinearLayout>(containerId).setOnClickListener {
            if (active != tab) action()
        }
    }

    private fun style(views: TabViews, active: Boolean) {
        val container = activity.findViewById<LinearLayout>(views.containerId)
        val icon = activity.findViewById<ImageView>(views.iconId)
        val label = activity.findViewById<TextView>(views.labelId)
        val color = activity.getColor(if (active) R.color.accent_green else R.color.text_tab_inactive)
        container.background = if (active) activity.getDrawable(R.drawable.bg_tab_active) else null
        container.isClickable = true
        container.isFocusable = true
        container.alpha = 1f
        icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        label.setTextColor(color)
        label.setTypeface(null, if (active) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
    }

    private data class TabViews(
        val containerId: Int,
        val iconId: Int,
        val labelId: Int,
        val tab: BottomTab
    )
}
