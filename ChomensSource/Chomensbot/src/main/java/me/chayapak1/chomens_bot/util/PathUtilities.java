/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.nio.file.Path;
import java.util.List;

public class PathUtilities {
    public static void sort(List<Path> paths) {
        paths.sort((p1, p2) -> {
            String s2;
            String s1 = p1.getFileName().toString();
            int result = s1.compareToIgnoreCase(s2 = p2.getFileName().toString());
            if (result == 0) {
                return s2.compareTo(s1);
            }
            return result;
        });
    }
}

