package com.ascdev.checkmo.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors for clock-in, pause and clock-out states that are not covered by Material roles.
 */
@Immutable
data class CheckmoExtendedColors(
    val clockIn: Color,
    val onClockIn: Color,
    val clockInContainer: Color,
    val onClockInContainer: Color,
    val pause: Color,
    val onPause: Color,
    val pauseContainer: Color,
    val onPauseContainer: Color,
    val clockOut: Color,
    val onClockOut: Color,
    val clockOutContainer: Color,
    val onClockOutContainer: Color,
)

val LightExtendedColors = CheckmoExtendedColors(
    clockIn = ClockInGreen,
    onClockIn = OnClockInLight,
    clockInContainer = ClockInContainerLight,
    onClockInContainer = OnClockInContainerLight,
    pause = PauseOrange,
    onPause = OnPauseLight,
    pauseContainer = PauseContainerLight,
    onPauseContainer = OnPauseContainerLight,
    clockOut = ClockOutRed,
    onClockOut = OnClockOutLight,
    clockOutContainer = ClockOutContainerLight,
    onClockOutContainer = OnClockOutContainerLight,
)

val DarkExtendedColors = CheckmoExtendedColors(
    clockIn = ClockInDark,
    onClockIn = OnClockInDark,
    clockInContainer = ClockInContainerDark,
    onClockInContainer = OnClockInContainerDark,
    pause = PauseDark,
    onPause = OnPauseDark,
    pauseContainer = PauseContainerDark,
    onPauseContainer = OnPauseContainerDark,
    clockOut = ClockOutDark,
    onClockOut = OnClockOutDark,
    clockOutContainer = ClockOutContainerDark,
    onClockOutContainer = OnClockOutContainerDark,
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
