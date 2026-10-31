package com.ascdev.checkmo.testing

import com.ascdev.checkmo.domain.model.SyncStatus
import com.ascdev.checkmo.domain.model.TimeEntry
import com.ascdev.checkmo.domain.model.TimeEntryType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

val TestZone: ZoneId = ZoneOffset.UTC
val TestDate: LocalDate = LocalDate.of(2026, 10, 30)

fun at(time: String, date: LocalDate = TestDate) =
    date.atTime(LocalTime.parse(time)).atZone(TestZone).toInstant()

fun entry(type: TimeEntryType, time: String, date: LocalDate = TestDate, userId: String = "uid") = TimeEntry(
    id = "$type-$date-$time",
    userId = userId,
    companyId = "c1",
    type = type,
    timestamp = at(time, date),
    syncStatus = SyncStatus.PENDING,
)
