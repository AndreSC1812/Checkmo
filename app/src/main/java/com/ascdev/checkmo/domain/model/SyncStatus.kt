package com.ascdev.checkmo.domain.model

/** Upload state of a locally stored entry. Entries are always saved locally first. */
enum class SyncStatus {
    PENDING,
    SYNCED,
    ERROR,
}
