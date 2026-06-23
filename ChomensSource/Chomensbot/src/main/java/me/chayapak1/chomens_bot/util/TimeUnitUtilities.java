/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;

public class TimeUnitUtilities {
    public static Pair<Long, TimeUnit> fromChronoUnit(long interval, ChronoUnit chronoUnit) {
        Duration duration = chronoUnit.getDuration();
        if (duration.getSeconds() >= 1L) {
            return Pair.of(interval * duration.toSeconds(), TimeUnit.SECONDS);
        }
        return Pair.of(interval * duration.toNanos(), TimeUnit.NANOSECONDS);
    }
}

