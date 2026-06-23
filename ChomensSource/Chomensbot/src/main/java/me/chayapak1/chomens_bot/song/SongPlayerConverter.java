/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.song;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.song.Converter;
import me.chayapak1.chomens_bot.song.Instrument;
import me.chayapak1.chomens_bot.song.Note;
import me.chayapak1.chomens_bot.song.Song;
import org.cloudburstmc.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;

public class SongPlayerConverter
implements Converter {
    public static final byte[] FILE_TYPE_SIGNATURE = new byte[]{-53, 123, -51, -124, -122, -46, -35, 38};
    public static final long MAX_UNCOMPRESSED_SIZE = 0x3200000L;

    @Override
    public Song getSongFromBytes(byte[] bytes, String fileName, Bot bot) throws Exception {
        short noteId;
        LimitedSizeInputStream is = new LimitedSizeInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)), 0x3200000L);
        bytes = is.readAllBytes();
        is.close();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (byte b : FILE_TYPE_SIGNATURE) {
            if (b == buffer.get()) continue;
            throw new IOException("Invalid file type signature");
        }
        byte version = buffer.get();
        if (version != 1) {
            throw new IOException("Unsupported format version!");
        }
        long songLength = buffer.getLong();
        String songName = SongPlayerConverter.getString(buffer, bytes.length);
        int loop = buffer.get() & 0xFF;
        int loopCount = buffer.get() & 0xFF;
        long loopPosition = buffer.getLong();
        Song song = new Song(fileName, bot, !songName.trim().isEmpty() ? songName : null, null, null, null, null, false);
        song.length = songLength;
        song.loopPosition = loopPosition;
        long time = 0L;
        while ((noteId = buffer.getShort()) >= 0 && noteId < 400) {
            song.add(new Note(Instrument.fromId(noteId / 25), noteId % 25, noteId % 25, 1.0f, time += SongPlayerConverter.getVarLong(buffer), Vector3d.ZERO, false));
        }
        if ((noteId & 0xFFFF) != 65535) {
            throw new IOException("Song contains invalid note id of " + noteId);
        }
        return song;
    }

    private static String getString(ByteBuffer buffer, int maxSize) throws IOException {
        int length = buffer.getInt();
        if (length > maxSize) {
            throw new IOException("String is too large");
        }
        byte[] arr = new byte[length];
        buffer.get(arr, 0, length);
        return new String(arr, StandardCharsets.UTF_8);
    }

    private static long getVarLong(ByteBuffer buffer) {
        long val = 0L;
        long mult = 1L;
        int flag = 1;
        while (flag != 0) {
            int b = buffer.get() & 0xFF;
            val += (long)(b & 0x7F) * mult;
            mult <<= 7;
            flag = b >>> 7;
        }
        return val;
    }

    private static class LimitedSizeInputStream
    extends InputStream {
        private final InputStream original;
        private final long maxSize;
        private long total;

        public LimitedSizeInputStream(InputStream original, long maxSize) {
            this.original = original;
            this.maxSize = maxSize;
        }

        @Override
        public int read() throws IOException {
            int i = this.original.read();
            if (i >= 0) {
                this.incrementCounter(1);
            }
            return i;
        }

        @Override
        public int read(byte @NotNull [] b) throws IOException {
            return this.read(b, 0, b.length);
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            int i = this.original.read(b, off, len);
            if (i >= 0) {
                this.incrementCounter(i);
            }
            return i;
        }

        private void incrementCounter(int size) throws IOException {
            this.total += (long)size;
            if (this.total > this.maxSize) {
                throw new IOException("Input stream exceeded maximum size of " + this.maxSize + " bytes");
            }
        }
    }
}

