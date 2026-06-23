/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.song;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.song.Loop;
import me.chayapak1.chomens_bot.song.Note;
import me.chayapak1.chomens_bot.util.StringUtilities;

public class Song {
    public final List<Note> notes = new ObjectArrayList<Note>();
    public final String originalName;
    public String name;
    public CommandContext context = null;
    public int position = 0;
    public boolean paused = true;
    public double startTime = 0.0;
    public double length = 0.0;
    public double time = 0.0;
    public long loopPosition = 0L;
    public final Map<Long, String> lyrics = new Object2ObjectOpenHashMap<Long, String>();
    public String songName;
    public String songAuthor;
    public String songOriginalAuthor;
    public String songDescription;
    public final String tracks;
    private final Bot bot;
    public final boolean nbs;

    public Song(String originalName, Bot bot, String songName, String songAuthor, String songOriginalAuthor, String songDescription, String tracks, boolean nbs) {
        this.originalName = originalName;
        this.bot = bot;
        this.songName = songName;
        this.songAuthor = songAuthor;
        this.songOriginalAuthor = songOriginalAuthor;
        this.songDescription = songDescription;
        this.tracks = tracks;
        this.nbs = nbs;
        this.updateName();
    }

    public void updateName() {
        String authorPart = null;
        if (StringUtilities.isNotNullAndNotBlank(this.songOriginalAuthor) && StringUtilities.isNotNullAndNotBlank(this.songAuthor)) {
            authorPart = String.format("%s/%s", this.songOriginalAuthor, this.songAuthor);
        } else if (StringUtilities.isNotNullAndNotBlank(this.songOriginalAuthor)) {
            authorPart = this.songOriginalAuthor;
        } else if (StringUtilities.isNotNullAndNotBlank(this.songAuthor)) {
            authorPart = this.songAuthor;
        }
        String namePart = StringUtilities.isNotNullAndNotBlank(this.songName) ? this.songName : this.originalName;
        this.name = authorPart != null ? String.format("%s - %s", authorPart, namePart) : namePart;
    }

    public Note get(int i) {
        return this.notes.get(i);
    }

    public void add(Note e) {
        this.notes.add(e);
    }

    public void sort() {
        Collections.sort(this.notes);
    }

    public void play() {
        if (this.paused) {
            if (this.loopPosition != 0L) {
                this.bot.music.loop = Loop.CURRENT;
            }
            this.paused = false;
            this.startTime = (double)System.currentTimeMillis() - this.time;
        }
    }

    public void pause() {
        if (!this.paused) {
            this.paused = true;
            this.advanceTime();
        }
    }

    public void setTime(double t2) {
        this.time = t2;
        this.startTime = (double)System.currentTimeMillis() - this.time;
        this.position = 0;
        while (this.position < this.notes.size() && this.notes.get((int)this.position).time / this.bot.music.speed <= t2) {
            ++this.position;
        }
    }

    public void advanceTime() {
        this.time = ((double)System.currentTimeMillis() - this.startTime) * this.bot.music.speed;
    }

    public boolean reachedNextNote() {
        if (this.position < this.notes.size()) {
            return this.notes.get((int)this.position).time / this.bot.music.speed <= this.time;
        }
        if (this.finished() && this.bot.music.loop != Loop.OFF) {
            if (this.position < this.notes.size()) {
                return this.notes.get((int)this.position).time / this.bot.music.speed <= this.time;
            }
            return false;
        }
        return false;
    }

    public void loop() {
        this.position = 0;
        this.startTime += this.length - (double)this.loopPosition;
        this.time -= this.length - (double)this.loopPosition;
        while (this.position < this.notes.size() && this.notes.get((int)this.position).time / this.bot.music.speed < (double)this.loopPosition) {
            ++this.position;
        }
    }

    public Note getNextNote() {
        if (this.position >= this.notes.size() && this.bot.music.loop == Loop.OFF) {
            return null;
        }
        return this.notes.get(this.position++);
    }

    public boolean finished() {
        return this.time > this.length || this.position >= this.size();
    }

    public int size() {
        return this.notes.size();
    }
}

