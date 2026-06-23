/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

public class IllegalCharactersUtilities {
    public static boolean isInvalidChatCharacter(char character) {
        return character == '\u00a7' || character < ' ' || character == '\u007f';
    }

    public static boolean isValidChatString(String string) {
        for (char character : string.toCharArray()) {
            if (!IllegalCharactersUtilities.isInvalidChatCharacter(character)) continue;
            return false;
        }
        return true;
    }

    public static String stripIllegalCharacters(String string) {
        StringBuilder replaced = new StringBuilder();
        for (char character : string.toCharArray()) {
            if (IllegalCharactersUtilities.isInvalidChatCharacter(character)) continue;
            replaced.append(character);
        }
        return replaced.toString();
    }
}

