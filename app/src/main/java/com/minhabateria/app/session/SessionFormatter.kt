package com.minhabateria.app.session

import java.util.Locale

object SessionFormatter {
    fun energy(wh: Double?): String = wh?.let {
        val pattern = if (it < 0.1) "%.3f Wh" else "%.2f Wh"
        String.format(Locale.getDefault(), pattern, it)
    } ?: "Indisponível"

    fun charge(mah: Double?): String = mah?.let {
        String.format(Locale.getDefault(), "%.0f mAh", it)
    } ?: "Indisponível"
}
