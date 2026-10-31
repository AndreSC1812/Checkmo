package com.ascdev.checkmo.domain.model

import java.time.Instant

data class TimeEntry(
    val id: String,
    val userId: String,
    val companyId: String,
    val type: TimeEntryType,
    val timestamp: Instant,
    val syncStatus: SyncStatus,
    /** `null` when the company has no workplace configured. */
    val location: EntryLocation? = null,
)
