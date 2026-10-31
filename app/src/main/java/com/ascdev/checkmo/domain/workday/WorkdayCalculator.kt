package com.ascdev.checkmo.domain.workday

import com.ascdev.checkmo.domain.model.TimeEntry
import com.ascdev.checkmo.domain.model.WorkStatus
import com.ascdev.checkmo.domain.model.Workday
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Turns the raw entries of a day into worked and break time.
 *
 * Shifts may cross midnight, so the status the user had when the day started is passed in
 * explicitly. Time is only counted up to [now] (or the end of the day for past days).
 * Entries that are not valid for the current status are ignored instead of corrupting totals.
 */
object WorkdayCalculator {

    fun calculate(
        date: LocalDate,
        zone: ZoneId,
        entries: List<TimeEntry>,
        statusAtStartOfDay: WorkStatus,
        now: Instant,
    ): Workday {
        val dayStart = date.atStartOfDay(zone).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant()
        val countUntil = minOf(now, dayEnd)

        var status = statusAtStartOfDay
        var segmentStart = dayStart
        var statusSince: Instant? = if (status == WorkStatus.OFF_DUTY) null else dayStart
        var worked = Duration.ZERO
        var onBreak = Duration.ZERO

        fun closeSegment(end: Instant) {
            val length = Duration.between(segmentStart, end).coerceAtLeast(Duration.ZERO)
            when (status) {
                WorkStatus.WORKING -> worked += length
                WorkStatus.ON_BREAK -> onBreak += length
                WorkStatus.OFF_DUTY -> Unit
            }
        }

        val sorted = entries
            .filter { it.timestamp >= dayStart && it.timestamp < dayEnd && it.timestamp <= countUntil }
            .sortedBy { it.timestamp }

        for (entry in sorted) {
            if (!status.canRegister(entry.type)) continue
            closeSegment(entry.timestamp)
            status = WorkStatus.after(entry.type)
            segmentStart = entry.timestamp
            statusSince = if (status == WorkStatus.OFF_DUTY) null else entry.timestamp
        }
        closeSegment(countUntil)

        return Workday(
            date = date,
            status = status,
            statusSince = statusSince,
            worked = worked,
            onBreak = onBreak,
            entries = sorted,
        )
    }

    private fun Duration.coerceAtLeast(min: Duration): Duration = if (this < min) min else this
}
