package com.ascdev.checkmo.domain.workday

import com.ascdev.checkmo.domain.model.EntryLocation
import com.ascdev.checkmo.domain.model.SyncStatus
import com.ascdev.checkmo.domain.model.TimeEntry
import com.ascdev.checkmo.domain.model.TimeEntryError
import com.ascdev.checkmo.domain.model.TimeEntryException
import com.ascdev.checkmo.domain.model.TimeEntryType
import com.ascdev.checkmo.domain.model.WorkStatus
import com.ascdev.checkmo.domain.repository.AuthRepository
import com.ascdev.checkmo.domain.repository.TimeEntryRepository
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.util.UUID
import javax.inject.Inject

/**
 * Records a clock-in, break or clock-out for the signed-in user. The entry is stored locally as
 * pending, so it works offline; uploading it is handled separately.
 */
class RegisterTimeEntryUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val timeEntryRepository: TimeEntryRepository,
    private val clock: Clock,
) {

    suspend operator fun invoke(type: TimeEntryType, location: EntryLocation? = null): Result<TimeEntry> {
        val user = authRepository.session.first()
            ?: return Result.failure(TimeEntryException(TimeEntryError.NOT_SIGNED_IN))

        val status = WorkStatus.fromLatest(timeEntryRepository.getLatest(user.id))
        if (!status.canRegister(type)) {
            return Result.failure(TimeEntryException(TimeEntryError.INVALID_TRANSITION))
        }

        val entry = TimeEntry(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            companyId = user.company.id,
            type = type,
            timestamp = clock.instant(),
            syncStatus = SyncStatus.PENDING,
            location = location,
        )
        timeEntryRepository.add(entry)
        return Result.success(entry)
    }
}
