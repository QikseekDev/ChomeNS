/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtilities {
    public static String formatTime(long milliseconds, String format, ZoneId zoneId) {
        Instant instant = Instant.ofEpochMilli(milliseconds);
        OffsetDateTime localDateTime = OffsetDateTime.ofInstant(instant, zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(formatter);
    }
}

