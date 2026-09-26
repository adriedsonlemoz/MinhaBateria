package com.minhabateria.app.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.minhabateria.app.R
import com.minhabateria.app.settings.AppVersionInfo

object UpdateNotesDialog {
    private const val PREFS_NAME = "update_notes"
    private const val KEY_LAST_SHOWN_VERSION = "last_shown_version"

    fun showIfNeeded(activity: Activity) {
        val version = AppVersionInfo.fullVersion(activity)
        val preferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (preferences.getString(KEY_LAST_SHOWN_VERSION, null) == version) return

        preferences.edit().putString(KEY_LAST_SHOWN_VERSION, version).apply()
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_update_notes)
        dialog.setCancelable(true)
        dialog.findViewById<TextView>(R.id.updateNotesVersion).text = "Versão $version"
        dialog.findViewById<Button>(R.id.updateNotesContinueButton).setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (activity.resources.displayMetrics.widthPixels * 0.92f).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }
}
