package com.ascdev.checkmo.domain.model

enum class TimeEntryError {
    NOT_SIGNED_IN,
    INVALID_TRANSITION,
}

class TimeEntryException(val error: TimeEntryError) : Exception(error.name)
