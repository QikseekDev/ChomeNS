/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimestampUtilities {
    public static final Pattern TIMESTAMP_PATTERN = Pattern.compile("(?:(\\d+):)?(\\d+):(\\d+)");

    public static long parseTimestamp(String timestamp) {
        Matcher matcher = TIMESTAMP_PATTERN.matcher(timestamp);
        if (!matcher.matches()) {
            return -1L;
        }
        long time = 0L;
        String hourString = matcher.group(1);
        String minuteString = matcher.group(2);
        String secondString = matcher.group(3);
        if (hourString != null) {
            time += Long.parseLong(hourString) * 60L * 60L * 1000L;
        }
        time += Long.parseLong(minuteString) * 60L * 1000L;
        return time += (long)(Double.parseDouble(secondString) * 1000.0);
    }
}

