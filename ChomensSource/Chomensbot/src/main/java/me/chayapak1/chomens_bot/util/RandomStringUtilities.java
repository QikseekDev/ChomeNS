/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.security.SecureRandom;

public class RandomStringUtilities {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase();
    private static final String DIGITS = "0123456789";
    public static final char[] ALPHANUMERIC = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ" + LOWER + "0123456789").toCharArray();
    public static final char[] ALPHABETS_ONLY = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ" + LOWER).toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(int length, char[] symbols) {
        char[] buf = new char[length];
        for (int i = 0; i < buf.length; ++i) {
            buf[i] = symbols[RANDOM.nextInt(symbols.length)];
        }
        return new String(buf);
    }
}

