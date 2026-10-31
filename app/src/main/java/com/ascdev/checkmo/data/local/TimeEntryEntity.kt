package com.ascdev.checkmo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ascdev.checkmo.domain.model.Coordinates
import com.ascdev.checkmo.domain.model.EntryLocation
import com.ascdev.checkmo.domain.model.SyncStatus
import com.ascdev.checkmo.domain.model.TimeEntry
import com.ascdev.checkmo.domain.model.TimeEntryType
import java.time.Instant

@Entity(
    tableName = "time_entries",
    indices = [
        Index(value = ["user_id", "timestamp"]),
        Index(value = ["sync_status"]),
    ],
)
data class TimeEntryEntity(
    // Generated on the device so the same id can be reused as the Firestore document id.
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    val type: TimeEntryType,
    /** Epoch millis. */
    val timestamp: Long,
    @ColumnInfo(name = "sync_status") val syncStatus: SyncStatus,
    // Added in schema v2; all three are null or all three are set.
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ColumnInfo(name = "accuracy_meters") val accuracyMeters: Float? = null,
)

fun TimeEntryEntity.toDomain() = TimeEntry(
    id = id,
    userId = userId,
    companyId = companyId,
    type = type,
    timestamp = Instant.ofEpochMilli(timestamp),
    syncStatus = syncStatus,
    location = if (latitude != null && longitude != null && accuracyMeters != null) {
        EntryLocation(Coordinates(latitude, longitude), accuracyMeters)
    } else {
        null
    },
)

fun TimeEntry.toEntity() = TimeEntryEntity(
    id = id,
    userId = userId,
    companyId = companyId,
    type = type,
    timestamp = timestamp.toEpochMilli(),
    syncStatus = syncStatus,
    latitude = location?.coordinates?.latitude,
    longitude = location?.coordinates?.longitude,
    accuracyMeters = location?.accuracyMeters,
)
