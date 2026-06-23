/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.song;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.song.Song;

public interface Converter {
    public Song getSongFromBytes(byte[] var1, String var2, Bot var3) throws Exception;
}

