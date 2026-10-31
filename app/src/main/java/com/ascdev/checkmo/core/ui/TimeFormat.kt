package com.ascdev.checkmo.core.ui

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object TimeFormat {

    // The app is Spanish only, so dates must not follow a device set to another language.
    private val AppLocale: Locale = Locale.forLanguageTag("es-ES")

    /** `07:05:09`. Hours are not capped at 24 so weekly totals still read correctly. */
    fun clock(duration: Duration): String {
        val seconds = duration.seconds.coerceAtLeast(0)
        return "%02d:%02d:%02d".format(Locale.ROOT, seconds / 3600, seconds % 3600 / 60, seconds % 60)
    }

    /** `7 h 05 min`, or `45 min` under one hour. */
    fun hoursMinutes(duration: Duration): String {
        val minutes = duration.toMinutes().coerceAtLeast(0)
        val hours = minutes / 60
        return if (hours == 0L) "$minutes min" else "%d h %02d min".format(Locale.ROOT, hours, minutes % 60)
    }

    fun time(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        instant.atZone(zone).format(TimeFormatter)

    fun fullDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(AppLocale)).replaceFirstChar { it.titlecase() }

    private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
}
