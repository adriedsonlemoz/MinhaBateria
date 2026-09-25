package com.minhabateria.app.ui

import android.app.Activity
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.minhabateria.app.R

enum class BottomTab { NOW, SESSION }

class BottomTabsBinder(private val activity: Activity) {
    fun bind(active: BottomTab, openNow: () -> Unit, openSession: () -> Unit) {
        val tabs = listOf(
            TabViews(R.id.tabNow, R.id.tabNowIcon, R.id.tabNowLabel, BottomTab.NOW),
            TabViews(R.id.tabSession, R.id.tabSessionIcon, R.id.tabSessionLabel, BottomTab.SESSION)
        )
        tabs.forEach { style(it, it.tab == active) }

        activity.findViewById<LinearLayout>(R.id.tabNow).setOnClickListener {
            if (active != BottomTab.NOW) openNow()
        }
        activity.findViewById<LinearLayout>(R.id.tabSession).setOnClickListener {
            if (active != BottomTab.SESSION) openSession()
        }

        disable(R.id.tabCharts, R.id.tabChartsIcon, R.id.tabChartsLabel)
        disable(R.id.tabHistory, R.id.tabHistoryIcon, R.id.tabHistoryLabel)
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

    private fun disable(containerId: Int, iconId: Int, labelId: Int) {
        val container = activity.findViewById<LinearLayout>(containerId)
        container.background = null
        container.isClickable = false
        container.isFocusable = false
        container.alpha = 0.55f
        val color = activity.getColor(R.color.text_tab_inactive)
        activity.findViewById<ImageView>(iconId).setColorFilter(color, PorterDuff.Mode.SRC_IN)
        activity.findViewById<TextView>(labelId).setTextColor(color)
    }

    private data class TabViews(
        val containerId: Int,
        val iconId: Int,
        val labelId: Int,
        val tab: BottomTab
    )
}
