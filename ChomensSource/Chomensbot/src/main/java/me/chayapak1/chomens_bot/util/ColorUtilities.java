/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextFormat;
import net.kyori.adventure.text.serializer.legacy.CharacterAndFormat;

public class ColorUtilities {
    private static final Map<TextFormat, Character> formatToLegacyMap = CharacterAndFormat.defaults().stream().collect(Collectors.toUnmodifiableMap(CharacterAndFormat::format, CharacterAndFormat::character));
    private static final Map<Integer, String> ansiToIrcMap = new Object2ObjectOpenHashMap<Integer, String>();
    private static final Map<Integer, String> ansiStyleToIrcMap = new Object2ObjectOpenHashMap<Integer, String>();

    public static TextColor getColorByString(String _color) {
        String color = _color.toLowerCase();
        if (color.startsWith("#")) {
            return TextColor.fromHexString(color);
        }
        return Optional.ofNullable(NamedTextColor.NAMES.value(color)).orElse(NamedTextColor.WHITE);
    }

    public static char getClosestChatColor(int rgb) {
        NamedTextColor closestNamed = NamedTextColor.nearestTo(TextColor.color(rgb));
        return formatToLegacyMap.get(closestNamed).charValue();
    }

    public static String convertAnsiToIrc(String input) {
        StringBuilder result = new StringBuilder();
        boolean insideEscape = false;
        StringBuilder ansiCode = new StringBuilder();
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            if (insideEscape) {
                if (c == 'm') {
                    String[] codes;
                    insideEscape = false;
                    for (String code : codes = ansiCode.toString().split(";")) {
                        try {
                            int ansiColorCode = Integer.parseInt(code);
                            if (ansiToIrcMap.containsKey(ansiColorCode)) {
                                result.append("\u0003").append(ansiToIrcMap.get(ansiColorCode));
                                continue;
                            }
                            if (!ansiStyleToIrcMap.containsKey(ansiColorCode)) continue;
                            result.append(ansiStyleToIrcMap.get(ansiColorCode));
                        }
                        catch (NumberFormatException numberFormatException) {
                            // empty catch block
                        }
                    }
                    ansiCode.setLength(0);
                    continue;
                }
                ansiCode.append(c);
                continue;
            }
            if (c == '\u001b' && i + 1 < input.length() && input.charAt(i + 1) == '[') {
                insideEscape = true;
                ++i;
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }

    static {
        ansiToIrcMap.put(0, "0");
        ansiToIrcMap.put(30, "1");
        ansiToIrcMap.put(31, "40");
        ansiToIrcMap.put(32, "32");
        ansiToIrcMap.put(33, "53");
        ansiToIrcMap.put(34, "60");
        ansiToIrcMap.put(35, "49");
        ansiToIrcMap.put(36, "46");
        ansiToIrcMap.put(37, "96");
        ansiToIrcMap.put(39, "0");
        ansiToIrcMap.put(90, "92");
        ansiToIrcMap.put(92, "56");
        ansiToIrcMap.put(96, "58");
        ansiToIrcMap.put(91, "52");
        ansiToIrcMap.put(94, "60");
        ansiToIrcMap.put(95, "61");
        ansiToIrcMap.put(93, "54");
        ansiToIrcMap.put(97, "0");
        HashMap<Integer, String> clone = new HashMap<Integer, String>(ansiToIrcMap);
        for (Map.Entry entry : clone.entrySet()) {
            ansiToIrcMap.put((Integer)entry.getKey() + 10, ansiToIrcMap.get(entry.getKey()));
        }
        ansiToIrcMap.put(49, "01");
        ansiStyleToIrcMap.put(1, "\u0002");
        ansiStyleToIrcMap.put(3, "\u001d");
        ansiStyleToIrcMap.put(4, "\u001f");
    }
}

