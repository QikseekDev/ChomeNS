/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class StringUtilities {
    public static String removeNamespace(String command) {
        StringBuilder removedCommand = new StringBuilder(command);
        String[] splitSpace = command.split("\\s+");
        String[] splitColon = splitSpace[0].split(":");
        if (splitColon.length >= 2) {
            removedCommand.setLength(0);
            removedCommand.append(String.join((CharSequence)":", Arrays.copyOfRange(splitColon, 1, splitColon.length)));
            if (splitSpace.length > 1) {
                removedCommand.append(' ');
                removedCommand.append(String.join((CharSequence)" ", Arrays.copyOfRange(splitSpace, 1, splitSpace.length)));
            }
        }
        return removedCommand.toString();
    }

    public static String fromUTF8Lossy(byte[] input) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < input.length) {
            byte b = input[i];
            if ((b & 0x80) == 0) {
                result.append((char)b);
            } else {
                int bytesRemaining = input.length - i;
                int seqLen = -1;
                if ((b & 0xE0) == 192 && bytesRemaining >= 2) {
                    seqLen = 2;
                } else if ((b & 0xF0) == 224 && bytesRemaining >= 3) {
                    seqLen = 3;
                } else if ((b & 0xF8) == 240 && bytesRemaining >= 4) {
                    seqLen = 4;
                }
                if (seqLen > 1) {
                    boolean valid = true;
                    for (int j = 1; j < seqLen; ++j) {
                        if ((input[i + j] & 0xC0) == 128) continue;
                        valid = false;
                        break;
                    }
                    if (valid) {
                        try {
                            String s2 = new String(input, i, seqLen, StandardCharsets.UTF_8);
                            result.append(s2);
                            i += seqLen;
                            continue;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                result.append(String.format("<%04X>", b & 0xFF));
            }
            ++i;
        }
        return result.toString();
    }

    public static String truncateToFitUtf8ByteLength(String s2, int maxBytes) {
        if (s2 == null) {
            return null;
        }
        Charset charset = StandardCharsets.UTF_8;
        CharsetDecoder decoder = charset.newDecoder();
        byte[] sba = s2.getBytes(charset);
        if (sba.length <= maxBytes) {
            return s2;
        }
        ByteBuffer bb = ByteBuffer.wrap(sba, 0, maxBytes);
        CharBuffer cb = CharBuffer.allocate(maxBytes);
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        decoder.decode(bb, cb, true);
        decoder.flush(cb);
        return new String(cb.array(), 0, cb.position());
    }

    public static boolean containsIgnoreCase(String src, String what) {
        int length = what.length();
        if (length == 0) {
            return true;
        }
        char firstLo = Character.toLowerCase(what.charAt(0));
        char firstUp = Character.toUpperCase(what.charAt(0));
        for (int i = src.length() - length; i >= 0; --i) {
            char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp || !src.regionMatches(true, i, what, 0, length)) continue;
            return true;
        }
        return false;
    }

    public static String addPlural(long amount, String unit) {
        return amount > 1L ? unit + "s" : unit;
    }

    public static boolean isNotNullAndNotBlank(String text) {
        return text != null && !text.isBlank();
    }

    public static String replaceAllWithMap(String input, Map<String, String> replacements) {
        String result = input;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replaceAll(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos) + replacement + string.substring(pos + toReplace.length());
        }
        return string;
    }
}

