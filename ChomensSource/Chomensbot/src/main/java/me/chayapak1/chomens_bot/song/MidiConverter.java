/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.song;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.song.Converter;
import me.chayapak1.chomens_bot.song.Instrument;
import me.chayapak1.chomens_bot.song.Note;
import me.chayapak1.chomens_bot.song.Song;
import org.cloudburstmc.math.vector.Vector3d;

public class MidiConverter
implements Converter {
    public static final int TEXT = 1;
    public static final int TRACK_NAME = 3;
    public static final int LYRICS = 5;
    public static final int VOLUME_CONTROL_MSB = 7;
    public static final int PAN_CONTROL_MSB = 10;
    public static final int SET_INSTRUMENT = 192;
    public static final int SET_TEMPO = 81;
    public static final int RESET_CONTROLS = 121;
    public static final Int2ObjectOpenHashMap<Instrument[]> instrumentMap = new Int2ObjectOpenHashMap();
    public static final HashMap<Integer, Integer> percussionMap;

    @Override
    public Song getSongFromBytes(byte[] bytes, String name, Bot bot) throws InvalidMidiDataException, IOException {
        Sequence sequence2 = MidiSystem.getSequence(new ByteArrayInputStream(bytes));
        return MidiConverter.getSong(sequence2, name, bot);
    }

    /*
     * Enabled aggressive block sorting
     */
    public static Song getSong(Sequence sequence2, String name, Bot bot) {
        String stringText;
        String stringTracks;
        HashMap<Long, String> lyrics = new HashMap<Long, String>();
        long tpq = sequence2.getResolution();
        String songName = null;
        StringBuilder tracks = new StringBuilder();
        StringBuilder text = new StringBuilder();
        boolean isFirst = true;
        ArrayList<MidiEvent> tempoEvents = new ArrayList<MidiEvent>();
        Track[] trackArray = sequence2.getTracks();
        int n = trackArray.length;
        int n2 = 0;
        while (true) {
            if (n2 >= n) {
                stringTracks = tracks.toString();
                stringText = text.toString();
                if (stringText.endsWith("\n")) {
                    stringText = stringText.substring(0, stringText.length() - 1);
                }
                if (stringTracks.endsWith("\n")) {
                    stringTracks = stringTracks.substring(0, stringTracks.length() - 1);
                }
                break;
            }
            Track track = trackArray[n2];
            int trackSize = track.size();
            block19: for (int i = 0; i < trackSize; ++i) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (!(message instanceof MetaMessage)) continue;
                MetaMessage mm4 = (MetaMessage)message;
                switch (mm4.getType()) {
                    case 81: {
                        tempoEvents.add(event);
                        continue block19;
                    }
                    case 3: {
                        String stringTitle = MidiConverter.decodeStringWithUTF8OrShiftJIS(mm4.getData());
                        if (stringTitle.isBlank()) continue block19;
                        tracks.append(stringTitle);
                        tracks.append("\n");
                        if (!isFirst) continue block19;
                        songName = stringTitle + " (" + name + ")";
                        isFirst = false;
                        continue block19;
                    }
                    case 1: {
                        text.append(MidiConverter.decodeStringWithUTF8OrShiftJIS(mm4.getData()));
                        text.append('\n');
                        continue block19;
                    }
                    case 5: {
                        String lyric = MidiConverter.decodeStringWithUTF8OrShiftJIS(mm4.getMessage());
                        lyrics.put(event.getTick(), lyric);
                        continue block19;
                    }
                }
            }
            ++n2;
        }
        Song song = new Song(name, bot, songName, null, null, stringText, stringTracks, false);
        tempoEvents.sort(Comparator.comparingLong(MidiEvent::getTick));
        byte[] channelVolumes = new byte[16];
        byte[] channelPans = new byte[16];
        Arrays.fill(channelVolumes, (byte)127);
        Arrays.fill(channelPans, (byte)64);
        Track[] trackArray2 = sequence2.getTracks();
        int n3 = trackArray2.length;
        int n4 = 0;
        while (true) {
            if (n4 >= n3) {
                song.sort();
                return song;
            }
            Track track = trackArray2[n4];
            long microTime = 0L;
            int[] ids = new int[16];
            int mpq = 500000;
            int tempoEventIdx = 0;
            long prevTick = 0L;
            int trackSize = track.size();
            block21: for (int i = 0; i < trackSize; ++i) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                while (tempoEventIdx < tempoEvents.size() && event.getTick() > ((MidiEvent)tempoEvents.get(tempoEventIdx)).getTick()) {
                    long deltaTick = ((MidiEvent)tempoEvents.get(tempoEventIdx)).getTick() - prevTick;
                    prevTick = ((MidiEvent)tempoEvents.get(tempoEventIdx)).getTick();
                    microTime += (long)mpq / tpq * deltaTick;
                    MetaMessage mm5 = (MetaMessage)((MidiEvent)tempoEvents.get(tempoEventIdx)).getMessage();
                    byte[] data = mm5.getData();
                    int new_mpq = data[2] & 0xFF | (data[1] & 0xFF) << 8 | (data[0] & 0xFF) << 16;
                    if (new_mpq != 0) {
                        mpq = new_mpq;
                    }
                    ++tempoEventIdx;
                }
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)message;
                    block6 : switch (sm.getCommand()) {
                        case 192: {
                            ids[sm.getChannel()] = sm.getData1();
                            break;
                        }
                        case 144: {
                            long time;
                            Note note;
                            if (sm.getData2() == 0) continue block21;
                            int pitch = sm.getData1();
                            int velocity = sm.getData2();
                            float effectiveVelocity = (float)velocity * (float)channelVolumes[sm.getChannel()] / 127.0f;
                            int pan = (channelPans[sm.getChannel()] - 64) / 64;
                            long deltaTick = event.getTick() - prevTick;
                            prevTick = event.getTick();
                            Note note2 = note = sm.getChannel() == 9 ? MidiConverter.getMidiPercussionNote(pitch, effectiveVelocity, microTime, pan) : MidiConverter.getMidiInstrumentNote(ids[sm.getChannel()], pitch, effectiveVelocity, microTime += (long)mpq / tpq * deltaTick, pan);
                            if (note != null) {
                                song.add(note);
                            }
                            if (!((double)(time = microTime / 1000L) > song.length)) break;
                            song.length = time;
                            break;
                        }
                        case 128: {
                            long deltaTick = event.getTick() - prevTick;
                            prevTick = event.getTick();
                            long time = (microTime += (long)mpq / tpq * deltaTick) / 1000L;
                            if (!((double)time > song.length)) break;
                            song.length = time;
                            break;
                        }
                        case 176: {
                            switch (sm.getData1()) {
                                case 7: {
                                    channelVolumes[sm.getChannel()] = (byte)sm.getData2();
                                    break block6;
                                }
                                case 10: {
                                    channelPans[sm.getChannel()] = (byte)sm.getData2();
                                    break block6;
                                }
                                case 121: {
                                    channelVolumes[sm.getChannel()] = 127;
                                    channelPans[sm.getChannel()] = 127;
                                    break block6;
                                }
                            }
                            break;
                        }
                        case 255: {
                            Arrays.fill(channelVolumes, (byte)127);
                            Arrays.fill(channelPans, (byte)64);
                        }
                    }
                }
                if (lyrics.get(event.getTick()) == null) continue;
                song.lyrics.put(microTime / 1000L, (String)lyrics.get(event.getTick()));
            }
            ++n4;
        }
    }

    public static Note getMidiInstrumentNote(int midiInstrument, int midiPitch, float velocity, long microTime, int pan) {
        Instrument shiftedInstrument = null;
        Instrument[] instrumentList = instrumentMap.get(midiInstrument);
        if (instrumentList != null) {
            for (Instrument candidateInstrument : instrumentList) {
                if (midiPitch < candidateInstrument.offset || midiPitch > candidateInstrument.offset + 24) continue;
                shiftedInstrument = candidateInstrument;
                break;
            }
            if (shiftedInstrument == null) {
                Integer[] offsets = (Integer[])Arrays.stream(instrumentList).map(ins -> ins.offset).toArray(Integer[]::new);
                int distance = Math.abs(offsets[0] - midiPitch);
                int idx = 0;
                for (int c = 1; c < offsets.length; ++c) {
                    int cdistance = Math.abs(offsets[c] - midiPitch);
                    if (cdistance >= distance) continue;
                    idx = c;
                    distance = cdistance;
                }
                int closest = offsets[idx];
                shiftedInstrument = Arrays.stream(instrumentList).filter(ins -> ins.offset == closest).findFirst().orElse(null);
            }
        }
        if (shiftedInstrument == null) {
            return null;
        }
        int shiftedInstrumentPitch = midiPitch - shiftedInstrument.offset;
        int pitch = midiPitch - instrumentList[0].offset;
        float volume = velocity / 127.0f;
        long time = microTime / 1000L;
        return new Note(instrumentList[0], shiftedInstrument, pitch, shiftedInstrumentPitch, midiPitch, volume, time, MidiConverter.getPosition(pan));
    }

    private static Note getMidiPercussionNote(int midiPitch, float velocity, long microTime, int pan) {
        if (!percussionMap.containsKey(midiPitch)) {
            return null;
        }
        int noteId = percussionMap.get(midiPitch);
        int pitch = noteId % 25;
        float volume = velocity / 127.0f;
        Instrument instrument = Instrument.fromId(noteId / 25);
        long time = microTime / 1000L;
        return new Note(instrument, pitch, midiPitch, volume, time, MidiConverter.getPosition(pan), false);
    }

    private static Vector3d getPosition(int pan) {
        return Vector3d.from(pan, 0.0f, 0.0f);
    }

    private static String decodeStringWithUTF8OrShiftJIS(byte[] bytes) {
        CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder();
        utf8Decoder.onMalformedInput(CodingErrorAction.REPORT);
        utf8Decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return utf8Decoder.decode(ByteBuffer.wrap(bytes)).toString();
        }
        catch (CharacterCodingException e) {
            return new String(bytes, Charset.forName("Shift_JIS"));
        }
    }

    static {
        instrumentMap.put(0, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(1, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(2, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(3, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(4, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(5, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(6, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(7, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(8, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(9, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(10, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(11, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(12, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(13, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(14, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(15, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(16, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(17, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(18, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(19, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(20, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(21, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(22, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(23, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(24, new Instrument[]{Instrument.GUITAR, Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(25, new Instrument[]{Instrument.GUITAR, Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(26, new Instrument[]{Instrument.GUITAR, Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(27, new Instrument[]{Instrument.GUITAR, Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(28, new Instrument[]{Instrument.GUITAR, Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(29, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(30, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(31, new Instrument[]{Instrument.GUITAR, Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(32, new Instrument[]{Instrument.BASS, Instrument.HARP, Instrument.BELL});
        instrumentMap.put(33, new Instrument[]{Instrument.BASS, Instrument.HARP, Instrument.BELL});
        instrumentMap.put(34, new Instrument[]{Instrument.BASS, Instrument.HARP, Instrument.BELL});
        instrumentMap.put(35, new Instrument[]{Instrument.BASS, Instrument.HARP, Instrument.BELL});
        instrumentMap.put(36, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(37, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(38, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(39, new Instrument[]{Instrument.DIDGERIDOO, Instrument.BIT, Instrument.XYLOPHONE});
        instrumentMap.put(40, new Instrument[]{Instrument.FLUTE, Instrument.GUITAR, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(41, new Instrument[]{Instrument.FLUTE, Instrument.GUITAR, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(42, new Instrument[]{Instrument.FLUTE, Instrument.GUITAR, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(43, new Instrument[]{Instrument.FLUTE, Instrument.GUITAR, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(44, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(45, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(46, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.CHIME});
        instrumentMap.put(47, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(48, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(49, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(50, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(51, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(52, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(53, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(54, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(55, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(56, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(57, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(58, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(59, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(60, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(61, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(62, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(63, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(64, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(65, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(66, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(67, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(68, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(69, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(70, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(71, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(72, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(73, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(74, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(75, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(76, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(77, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(78, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(79, new Instrument[]{Instrument.FLUTE, Instrument.DIDGERIDOO, Instrument.IRON_XYLOPHONE, Instrument.BELL});
        instrumentMap.put(80, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(81, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(82, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(83, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(84, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(85, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(86, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(87, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(88, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(89, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(90, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(91, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(92, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(93, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(94, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(95, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(96, new Instrument[]{Instrument.FLUTE, Instrument.FLUTE, Instrument.FLUTE});
        instrumentMap.put(97, new Instrument[]{Instrument.CHIME, Instrument.CHIME, Instrument.CHIME});
        instrumentMap.put(98, new Instrument[]{Instrument.BIT, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(99, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(100, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(101, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(102, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(103, new Instrument[]{Instrument.HARP, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(104, new Instrument[]{Instrument.BANJO, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(105, new Instrument[]{Instrument.BANJO, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(106, new Instrument[]{Instrument.BANJO, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(107, new Instrument[]{Instrument.BANJO, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(108, new Instrument[]{Instrument.BANJO, Instrument.BASS, Instrument.BELL});
        instrumentMap.put(109, new Instrument[]{Instrument.HARP, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(110, new Instrument[]{Instrument.HARP, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(111, new Instrument[]{Instrument.HARP, Instrument.DIDGERIDOO, Instrument.BELL});
        instrumentMap.put(112, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(113, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(114, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(115, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(116, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(117, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(118, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        instrumentMap.put(119, new Instrument[]{Instrument.IRON_XYLOPHONE, Instrument.BASS, Instrument.XYLOPHONE});
        percussionMap = new HashMap();
        percussionMap.put(35, 10 + 25 * Instrument.BASEDRUM.id);
        percussionMap.put(36, 6 + 25 * Instrument.BASEDRUM.id);
        percussionMap.put(37, 6 + 25 * Instrument.HAT.id);
        percussionMap.put(38, 8 + 25 * Instrument.SNARE.id);
        percussionMap.put(39, 6 + 25 * Instrument.HAT.id);
        percussionMap.put(40, 4 + 25 * Instrument.SNARE.id);
        percussionMap.put(41, 6 + 25 * Instrument.BASEDRUM.id);
        percussionMap.put(42, 22 + 25 * Instrument.SNARE.id);
        percussionMap.put(43, 13 + 25 * Instrument.BASEDRUM.id);
        percussionMap.put(44, 22 + 25 * Instrument.SNARE.id);
        percussionMap.put(45, 15 + 25 * Instrument.BASEDRUM.id);
        percussionMap.put(46, 18 + 25 * Instrument.SNARE.id);
        percussionMap.put(47, 20 + 25 * Instrument.BASEDRUM.id);
        percussionMap.put(48, 23 + 25 * Instrument.BASEDRUM.id);
        percussionMap.put(49, 17 + 25 * Instrument.SNARE.id);
        percussionMap.put(50, 23 + 25 * Instrument.BASEDRUM.id);
        percussionMap.put(51, 24 + 25 * Instrument.SNARE.id);
        percussionMap.put(52, 8 + 25 * Instrument.SNARE.id);
        percussionMap.put(53, 13 + 25 * Instrument.SNARE.id);
        percussionMap.put(54, 18 + 25 * Instrument.HAT.id);
        percussionMap.put(55, 18 + 25 * Instrument.SNARE.id);
        percussionMap.put(56, 1 + 25 * Instrument.HAT.id);
        percussionMap.put(57, 13 + 25 * Instrument.SNARE.id);
        percussionMap.put(58, 2 + 25 * Instrument.HAT.id);
        percussionMap.put(59, 13 + 25 * Instrument.SNARE.id);
        percussionMap.put(60, 9 + 25 * Instrument.HAT.id);
        percussionMap.put(61, 2 + 25 * Instrument.HAT.id);
        percussionMap.put(62, 8 + 25 * Instrument.HAT.id);
        percussionMap.put(63, 22 + 25 * Instrument.BASEDRUM.id);
        percussionMap.put(64, 15 + 25 * Instrument.BASEDRUM.id);
        percussionMap.put(65, 13 + 25 * Instrument.SNARE.id);
        percussionMap.put(66, 8 + 25 * Instrument.SNARE.id);
        percussionMap.put(67, 8 + 25 * Instrument.HAT.id);
        percussionMap.put(68, 3 + 25 * Instrument.HAT.id);
        percussionMap.put(69, 20 + 25 * Instrument.HAT.id);
        percussionMap.put(70, 23 + 25 * Instrument.HAT.id);
        percussionMap.put(71, 24 + 25 * Instrument.HAT.id);
        percussionMap.put(72, 24 + 25 * Instrument.HAT.id);
        percussionMap.put(73, 17 + 25 * Instrument.HAT.id);
        percussionMap.put(74, 11 + 25 * Instrument.HAT.id);
        percussionMap.put(75, 18 + 25 * Instrument.HAT.id);
        percussionMap.put(76, 9 + 25 * Instrument.HAT.id);
        percussionMap.put(77, 5 + 25 * Instrument.HAT.id);
        percussionMap.put(78, 22 + 25 * Instrument.HAT.id);
        percussionMap.put(79, 19 + 25 * Instrument.SNARE.id);
        percussionMap.put(80, 17 + 25 * Instrument.HAT.id);
        percussionMap.put(81, 22 + 25 * Instrument.HAT.id);
        percussionMap.put(82, 22 + 25 * Instrument.SNARE.id);
        percussionMap.put(83, 24 + 25 * Instrument.CHIME.id);
        percussionMap.put(84, 24 + 25 * Instrument.CHIME.id);
        percussionMap.put(85, 21 + 25 * Instrument.HAT.id);
        percussionMap.put(86, 14 + 25 * Instrument.BASEDRUM.id);
        percussionMap.put(87, 7 + 25 * Instrument.BASEDRUM.id);
    }
}

