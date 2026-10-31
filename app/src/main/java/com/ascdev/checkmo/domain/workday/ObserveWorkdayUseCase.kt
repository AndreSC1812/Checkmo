package com.ascdev.checkmo.domain.workday

import com.ascdev.checkmo.domain.model.TimeEntry
import com.ascdev.checkmo.domain.model.WorkStatus
import com.ascdev.checkmo.domain.model.Workday
import com.ascdev.checkmo.domain.repository.AuthRepository
import com.ascdev.checkmo.domain.repository.TimeEntryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * Today's workday for the signed-in user, recalculated on every [ticks] emission so running
 * timers stay current. When a tick crosses midnight the data source switches to the new day.
 */
class ObserveWorkdayUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val timeEntryRepository: TimeEntryRepository,
    private val clock: Clock,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(ticks: Flow<Instant>): Flow<Workday?> =
        authRepository.session
            .map { it?.id }
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                if (userId == null) return@flatMapLatest flowOf(null)

                val dayData = ticks
                    .map { it.atZone(clock.zone).toLocalDate() }
                    .distinctUntilChanged()
                    .flatMapLatest { date -> observeDay(userId, date) }

                combine(dayData, ticks) { day, now ->
                    WorkdayCalculator.calculate(
                        date = day.date,
                        zone = clock.zone,
                        entries = day.entries,
                        statusAtStartOfDay = day.statusAtStartOfDay,
                        now = now,
                    )
                }
            }

    private fun observeDay(userId: String, date: LocalDate): Flow<DayData> {
        val start = date.atStartOfDay(clock.zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(clock.zone).toInstant()
        return combine(
            timeEntryRepository.observeEntries(userId, start, end),
            timeEntryRepository.observeLatestBefore(userId, start),
        ) { entries, latestBefore ->
            DayData(date, entries, WorkStatus.fromLatest(latestBefore))
        }
    }

    private data class DayData(
        val date: LocalDate,
        val entries: List<TimeEntry>,
        val statusAtStartOfDay: WorkStatus,
    )
}
