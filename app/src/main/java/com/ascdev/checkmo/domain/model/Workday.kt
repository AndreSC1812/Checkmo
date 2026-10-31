package com.ascdev.checkmo.domain.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/** Summary of one calendar day for a user, computed from its time entries. */
data class Workday(
    val date: LocalDate,
    val status: WorkStatus,
    /** When the current status started, or `null` when off duty. */
    val statusSince: Instant?,
    val worked: Duration,
    val onBreak: Duration,
    val entries: List<TimeEntry>,
)
