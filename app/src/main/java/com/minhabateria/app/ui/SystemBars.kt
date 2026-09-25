package com.minhabateria.app.ui

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import com.minhabateria.app.R

object SystemBars {
    fun apply(activity: Activity, root: View) {
        activity.window.statusBarColor = activity.getColor(R.color.background)
        activity.window.navigationBarColor = activity.getColor(R.color.background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced = false
        }

        val initialLeft = root.paddingLeft
        val initialTop = root.paddingTop
        val initialRight = root.paddingRight
        val initialBottom = root.paddingBottom

        root.setOnApplyWindowInsetsListener { view, insets ->
            val topInset: Int
            val bottomInset: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                topInset = bars.top
                bottomInset = bars.bottom
            } else {
                @Suppress("DEPRECATION")
                topInset = insets.systemWindowInsetTop
                @Suppress("DEPRECATION")
                bottomInset = insets.systemWindowInsetBottom
            }

            view.setPadding(
                initialLeft,
                initialTop + topInset,
                initialRight,
                initialBottom + bottomInset
            )
            insets
        }
        root.requestApplyInsets()
    }
}
