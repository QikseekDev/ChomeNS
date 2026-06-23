/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Ascii85 {
    private static final int ASCII_SHIFT = 33;
    private static final int[] BASE85_POW = new int[]{1, 85, 7225, 614125, 52200625};
    private static final Pattern REMOVE_WHITESPACE = Pattern.compile("\\s+");

    private Ascii85() {
    }

    public static String encode(byte[] payload) {
        if (payload == null) {
            throw new IllegalArgumentException("You must provide a non-null input");
        }
        StringBuilder stringBuff = new StringBuilder(payload.length * 5 / 4);
        byte[] chunk = new byte[4];
        int chunkIndex = 0;
        for (byte currByte : payload) {
            chunk[chunkIndex++] = currByte;
            if (chunkIndex != 4) continue;
            int value = Ascii85.byteToInt(chunk);
            if (value == 0) {
                stringBuff.append('z');
            } else {
                stringBuff.append(Ascii85.encodeChunk(value));
            }
            Arrays.fill(chunk, (byte)0);
            chunkIndex = 0;
        }
        if (chunkIndex > 0) {
            int numPadded = chunk.length - chunkIndex;
            Arrays.fill(chunk, chunkIndex, chunk.length, (byte)0);
            int value = Ascii85.byteToInt(chunk);
            char[] encodedChunk = Ascii85.encodeChunk(value);
            for (int i = 0; i < encodedChunk.length - numPadded; ++i) {
                stringBuff.append(encodedChunk[i]);
            }
        }
        return stringBuff.toString();
    }

    private static char[] encodeChunk(int value) {
        long longValue = (long)value & 0xFFFFFFFFL;
        char[] encodedChunk = new char[5];
        for (int i = 0; i < encodedChunk.length; ++i) {
            encodedChunk[i] = (char)(longValue / (long)BASE85_POW[4 - i] + 33L);
            longValue %= (long)BASE85_POW[4 - i];
        }
        return encodedChunk;
    }

    public static byte[] decode(String chars) {
        if (chars == null) {
            throw new IllegalArgumentException("You must provide a non-null input");
        }
        int inputLength = chars.length();
        long zCount = chars.chars().filter(c -> c == 122).count();
        BigDecimal uncompressedZLength = BigDecimal.valueOf(zCount).multiply(BigDecimal.valueOf(4L));
        BigDecimal uncompressedNonZLength = BigDecimal.valueOf((long)inputLength - zCount).multiply(BigDecimal.valueOf(4L)).divide(BigDecimal.valueOf(5L));
        BigDecimal uncompressedLength = uncompressedZLength.add(uncompressedNonZLength);
        ByteBuffer bytebuff = ByteBuffer.allocate(uncompressedLength.intValue());
        chars = REMOVE_WHITESPACE.matcher(chars).replaceAll("");
        byte[] payload = chars.getBytes(StandardCharsets.US_ASCII);
        byte[] chunk = new byte[5];
        int chunkIndex = 0;
        for (byte currByte : payload) {
            if (currByte == 122) {
                if (chunkIndex > 0) {
                    throw new IllegalArgumentException("The payload is not base 85 encoded.");
                }
                chunk[chunkIndex++] = 33;
                chunk[chunkIndex++] = 33;
                chunk[chunkIndex++] = 33;
                chunk[chunkIndex++] = 33;
                chunk[chunkIndex++] = 33;
            } else {
                chunk[chunkIndex++] = currByte;
            }
            if (chunkIndex != 5) continue;
            bytebuff.put(Ascii85.decodeChunk(chunk));
            Arrays.fill(chunk, (byte)0);
            chunkIndex = 0;
        }
        if (chunkIndex > 0) {
            int numPadded = chunk.length - chunkIndex;
            Arrays.fill(chunk, chunkIndex, chunk.length, (byte)117);
            byte[] paddedDecode = Ascii85.decodeChunk(chunk);
            for (int i = 0; i < paddedDecode.length - numPadded; ++i) {
                bytebuff.put(paddedDecode[i]);
            }
        }
        bytebuff.flip();
        return Arrays.copyOf(bytebuff.array(), bytebuff.limit());
    }

    private static byte[] decodeChunk(byte[] chunk) {
        if (chunk.length != 5) {
            throw new IllegalArgumentException("You can only decode chunks of size 5.");
        }
        int value = 0;
        value += (chunk[0] - 33) * BASE85_POW[4];
        value += (chunk[1] - 33) * BASE85_POW[3];
        value += (chunk[2] - 33) * BASE85_POW[2];
        value += (chunk[3] - 33) * BASE85_POW[1];
        return Ascii85.intToByte(value += (chunk[4] - 33) * BASE85_POW[0]);
    }

    private static int byteToInt(byte[] value) {
        if (value == null || value.length != 4) {
            throw new IllegalArgumentException("You cannot create an int without exactly 4 bytes.");
        }
        return ByteBuffer.wrap(value).getInt();
    }

    private static byte[] intToByte(int value) {
        return new byte[]{(byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)value};
    }
}

