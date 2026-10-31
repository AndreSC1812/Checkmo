package com.ascdev.checkmo.testing

import com.ascdev.checkmo.domain.model.TimeEntry
import com.ascdev.checkmo.domain.repository.TimeEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant

class FakeTimeEntryRepository : TimeEntryRepository {

    val entries = MutableStateFlow<List<TimeEntry>>(emptyList())

    override fun observeEntries(userId: String, from: Instant, to: Instant): Flow<List<TimeEntry>> =
        entries.map { list ->
            list.filter { it.userId == userId && it.timestamp >= from && it.timestamp < to }
                .sortedBy { it.timestamp }
        }

    override fun observeLatestBefore(userId: String, before: Instant): Flow<TimeEntry?> =
        entries.map { list ->
            list.filter { it.userId == userId && it.timestamp < before }.maxByOrNull { it.timestamp }
        }

    override suspend fun getLatest(userId: String): TimeEntry? =
        entries.value.filter { it.userId == userId }.maxByOrNull { it.timestamp }

    override suspend fun add(entry: TimeEntry) {
        entries.value = entries.value + entry
    }
}
