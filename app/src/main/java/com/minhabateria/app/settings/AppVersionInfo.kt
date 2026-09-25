package com.minhabateria.app.settings

import android.content.Context
import android.os.Build

object AppVersionInfo {
    fun fullVersion(context: Context): String {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
        return "${info.versionName}+$code"
    }
}
