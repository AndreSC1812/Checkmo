package com.ascdev.checkmo.domain.location

import com.ascdev.checkmo.domain.model.EntryLocation
import com.ascdev.checkmo.domain.model.LocationException
import com.ascdev.checkmo.domain.model.WorkplaceCheck
import com.ascdev.checkmo.domain.repository.AuthRepository
import com.ascdev.checkmo.domain.repository.WorkplaceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Checks whether the employee is inside the company's workplace radius right now.
 *
 * GPS fixes are never exact, so the reported accuracy (capped) is added as tolerance; otherwise
 * someone standing at the edge of the radius could be rejected because of a noisy fix.
 */
class CheckWorkplaceUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val workplaceRepository: WorkplaceRepository,
    private val locationProvider: LocationProvider,
) {

    suspend operator fun invoke(): WorkplaceCheck {
        val companyId = authRepository.session.first()?.company?.id ?: return WorkplaceCheck.NotConfigured
        // Firestore answers from its cache; the timeout covers a first launch offline with no cache.
        val workplace = withTimeoutOrNull(WORKPLACE_TIMEOUT_MILLIS) {
            workplaceRepository.observeWorkplace(companyId).first()
        } ?: return WorkplaceCheck.NotConfigured

        val location = try {
            locationProvider.currentLocation()
        } catch (e: LocationException) {
            return WorkplaceCheck.Failed(e.error)
        }
        if (location.isMock) return WorkplaceCheck.MockLocation

        val distance = GeoDistance.meters(location.coordinates, workplace.center)
        val tolerance = min(location.accuracyMeters.toDouble(), MAX_ACCURACY_TOLERANCE_METERS)
        return if (distance <= workplace.radiusMeters + tolerance) {
            WorkplaceCheck.Inside(EntryLocation(location.coordinates, location.accuracyMeters))
        } else {
            WorkplaceCheck.Outside(distanceMeters = distance.roundToInt())
        }
    }

    companion object {
        const val MAX_ACCURACY_TOLERANCE_METERS = 50.0
        private const val WORKPLACE_TIMEOUT_MILLIS = 5_000L
    }
}
