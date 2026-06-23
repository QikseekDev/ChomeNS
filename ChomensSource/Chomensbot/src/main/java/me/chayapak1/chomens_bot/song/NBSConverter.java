/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.song;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.song.Converter;
import me.chayapak1.chomens_bot.song.Instrument;
import me.chayapak1.chomens_bot.song.Note;
import me.chayapak1.chomens_bot.song.Song;
import me.chayapak1.chomens_bot.util.StringUtilities;
import org.cloudburstmc.math.vector.Vector3d;

public class NBSConverter
implements Converter {
    public static final Instrument[] INSTRUMENT_INDEX = new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BASEDRUM, Instrument.SNARE, Instrument.HAT, Instrument.GUITAR, Instrument.FLUTE, Instrument.BELL, Instrument.CHIME, Instrument.XYLOPHONE, Instrument.IRON_XYLOPHONE, Instrument.COW_BELL, Instrument.DIDGERIDOO, Instrument.BIT, Instrument.BANJO, Instrument.PLING};
    private static final Map<String, String> minecraftToPlaySound = new Object2ObjectOpenHashMap<String, String>();
    private static final List<String> playSound = new ObjectArrayList<String>();

    @Override
    public Song getSongFromBytes(byte[] bytes, String fileName, Bot bot) throws IOException {
        short tickJumps;
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte format = 0;
        byte vanillaInstrumentCount = 0;
        short songLength = buffer.getShort();
        if (songLength == 0) {
            format = buffer.get();
        }
        if (format >= 1) {
            vanillaInstrumentCount = buffer.get();
        }
        if (format >= 3) {
            songLength = buffer.getShort();
        }
        int layerCount = buffer.getShort();
        String songName = NBSConverter.getString(buffer, bytes.length);
        String songAuthor = NBSConverter.getString(buffer, bytes.length);
        String songOriginalAuthor = NBSConverter.getString(buffer, bytes.length);
        String songDescription = NBSConverter.getString(buffer, bytes.length);
        double tempo = buffer.getShort();
        byte autoSaving = buffer.get();
        byte autoSavingDuration = buffer.get();
        byte timeSignature = buffer.get();
        int minutesSpent = buffer.getInt();
        int leftClicks = buffer.getInt();
        int rightClicks = buffer.getInt();
        int blocksAdded = buffer.getInt();
        int blocksRemoved = buffer.getInt();
        String origFileName = NBSConverter.getString(buffer, bytes.length);
        byte loop = 0;
        byte maxLoopCount = 0;
        short loopStartTick = 0;
        if (format >= 4) {
            loop = buffer.get();
            maxLoopCount = buffer.get();
            loopStartTick = buffer.getShort();
        }
        ObjectArrayList nbsNotes = new ObjectArrayList();
        long tick = -1L;
        while ((tickJumps = buffer.getShort()) != 0) {
            short layerJumps;
            tick += (long)tickJumps;
            int layer = -1;
            while ((layerJumps = buffer.getShort()) != 0) {
                layer = (short)(layer + (short)layerJumps);
                NBSNote note = new NBSNote();
                note.tick = tick;
                note.layer = (short)layer;
                note.instrument = buffer.get();
                note.key = buffer.get();
                if (format >= 4) {
                    note.velocity = buffer.get();
                    note.panning = buffer.get();
                    note.pitch = buffer.getShort();
                }
                nbsNotes.add(note);
            }
        }
        ObjectArrayList nbsLayers = new ObjectArrayList();
        if (buffer.hasRemaining()) {
            for (int i = 0; i < layerCount; ++i) {
                NBSLayer layer = new NBSLayer();
                layer.name = NBSConverter.getString(buffer, bytes.length);
                if (format >= 4) {
                    layer.lock = buffer.get();
                }
                layer.volume = buffer.get();
                if (format >= 2) {
                    layer.stereo = buffer.get();
                }
                nbsLayers.add(layer);
            }
        }
        ArrayList<TempoSection> tempoSections = new ArrayList<TempoSection>();
        tempoSections.add(new TempoSection(0L, tempo));
        ObjectArrayList customInstruments = new ObjectArrayList();
        if (buffer.hasRemaining()) {
            int customInstrumentCount = buffer.get();
            for (int i = 0; i < customInstrumentCount; ++i) {
                NBSCustomInstrument customInstrument = new NBSCustomInstrument();
                customInstrument.name = NBSConverter.getString(buffer, bytes.length);
                customInstrument.file = NBSConverter.getString(buffer, bytes.length);
                customInstrument.pitch = buffer.get();
                customInstrument.key = buffer.get() != 0;
                String processedName = customInstrument.name.replace("entity.firework.", "entity.firework_rocket.");
                if (processedName.equals("Toggle Rainbow")) {
                    customInstrument.isRainbowToggle = true;
                }
                String file = customInstrument.file.replaceFirst("minecraft/|Custom/", "").replace(".ogg", "");
                if (!customInstrument.isRainbowToggle) {
                    if (!playSound.contains(processedName) && minecraftToPlaySound.containsKey(file)) {
                        processedName = minecraftToPlaySound.get(file);
                    } else if (playSound.contains(file) && !minecraftToPlaySound.containsKey(processedName)) {
                        processedName = file;
                    }
                }
                customInstrument.name = processedName;
                customInstruments.add(customInstrument);
            }
        }
        StringBuilder layerNames = new StringBuilder();
        for (NBSLayer layer : nbsLayers) {
            layerNames.append(layer.name);
            layerNames.append("\n");
        }
        String stringLayerNames = layerNames.toString();
        Song song = new Song(!songName.isBlank() ? songName : fileName, bot, songName, songAuthor, songOriginalAuthor, songDescription, stringLayerNames.substring(0, Math.max(0, stringLayerNames.length() - 1)), true);
        if (loop > 0) {
            song.loopPosition = NBSConverter.getMilliTime(loopStartTick, tempoSections);
        }
        for (NBSNote note : nbsNotes) {
            double key;
            Instrument instrument;
            boolean isRainbowToggle = false;
            if (note.instrument < INSTRUMENT_INDEX.length) {
                instrument = INSTRUMENT_INDEX[note.instrument];
                key = (double)(note.key * 100 + note.pitch) / 100.0;
            } else {
                int index = note.instrument - INSTRUMENT_INDEX.length;
                if (index >= customInstruments.size()) continue;
                NBSCustomInstrument customInstrument = (NBSCustomInstrument)customInstruments.get(index);
                if (customInstrument.name.equals("Tempo Changer")) {
                    tempoSections.add(new TempoSection(note.tick, (double)Math.abs(note.pitch) * 100.0 / 15.0));
                }
                isRainbowToggle = customInstrument.isRainbowToggle;
                instrument = Instrument.of(customInstrument.name);
                key = (double)(note.key + customInstrument.pitch - 45) + (double)note.pitch / 100.0;
            }
            int layerVolume = 100;
            if (nbsLayers.size() > note.layer) {
                layerVolume = ((NBSLayer)nbsLayers.get((int)note.layer)).volume;
            }
            double pitch = key - 33.0;
            song.add(new Note(instrument, pitch, key, (float)note.velocity * (float)layerVolume / 10000.0f, NBSConverter.getMilliTime(note.tick, tempoSections), this.getPosition(Byte.toUnsignedInt(note.panning), nbsLayers.isEmpty() ? 100 : Byte.toUnsignedInt(((NBSLayer)nbsLayers.get((int)note.layer)).stereo)), isRainbowToggle));
        }
        song.length = song.get((int)(song.size() - 1)).time + 50.0;
        return song;
    }

    private Vector3d getPosition(int panning, int stereo) {
        double value = stereo == 100 && panning != 100 ? (double)panning : (panning == 100 && stereo != 100 ? (double)stereo : (double)(stereo + panning) / 2.0);
        double xPos = value > 100.0 ? (value - 100.0) / -100.0 : (value == 100.0 ? 0.0 : (value - 100.0) * -1.0 / 100.0);
        return Vector3d.from(xPos * 2.0, 0.0, 0.0);
    }

    private static String getString(ByteBuffer buffer, int maxSize) throws IOException {
        int length = buffer.getInt();
        if (length > maxSize) {
            throw new IOException("String is too large");
        }
        byte[] arr = new byte[length];
        buffer.get(arr, 0, length);
        return StringUtilities.fromUTF8Lossy(arr);
    }

    private static long getMilliTime(long currentTick, List<TempoSection> sections) {
        long totalMillis = 0L;
        for (int i = 0; i < sections.size(); ++i) {
            long endTick;
            TempoSection current = sections.get(i);
            TempoSection next = i + 1 < sections.size() ? sections.get(i + 1) : null;
            long startTick = current.startTick;
            long l = endTick = next != null ? Math.min(next.startTick, currentTick) : currentTick;
            if (currentTick < startTick) break;
            long ticksInThisSegment = endTick - startTick;
            totalMillis += (long)((double)(1000L * ticksInThisSegment * 100L) / current.tempo);
        }
        return totalMillis;
    }

    static {
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("sounds.json");
        assert (is != null);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String playSoundName = entry.getKey();
            JsonObject data = entry.getValue().getAsJsonObject();
            JsonArray sounds = data.getAsJsonArray("sounds");
            for (JsonElement element : sounds) {
                String sound;
                if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    sound = object.get("name").getAsString();
                } else {
                    sound = element.getAsString();
                }
                minecraftToPlaySound.put(sound, playSoundName);
                playSound.add(playSoundName);
            }
        }
    }

    public static class NBSNote {
        public long tick;
        public short layer;
        public byte instrument;
        public byte key;
        public byte velocity = (byte)100;
        public byte panning = (byte)100;
        public short pitch = 0;
    }

    public static class NBSLayer {
        public String name;
        public byte lock = 0;
        public byte volume;
        public byte stereo = (byte)100;
    }

    private record TempoSection(long startTick, double tempo) {
    }

    private static class NBSCustomInstrument {
        public String name;
        public String file;
        public byte pitch = 0;
        public boolean key = false;
        public boolean isRainbowToggle = false;

        private NBSCustomInstrument() {
        }
    }
}

