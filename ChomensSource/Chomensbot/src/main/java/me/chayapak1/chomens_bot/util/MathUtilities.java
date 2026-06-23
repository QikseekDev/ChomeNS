/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

public class MathUtilities {
    public static int between(int min2, int max) {
        return (int)Math.floor(Math.random() * (double)(max - min2) + (double)min2);
    }

    public static int clamp(int value, int min2, int max) {
        return Math.max(Math.min(value, max), min2);
    }

    public static float clamp(float value, float min2, float max) {
        return Math.max(Math.min(value, max), min2);
    }

    public static double clamp(double value, double min2, double max) {
        return Math.max(Math.min(value, max), min2);
    }
}

