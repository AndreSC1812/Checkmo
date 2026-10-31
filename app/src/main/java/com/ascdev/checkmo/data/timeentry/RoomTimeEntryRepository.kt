package com.ascdev.checkmo.data.timeentry

import com.ascdev.checkmo.core.di.IoDispatcher
import com.ascdev.checkmo.data.local.TimeEntryDao
import com.ascdev.checkmo.data.local.toDomain
import com.ascdev.checkmo.data.local.toEntity
import com.ascdev.checkmo.domain.model.TimeEntry
import com.ascdev.checkmo.domain.repository.TimeEntryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomTimeEntryRepository @Inject constructor(
    private val dao: TimeEntryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TimeEntryRepository {

    override fun observeEntries(userId: String, from: Instant, to: Instant): Flow<List<TimeEntry>> =
        dao.observeBetween(userId, from.toEpochMilli(), to.toEpochMilli())
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeLatestBefore(userId: String, before: Instant): Flow<TimeEntry?> =
        dao.observeLatestBefore(userId, before.toEpochMilli()).map { it?.toDomain() }

    override suspend fun getLatest(userId: String): TimeEntry? = withContext(ioDispatcher) {
        dao.getLatest(userId)?.toDomain()
    }

    override suspend fun add(entry: TimeEntry) = withContext(ioDispatcher) {
        dao.insert(entry.toEntity())
    }
}
