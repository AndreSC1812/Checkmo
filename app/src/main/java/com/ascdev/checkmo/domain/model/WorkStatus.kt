package com.ascdev.checkmo.domain.model

enum class WorkStatus {
    OFF_DUTY,
    WORKING,
    ON_BREAK;

    /** Entry types that are valid from this status, in the order the UI should offer them. */
    val allowedEntries: List<TimeEntryType>
        get() = when (this) {
            OFF_DUTY -> listOf(TimeEntryType.CLOCK_IN)
            WORKING -> listOf(TimeEntryType.BREAK_START, TimeEntryType.CLOCK_OUT)
            ON_BREAK -> listOf(TimeEntryType.BREAK_END)
        }

    fun canRegister(type: TimeEntryType): Boolean = type in allowedEntries

    companion object {
        /** Status right after registering an entry of [type]. */
        fun after(type: TimeEntryType): WorkStatus = when (type) {
            TimeEntryType.CLOCK_IN, TimeEntryType.BREAK_END -> WORKING
            TimeEntryType.BREAK_START -> ON_BREAK
            TimeEntryType.CLOCK_OUT -> OFF_DUTY
        }

        /** Status implied by the most recent entry, or off duty when there is none. */
        fun fromLatest(entry: TimeEntry?): WorkStatus = entry?.let { after(it.type) } ?: OFF_DUTY
    }
}
