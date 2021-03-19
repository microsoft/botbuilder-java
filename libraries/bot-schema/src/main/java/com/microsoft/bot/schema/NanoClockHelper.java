// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * A customized nanoseconds clock providing access to the current instant, date and time using a time-zone.
 */
public class NanoClockHelper extends Clock {

    private final Clock clock;
    private final long initialNanos;
    private final Instant initialInstant;

    /**
     * Obtains a clock that returns the current instant using the best available
     * system clock with nanoseconds.
     */
    public NanoClockHelper() {
        this(Clock.systemUTC());
    }

    /**
     * Obtains a clock that returns the current instant using the best available
     * system clock with nanoseconds.
     * @param clock A {@link Clock}
     */
    public NanoClockHelper(final Clock clock) {
        this.clock = clock;
        initialInstant = clock.instant();
        initialNanos = getSystemNanos();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZoneId getZone() {
        return clock.getZone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant instant() {
        return initialInstant.plusNanos(getSystemNanos() - initialNanos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Clock withZone(final ZoneId zone) {
        return new NanoClockHelper(clock.withZone(zone));
    }

    private long getSystemNanos() {
        return System.nanoTime();
    }
}
