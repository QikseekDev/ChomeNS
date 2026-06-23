/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.song;

import me.chayapak1.chomens_bot.song.Instrument;
import org.cloudburstmc.math.vector.Vector3d;

public class Note
implements Comparable<Note> {
    public final Instrument instrument;
    public final Instrument shiftedInstrument;
    public final double pitch;
    public final double shiftedPitch;
    public final double originalPitch;
    public final float volume;
    public final double time;
    public final Vector3d position;
    public final boolean isRainbowToggle;

    public Note(Instrument instrument, double pitch, double originalPitch, float volume, double time, Vector3d position, boolean isRainbowToggle) {
        this.shiftedInstrument = this.instrument = instrument;
        this.shiftedPitch = this.pitch = pitch;
        this.originalPitch = originalPitch;
        this.volume = volume;
        this.time = time;
        this.position = position;
        this.isRainbowToggle = isRainbowToggle;
    }

    public Note(Instrument instrument, Instrument shiftedInstrument, double pitch, double shiftedPitch, double originalPitch, float volume, double time, Vector3d position) {
        this.instrument = instrument;
        this.shiftedInstrument = shiftedInstrument;
        this.pitch = pitch;
        this.shiftedPitch = shiftedPitch;
        this.originalPitch = originalPitch;
        this.volume = volume;
        this.time = time;
        this.position = position;
        this.isRainbowToggle = false;
    }

    @Override
    public int compareTo(Note other) {
        return Double.compare(this.time, other.time);
    }
}

