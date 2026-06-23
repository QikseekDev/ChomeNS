/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.song;

import java.nio.charset.StandardCharsets;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.song.Converter;
import me.chayapak1.chomens_bot.song.Instrument;
import me.chayapak1.chomens_bot.song.Note;
import me.chayapak1.chomens_bot.song.Song;
import org.cloudburstmc.math.vector.Vector3d;

public class TextFileConverter
implements Converter {
    @Override
    public Song getSongFromBytes(byte[] bytes, String fileName, Bot bot) {
        String data = new String(bytes, StandardCharsets.UTF_8);
        if (!data.contains(":")) {
            return null;
        }
        int length = 0;
        Song song = new Song(fileName, bot, null, null, null, null, null, false);
        for (String line : data.split("\r\n|\r|\n")) {
            if (line.isBlank()) continue;
            if (line.startsWith("title:")) {
                song.songName = line.substring("title:".length());
                continue;
            }
            if (line.startsWith("author:")) {
                song.songAuthor = line.substring("author:".length());
                continue;
            }
            if (line.startsWith("originalAuthor:")) {
                song.songOriginalAuthor = line.substring("originalAuthor:".length());
                continue;
            }
            if (line.startsWith("description:")) {
                song.songDescription = line.substring("description:".length());
                continue;
            }
            song.updateName();
            String[] split = line.split(":");
            int tick = Integer.parseInt(split[0]);
            int pitch = (int)Float.parseFloat(split[1]);
            String instrument = split[2];
            int intInstrument = -1;
            try {
                intInstrument = Integer.parseInt(instrument);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            float volume = 1.0f;
            if (split.length > 3) {
                volume = Float.parseFloat(split[3]);
            }
            int time = tick * 50;
            length = Math.max(length, time);
            song.add(new Note(intInstrument == -1 ? Instrument.of(instrument) : Instrument.fromId(intInstrument), pitch, pitch, volume, time, Vector3d.ZERO, false));
        }
        song.length = song.get((int)(song.size() - 1)).time + 50.0;
        return song;
    }
}

