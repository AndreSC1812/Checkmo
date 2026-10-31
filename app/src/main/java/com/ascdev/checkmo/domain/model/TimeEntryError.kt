package com.ascdev.checkmo.domain.model

enum class TimeEntryError {
    NOT_SIGNED_IN,
    INVALID_TRANSITION,
    UNKNOWN,
}

class TimeEntryException(val error: TimeEntryError) : Exception(error.name)
