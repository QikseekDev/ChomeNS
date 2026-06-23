/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.song;

import it.unimi.dsi.fastutil.objects.ObjectList;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.song.Converter;
import me.chayapak1.chomens_bot.song.MidiConverter;
import me.chayapak1.chomens_bot.song.NBSConverter;
import me.chayapak1.chomens_bot.song.Song;
import me.chayapak1.chomens_bot.song.SongLoaderException;
import me.chayapak1.chomens_bot.song.SongPlayerConverter;
import me.chayapak1.chomens_bot.song.TextFileConverter;
import me.chayapak1.chomens_bot.util.DownloadUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class SongLoaderThread
extends Thread {
    public static final List<Converter> converters = ObjectList.of(new Converter[]{new MidiConverter(), new NBSConverter(), new TextFileConverter(), new SongPlayerConverter()});
    public final String fileName;
    private Path songPath;
    private URL songUrl;
    public SongLoaderException exception;
    public Song song;
    private final Bot bot;
    public final CommandContext context;
    private final boolean isUrl;
    private byte[] data;
    private boolean isItem = false;
    private boolean isFolder = false;

    public SongLoaderThread(URL location, Bot bot, CommandContext context) {
        this.bot = bot;
        this.context = context;
        this.isUrl = true;
        this.songUrl = location;
        this.fileName = location.getFile();
        this.updateName();
    }

    public SongLoaderThread(Path location, Bot bot, CommandContext context) {
        this.bot = bot;
        this.context = context;
        this.isUrl = false;
        this.songPath = location;
        this.isFolder = Files.isDirectory(this.songPath, new LinkOption[0]);
        this.fileName = location.getFileName().toString();
        this.updateName();
    }

    public SongLoaderThread(byte[] data, Bot bot, CommandContext context) {
        this.bot = bot;
        this.context = context;
        this.data = data;
        this.isItem = true;
        this.isUrl = false;
        this.fileName = context.sender.profile.getName() + "'s song item";
        this.updateName();
    }

    private void updateName() {
        this.setName("SongLoaderThread for " + this.fileName);
    }

    @Override
    public void run() {
        if (this.isFolder && !this.isUrl && !this.isItem) {
            try (Stream<Path> files = Files.list(this.songPath);){
                files.forEach(file -> {
                    this.songPath = file;
                    this.processFile();
                });
                this.showAddedToQueue();
            }
            catch (IOException e) {
                this.bot.logger.error(e);
            }
        } else {
            this.processFile();
        }
    }

    private void processFile() {
        Object name;
        byte[] bytes;
        if (this.bot.music.songQueue.size() > 100) {
            return;
        }
        try {
            if (this.isUrl) {
                bytes = DownloadUtilities.DownloadToByteArray(this.songUrl, 0xA00000);
                Path fileName = Paths.get(this.songUrl.toURI().getPath(), new String[0]).getFileName();
                name = fileName == null ? "(root)" : fileName.toString();
            } else if (this.isItem) {
                bytes = this.data;
                name = this.context.sender.profile.getName() + "'s song item";
            } else {
                bytes = Files.readAllBytes(this.songPath);
                name = !this.isFolder ? this.fileName : this.songPath.getFileName().toString();
            }
        }
        catch (Exception e) {
            this.exception = new SongLoaderException(Component.text(e.getMessage()));
            this.failed();
            return;
        }
        for (Converter converter : converters) {
            if (this.song != null && !this.isFolder) break;
            try {
                this.song = converter.getSongFromBytes(bytes, (String)name, this.bot);
            }
            catch (Exception exception) {}
        }
        if (this.song == null) {
            this.exception = new SongLoaderException(Component.translatable("commands.music.error.invalid_format"));
            this.failed();
        } else {
            this.song.context = this.context;
            this.bot.music.songQueue.add(this.song);
            if (!this.isFolder) {
                this.showAddedToQueue();
            }
        }
        this.bot.music.loaderThread = null;
    }

    private void showAddedToQueue() {
        if (this.isFolder) {
            this.bot.music.sendOutput(this.context, Component.translatable("commands.music.loading.added_folder_to_queue", this.bot.colorPalette.defaultColor));
        } else {
            this.bot.music.sendOutput(this.context, Component.translatable("commands.music.loading.added_song_to_queue", this.bot.colorPalette.defaultColor, Component.empty().append(Component.text(this.song.name, this.bot.colorPalette.secondary))));
        }
    }

    private void failed() {
        this.bot.music.sendOutput(this.context, Component.translatable("commands.music.error.loading_failed", (TextColor)NamedTextColor.RED, this.exception.message));
        this.bot.music.loaderThread = null;
    }
}

