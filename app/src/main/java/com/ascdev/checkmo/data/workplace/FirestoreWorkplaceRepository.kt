package com.ascdev.checkmo.data.workplace

import android.util.Log
import com.ascdev.checkmo.data.remote.FirestoreSchema
import com.ascdev.checkmo.data.remote.FirestoreSchema.Companies
import com.ascdev.checkmo.domain.model.Coordinates
import com.ascdev.checkmo.domain.model.Workplace
import com.ascdev.checkmo.domain.repository.WorkplaceRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class FirestoreWorkplaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : WorkplaceRepository {

    // Firestore keeps the last snapshot in its offline cache, so this also works without network.
    override fun observeWorkplace(companyId: String): Flow<Workplace?> = callbackFlow {
        val registration = companyDocument(companyId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Workplace listener failed", error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toWorkplace())
        }
        awaitClose { registration.remove() }
    }.distinctUntilChanged()

    override suspend fun saveWorkplace(companyId: String, workplace: Workplace): Result<Unit> = try {
        companyDocument(companyId).update(
            Companies.WORKPLACE,
            mapOf(
                FirestoreSchema.Workplace.LATITUDE to workplace.center.latitude,
                FirestoreSchema.Workplace.LONGITUDE to workplace.center.longitude,
                FirestoreSchema.Workplace.RADIUS_METERS to workplace.radiusMeters,
            ),
        ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Saving workplace failed", e)
        Result.failure(e)
    }

    private fun companyDocument(companyId: String) =
        firestore.collection(Companies.COLLECTION).document(companyId)

    private fun DocumentSnapshot.toWorkplace(): Workplace? {
        val map = get(Companies.WORKPLACE) as? Map<*, *> ?: return null
        val latitude = (map[FirestoreSchema.Workplace.LATITUDE] as? Number)?.toDouble() ?: return null
        val longitude = (map[FirestoreSchema.Workplace.LONGITUDE] as? Number)?.toDouble() ?: return null
        val radius = (map[FirestoreSchema.Workplace.RADIUS_METERS] as? Number)?.toInt() ?: return null
        return Workplace(Coordinates(latitude, longitude), radius)
    }

    private companion object {
        const val TAG = "WorkplaceRepository"
    }
}
