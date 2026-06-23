/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.List;
import me.chayapak1.chomens_bot.util.ValueComparableMap;
import org.apache.commons.lang3.StringUtils;

public class LevenshteinUtilities {
    public static List<String> searchTitles(String text, Collection<String> texts) {
        ValueComparableMap output = new ValueComparableMap(Ordering.natural());
        for (String eachText : texts) {
            int score = LevenshteinUtilities.searchLevenshteinDefault(text, eachText, false);
            output.put(eachText, output.getOrDefault(eachText, 0) + score);
        }
        return output.keySet().stream().toList();
    }

    public static int searchLevenshteinDefault(String text, String filter, boolean caseSensitive) {
        return LevenshteinUtilities.levenshteinDistance(caseSensitive ? filter : filter.toLowerCase(), caseSensitive ? text : text.toLowerCase(), 1, 8, 8);
    }

    public static int searchInWords(String text, String filter) {
        String[] words;
        if (filter.isEmpty()) {
            return 1;
        }
        int wordsFound = 0;
        text = text.toLowerCase();
        for (String word : words = filter.toLowerCase().split(" ")) {
            if (!text.contains(word)) {
                return 0;
            }
            wordsFound += StringUtils.countMatches(text, word);
        }
        return wordsFound;
    }

    public static int levenshteinDistance(String from, String to, int insCost, int subCost, int delCost) {
        int i;
        int textLength = from.length();
        int filterLength = to.length();
        if (textLength == 0) {
            return filterLength * insCost;
        }
        if (filterLength == 0) {
            return textLength * delCost;
        }
        int[][] d = new int[textLength + 1][filterLength + 1];
        for (i = 0; i <= textLength; ++i) {
            d[i][0] = i * delCost;
        }
        for (int j = 0; j <= filterLength; ++j) {
            d[0][j] = j * insCost;
        }
        for (i = 1; i <= textLength; ++i) {
            for (int j = 1; j <= filterLength; ++j) {
                int sCost = d[i - 1][j - 1] + (from.charAt(i - 1) == to.charAt(j - 1) ? 0 : subCost);
                int dCost = d[i - 1][j] + delCost;
                int iCost = d[i][j - 1] + insCost;
                d[i][j] = Math.min(Math.min(dCost, iCost), sCost);
            }
        }
        return d[textLength][filterLength];
    }
}

