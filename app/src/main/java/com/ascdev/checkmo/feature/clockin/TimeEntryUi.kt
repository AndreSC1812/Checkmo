package com.ascdev.checkmo.feature.clockin

import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.ascdev.checkmo.R
import com.ascdev.checkmo.core.designsystem.theme.CheckmoTheme
import com.ascdev.checkmo.domain.model.TimeEntryError
import com.ascdev.checkmo.domain.model.TimeEntryType
import com.ascdev.checkmo.domain.model.WorkStatus

// UI mappings shared by the clock-in screen and, later, the history and team screens.

@StringRes
fun TimeEntryType.labelRes(): Int = when (this) {
    TimeEntryType.CLOCK_IN -> R.string.entry_clock_in
    TimeEntryType.BREAK_START -> R.string.entry_break_start
    TimeEntryType.BREAK_END -> R.string.entry_break_end
    TimeEntryType.CLOCK_OUT -> R.string.entry_clock_out
}

@StringRes
fun TimeEntryType.actionRes(): Int = when (this) {
    TimeEntryType.CLOCK_IN -> R.string.action_clock_in
    TimeEntryType.BREAK_START -> R.string.action_break_start
    TimeEntryType.BREAK_END -> R.string.action_break_end
    TimeEntryType.CLOCK_OUT -> R.string.action_clock_out
}

@DrawableRes
fun TimeEntryType.iconRes(): Int = when (this) {
    TimeEntryType.CLOCK_IN -> R.drawable.ic_login
    TimeEntryType.BREAK_START -> R.drawable.ic_coffee
    TimeEntryType.BREAK_END -> R.drawable.ic_play
    TimeEntryType.CLOCK_OUT -> R.drawable.ic_logout
}

/** Container and content colors for an entry type. */
@Composable
@ReadOnlyComposable
fun TimeEntryType.colors(): Pair<Color, Color> {
    val colors = CheckmoTheme.extendedColors
    return when (this) {
        TimeEntryType.CLOCK_IN, TimeEntryType.BREAK_END -> colors.clockIn to colors.onClockIn
        TimeEntryType.BREAK_START -> colors.pause to colors.onPause
        TimeEntryType.CLOCK_OUT -> colors.clockOut to colors.onClockOut
    }
}

@StringRes
fun WorkStatus.labelRes(): Int = when (this) {
    WorkStatus.OFF_DUTY -> R.string.work_status_off_duty
    WorkStatus.WORKING -> R.string.work_status_working
    WorkStatus.ON_BREAK -> R.string.work_status_on_break
}

@StringRes
fun TimeEntryError.messageRes(): Int = when (this) {
    TimeEntryError.NOT_SIGNED_IN -> R.string.time_entry_error_not_signed_in
    TimeEntryError.INVALID_TRANSITION -> R.string.time_entry_error_invalid_transition
    TimeEntryError.UNKNOWN -> R.string.time_entry_error_unknown
}

fun ClockInMessage.text(resources: Resources): String = when (this) {
    is ClockInMessage.EntryFailed -> resources.getString(error.messageRes())
    ClockInMessage.VerificationUnavailable -> resources.getString(R.string.verification_unavailable)
    is ClockInMessage.VerificationFailed -> resources.getString(R.string.verification_failed, reason)
}
