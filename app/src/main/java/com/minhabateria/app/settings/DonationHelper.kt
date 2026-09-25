package com.minhabateria.app.settings

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Button
import android.widget.Toast
import com.minhabateria.app.R

object DonationHelper {
    private const val PIX_KEY = "adriedson@outlook.com"

    fun bind(activity: Activity) {
        activity.findViewById<Button>(R.id.donationButton).setOnClickListener {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Chave Pix", PIX_KEY))
            Toast.makeText(activity, "Chave Pix copiada", Toast.LENGTH_SHORT).show()
        }
    }
}
