package com.ascdev.checkmo.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.ascdev.checkmo.domain.location.LocationProvider
import com.ascdev.checkmo.domain.model.Coordinates
import com.ascdev.checkmo.domain.model.DeviceLocation
import com.ascdev.checkmo.domain.model.LocationError
import com.ascdev.checkmo.domain.model.LocationException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class FusedLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: FusedLocationProviderClient,
) : LocationProvider {

    override suspend fun currentLocation(): DeviceLocation {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) throw LocationException(LocationError.PERMISSION_DENIED)

        val cancellation = CancellationTokenSource()
        val location = try {
            withTimeoutOrNull(TIMEOUT_MILLIS) {
                // A fresh fix, never a cached one: an old location could be from outside the workplace.
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token).await()
            }
        } catch (_: SecurityException) {
            throw LocationException(LocationError.PERMISSION_DENIED)
        } finally {
            cancellation.cancel()
        }
        return location?.toDeviceLocation() ?: throw LocationException(LocationError.UNAVAILABLE)
    }

    private fun Location.toDeviceLocation() = DeviceLocation(
        coordinates = Coordinates(latitude, longitude),
        accuracyMeters = accuracy,
        isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isMock else @Suppress("DEPRECATION") isFromMockProvider,
    )

    private companion object {
        const val TIMEOUT_MILLIS = 15_000L
    }
}
