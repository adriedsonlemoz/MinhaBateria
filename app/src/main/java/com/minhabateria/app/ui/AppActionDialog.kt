package com.minhabateria.app.ui

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.minhabateria.app.R

object AppActionDialog {
    fun show(
        activity: Activity,
        title: String,
        message: String,
        details: String? = null,
        confirmText: String,
        destructive: Boolean = false,
        iconRes: Int = if (destructive) R.drawable.ic_delete else R.drawable.ic_session_info,
        onConfirm: () -> Unit
    ) {
        if (activity.isFinishing || activity.isDestroyed) return
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_app_action, null)
        dialog.setContentView(view)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.66f)
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        view.findViewById<ImageView>(R.id.appDialogIcon).apply {
            setImageResource(iconRes)
            if (destructive) setBackgroundResource(R.drawable.bg_icon_circle_red)
        }
        view.findViewById<TextView>(R.id.appDialogTitle).text = title
        view.findViewById<TextView>(R.id.appDialogMessage).text = message
        view.findViewById<TextView>(R.id.appDialogDetails).apply {
            if (details.isNullOrBlank()) visibility = View.GONE else text = details
        }
        view.findViewById<Button>(R.id.appDialogCancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.appDialogConfirm).apply {
            text = confirmText
            if (destructive) {
                setBackgroundResource(R.drawable.bg_danger_button)
                setTextColor(activity.getColor(R.color.accent_red))
            }
            setOnClickListener {
                dialog.dismiss()
                onConfirm()
            }
        }

        dialog.setOnShowListener {
            dialog.window?.setLayout(
                (activity.resources.displayMetrics.widthPixels * 0.90f).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            view.alpha = 0f
            view.translationY = 16f
            view.animate().alpha(1f).translationY(0f).setDuration(160L).start()
        }
        dialog.show()
    }
}
