/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.nio.ByteBuffer;
import java.util.UUID;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;

public class UUIDUtilities {
    public static UUID getOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
    }

    public static UUID tryParse(String input) {
        try {
            return UUID.fromString(input);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static int[] intArray(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(0, uuid.getMostSignificantBits());
        buffer.putLong(8, uuid.getLeastSignificantBits());
        int[] intArray = new int[4];
        for (int i = 0; i < intArray.length; ++i) {
            intArray[i] = buffer.getInt();
        }
        return intArray;
    }

    public static NbtMap tag(UUID uuid) {
        NbtMapBuilder builder = NbtMap.builder();
        builder.putIntArray("", UUIDUtilities.intArray(uuid));
        return builder.build();
    }

    public static String snbt(UUID uuid) {
        int[] array = UUIDUtilities.intArray(uuid);
        return String.format("[I;%d,%d,%d,%d]", array[0], array[1], array[2], array[3]);
    }

    public static String selector(UUID uuid) {
        return UUIDUtilities.selector(uuid, true);
    }

    public static String selector(UUID uuid, boolean end) {
        return "@p[nbt={UUID:" + UUIDUtilities.snbt(uuid) + "}" + (end ? "]" : "");
    }

    public static String exclusiveSelector(UUID uuid) {
        return UUIDUtilities.exclusiveSelector(uuid, true);
    }

    public static String exclusiveSelector(UUID uuid, boolean end) {
        return "@a[nbt=!{UUID:" + UUIDUtilities.snbt(uuid) + "}" + (end ? "]" : "");
    }
}

