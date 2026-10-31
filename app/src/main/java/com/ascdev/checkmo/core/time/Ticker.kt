package com.ascdev.checkmo.core.time

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/** Emits the current time periodically so running timers can refresh. */
fun interface Ticker {
    fun ticks(): Flow<Instant>
}

class SystemTicker @Inject constructor(private val clock: Clock) : Ticker {

    override fun ticks(): Flow<Instant> = flow {
        while (true) {
            emit(clock.instant())
            // Align to the next whole second so the displayed seconds change in step with the clock.
            delay(1_000 - clock.millis() % 1_000)
        }
    }
}
