package com.ascdev.checkmo.core.ui

import java.text.NumberFormat
import java.util.Locale

object DistanceFormat {

    private val AppLocale: Locale = Locale.forLanguageTag("es-ES")

    /** `350 m` below one kilometer, `1,3 km` above. */
    fun meters(meters: Int): String {
        if (meters < 1_000) return "$meters m"
        val format = NumberFormat.getNumberInstance(AppLocale).apply { maximumFractionDigits = 1 }
        return "${format.format(meters / 1_000.0)} km"
    }
}
