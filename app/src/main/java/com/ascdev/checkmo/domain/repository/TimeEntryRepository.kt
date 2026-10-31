package com.ascdev.checkmo.domain.repository

import com.ascdev.checkmo.domain.model.TimeEntry
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface TimeEntryRepository {

    /** Entries of [userId] with `from <= timestamp < to`, oldest first. */
    fun observeEntries(userId: String, from: Instant, to: Instant): Flow<List<TimeEntry>>

    /** Most recent entry of [userId] strictly before [before], or `null` if there is none. */
    fun observeLatestBefore(userId: String, before: Instant): Flow<TimeEntry?>

    suspend fun getLatest(userId: String): TimeEntry?

    /** Stores [entry] locally. It stays [com.ascdev.checkmo.domain.model.SyncStatus.PENDING] until uploaded. */
    suspend fun add(entry: TimeEntry)
}
