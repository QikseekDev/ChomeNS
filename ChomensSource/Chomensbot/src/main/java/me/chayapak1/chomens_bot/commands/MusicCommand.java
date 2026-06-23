/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.command.contexts.ConsoleCommandContext;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.plugins.MusicPlayerPlugin;
import me.chayapak1.chomens_bot.song.Instrument;
import me.chayapak1.chomens_bot.song.Loop;
import me.chayapak1.chomens_bot.song.Note;
import me.chayapak1.chomens_bot.song.Song;
import me.chayapak1.chomens_bot.util.Ascii85;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import me.chayapak1.chomens_bot.util.PathUtilities;
import me.chayapak1.chomens_bot.util.StringUtilities;
import me.chayapak1.chomens_bot.util.TimestampUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.cloudburstmc.math.vector.Vector3d;

public class MusicCommand
extends Command
implements Listener {
    private static final Path ROOT = MusicPlayerPlugin.SONG_DIR;
    private static final AtomicInteger commandsPerSecond = new AtomicInteger();

    public MusicCommand() {
        super("music", new String[]{"play <song|URL>", "playitem", "stop", "loop <current|all|off>", "list [directory]", "skip", "nowplaying", "queue", "goto <timestamp>", "pitch <pitch>", "speed <speed>", "volume <volume modifier>", "amplify <amplification>", "noteinstrument <instrument>", "pause", "resume", "info", "listen", "unlisten"}, new String[]{"song"}, TrustLevel.PUBLIC, false, new ChatPacketType[]{ChatPacketType.DISGUISED});
        Main.EXECUTOR.scheduleAtFixedRate(() -> commandsPerSecond.set(0), 0L, 1L, TimeUnit.SECONDS);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        String action;
        if (commandsPerSecond.get() > 3) {
            return null;
        }
        commandsPerSecond.getAndIncrement();
        if (context.bot.music.locked && !(context instanceof ConsoleCommandContext)) {
            throw new CommandException(Component.translatable("commands.music.error.locked"));
        }
        return switch (action = context.getAction()) {
            case "play", "playurl", "playnbs", "playnbsurl" -> this.play(context);
            case "playfromitem", "playitem", "playsongplayer" -> this.playFromItem(context);
            case "stop" -> this.stop(context);
            case "loop" -> this.loop(context);
            case "list" -> this.list(context);
            case "skip" -> this.skip(context);
            case "nowplaying" -> this.nowPlaying(context);
            case "queue" -> this.queue(context);
            case "goto" -> this.goTo(context);
            case "pitch" -> this.pitch(context);
            case "speed" -> this.speed(context);
            case "volume" -> this.volume(context);
            case "amplify" -> this.amplify(context);
            case "noteinstrument" -> this.noteInstrument(context);
            case "pause", "resume" -> this.pause(context);
            case "info" -> this.info(context);
            case "testsong" -> this.testSong(context);
            case "listen", "unmute" -> this.listen(context);
            case "unlisten", "mute" -> this.unlisten(context);
            default -> throw new CommandException(Component.translatable("commands.generic.error.invalid_action"));
        };
    }

    public Component play(CommandContext context) throws CommandException {
        block21: {
            MusicPlayerPlugin player = context.bot.music;
            if (player.loaderThread != null) {
                throw new CommandException(Component.translatable("commands.music.play.error.already_loading"));
            }
            String stringPath = context.getString(true, true);
            try {
                String stringFile;
                Path path;
                Path joinedPath = Path.of(ROOT.toString(), stringPath);
                if (joinedPath.toString().contains("http")) {
                    player.loadSong(new URI(stringPath).toURL(), context);
                    break block21;
                }
                if (!joinedPath.normalize().startsWith(ROOT.toString())) {
                    throw new CommandException(Component.text("no"));
                }
                String separator = FileSystems.getDefault().getSeparator();
                if (stringPath.contains(separator) && !stringPath.isEmpty()) {
                    String[] splitPath = stringPath.split(separator);
                    ObjectArrayList<String> splitPathClone = new ObjectArrayList<String>(Arrays.stream(splitPath).toList());
                    splitPathClone.removeLast();
                    path = Path.of(ROOT.toString(), String.join((CharSequence)separator, splitPathClone));
                    stringFile = splitPath[splitPath.length - 1];
                } else {
                    path = ROOT;
                    stringFile = stringPath;
                }
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(path);){
                    ObjectArrayList<Path> songsPaths = new ObjectArrayList<Path>();
                    for (Path path2 : stream) {
                        songsPaths.add(path2);
                    }
                    PathUtilities.sort(songsPaths);
                    ObjectArrayList songs = new ObjectArrayList();
                    for (Path eachPath : songsPaths) {
                        songs.add(eachPath.getFileName().toString());
                    }
                    String[] stringArray = (String[])songs.stream().filter(song -> song.equalsIgnoreCase(stringFile) || song.toLowerCase().contains(stringFile.toLowerCase())).toArray(String[]::new);
                    if (stringArray.length == 0) {
                        throw new CommandException(Component.translatable("commands.music.error.song_not_found"));
                    }
                    String file = stringArray[0];
                    player.loadSong(Path.of(path.toString(), file), context);
                }
                catch (NoSuchFileException e) {
                    throw new CommandException(Component.translatable("commands.music.error.no_directory"));
                }
            }
            catch (MalformedURLException e) {
                throw new CommandException(Component.translatable("commands.music.error.invalid_url"));
            }
            catch (IndexOutOfBoundsException e) {
                throw new CommandException(Component.translatable("commands.music.error.song_not_found"));
            }
            catch (CommandException e) {
                throw e;
            }
            catch (Exception e) {
                throw new CommandException(Component.text(e.toString()));
            }
        }
        return null;
    }

    public Component playFromItem(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        CompletableFuture<String> future = bot.query.entity(context.sender.profile.getIdAsString(), "SelectedItem.components.minecraft:custom_data.SongItemData.SongData");
        future.thenApply(output -> {
            if (output == null) {
                context.sendOutput(Component.translatable("commands.music.playitem.error.no_item_nbt", (TextColor)NamedTextColor.RED));
                return null;
            }
            try {
                bot.music.loadSong(Base64.getDecoder().decode((String)output), context);
            }
            catch (IllegalArgumentException e) {
                try {
                    bot.music.loadSong(Ascii85.decode(output), context);
                }
                catch (IllegalArgumentException e2) {
                    context.sendOutput(Component.translatable("commands.music.playitem.invalid_data", (TextColor)NamedTextColor.RED));
                }
            }
            return output;
        });
        return null;
    }

    public Component stop(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        bot.music.stopPlaying();
        bot.music.songQueue.clear();
        bot.music.loaderThread = null;
        return Component.translatable("commands.music.stop", bot.colorPalette.defaultColor);
    }

    public Component loop(CommandContext context) throws CommandException {
        Loop loop;
        context.checkOverloadArgs(2);
        Bot bot = context.bot;
        bot.music.loop = loop = context.getEnum(true, Loop.class);
        switch (loop) {
            case OFF: {
                return Component.translatable("commands.music.loop.off", bot.colorPalette.defaultColor, Component.translatable("commands.music.loop.off.disabled", (TextColor)NamedTextColor.RED));
            }
            case CURRENT: {
                if (bot.music.currentSong != null) {
                    return Component.translatable("commands.music.loop.current.with_song", bot.colorPalette.defaultColor, Component.text(bot.music.currentSong.name, bot.colorPalette.secondary));
                }
                return Component.translatable("commands.music.loop.current.without_song", bot.colorPalette.defaultColor);
            }
            case ALL: {
                return Component.translatable("commands.music.loop.all", bot.colorPalette.defaultColor);
            }
        }
        return null;
    }

    public Component list(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        String prefix = context.prefix;
        String stringPathIfExists = context.getString(true, false);
        Path path = Path.of(ROOT.toString(), stringPathIfExists);
        if (!path.normalize().startsWith(ROOT.toString())) {
            throw new CommandException(Component.text("no"));
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path);){
            ObjectArrayList<Path> paths = new ObjectArrayList<Path>();
            for (Path eachPath : stream) {
                paths.add(eachPath);
            }
            PathUtilities.sort(paths);
            ObjectArrayList fullList = new ObjectArrayList();
            int i = 0;
            for (Path eachPath : paths) {
                Path location;
                boolean isDirectory = Files.isDirectory(eachPath, new LinkOption[0]);
                try {
                    location = path;
                }
                catch (IllegalArgumentException e) {
                    location = Paths.get("", new String[0]);
                }
                String joinedPath = location.equals(ROOT) ? eachPath.getFileName().toString() : eachPath.toAbsolutePath().toString().replace(ROOT.toAbsolutePath().toString(), "").substring(1);
                fullList.add(Component.text(eachPath.getFileName().toString(), (i++ & 1) == 0 ? bot.colorPalette.primary : bot.colorPalette.secondary).clickEvent(ClickEvent.suggestCommand(prefix + this.name + (isDirectory ? " list " : " play ") + joinedPath)));
            }
            int eachSize = 100;
            for (int index = 0; index <= fullList.size(); index += 100) {
                List list = new ObjectArrayList(fullList).subList(index, Math.min(index + 100, fullList.size()));
                Component component = Component.join(JoinConfiguration.separator(Component.space()), (Iterable<? extends ComponentLike>)list);
                context.sendOutput(component);
                list.clear();
            }
        }
        catch (IOException e) {
            throw new CommandException(Component.translatable("commands.music.error.no_directory"));
        }
        return null;
    }

    public Component skip(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        MusicPlayerPlugin music = bot.music;
        if (music.currentSong == null) {
            throw new CommandException(Component.translatable("commands.music.error.not_playing"));
        }
        String name = music.currentSong.name;
        music.skip();
        return Component.translatable("commands.music.skip", bot.colorPalette.defaultColor, Component.text(name, bot.colorPalette.secondary));
    }

    public Component nowPlaying(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        Song song = bot.music.currentSong;
        if (song == null) {
            throw new CommandException(Component.translatable("commands.music.error.not_playing"));
        }
        return Component.translatable("commands.music.nowplaying", bot.colorPalette.defaultColor, Component.text(song.name, bot.colorPalette.secondary));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Component queue(CommandContext context) throws CommandException {
        List<Song> queue;
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        List<Song> list = queue = bot.music.songQueue;
        synchronized (list) {
            ObjectArrayList queueWithNames = new ObjectArrayList();
            int i = 0;
            for (Song song : queue) {
                queueWithNames.add(Component.text(song.name, (i++ & 1) == 0 ? bot.colorPalette.primary : bot.colorPalette.secondary));
            }
            return Component.translatable("commands.music.queue", (TextColor)NamedTextColor.GREEN, Component.join(JoinConfiguration.commas(true), queueWithNames));
        }
    }

    public Component listen(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        bot.music.addTag(context.sender.profile.getId());
        bot.bossbar.refreshPlayers();
        return Component.translatable("commands.music.listen", bot.colorPalette.defaultColor);
    }

    public Component unlisten(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        bot.music.removeTag(context.sender.profile.getId());
        bot.bossbar.refreshPlayers();
        return Component.translatable("commands.music.unlisten", bot.colorPalette.defaultColor);
    }

    public Component goTo(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        Song currentSong = bot.music.currentSong;
        String input = context.getString(true, true);
        long timestamp = TimestampUtilities.parseTimestamp(input);
        if (currentSong == null) {
            throw new CommandException(Component.translatable("commands.music.error.not_playing"));
        }
        if (timestamp < 0L || (double)timestamp > currentSong.length / bot.music.speed) {
            throw new CommandException(Component.translatable("commands.music.goto.error.invalid_timestamp"));
        }
        currentSong.setTime((double)timestamp / bot.music.speed);
        return Component.translatable("commands.music.goto", bot.colorPalette.defaultColor, Component.text(input, bot.colorPalette.number));
    }

    public Component pitch(CommandContext context) throws CommandException {
        context.checkOverloadArgs(2);
        Bot bot = context.bot;
        float pitch = context.getFloat(true, false).floatValue();
        bot.music.pitch = pitch;
        return Component.translatable("commands.music.pitch", bot.colorPalette.defaultColor, Component.text(pitch, bot.colorPalette.number));
    }

    public Component speed(CommandContext context) throws CommandException {
        context.checkOverloadArgs(2);
        Bot bot = context.bot;
        Song currentSong = bot.music.currentSong;
        double speed = context.getDouble(true, false);
        if (speed > 5.0) {
            throw new CommandException(Component.translatable("commands.music.speed.error.too_fast"));
        }
        if (speed < 0.0) {
            throw new CommandException(Component.translatable("commands.music.speed.error.negative"));
        }
        double oldTime = -1.0;
        if (currentSong != null) {
            oldTime = currentSong.time / speed;
        }
        bot.music.speed = speed;
        if (currentSong != null) {
            currentSong.setTime(oldTime);
        }
        return Component.translatable("commands.music.speed", bot.colorPalette.defaultColor, Component.text(speed, bot.colorPalette.number));
    }

    public Component volume(CommandContext context) throws CommandException {
        float volume;
        context.checkOverloadArgs(2);
        Bot bot = context.bot;
        bot.music.volume = volume = context.getFloat(true, false).floatValue();
        return Component.translatable("commands.music.volume", bot.colorPalette.defaultColor, Component.text(volume, bot.colorPalette.number));
    }

    public Component amplify(CommandContext context) throws CommandException {
        context.checkOverloadArgs(2);
        Bot bot = context.bot;
        int amplify = context.getInteger(true);
        if (amplify > 8) {
            throw new CommandException(Component.translatable("commands.music.amplify.error.too_big_value"));
        }
        if (amplify < 0) {
            throw new CommandException(Component.translatable("commands.music.amplify.error.negative"));
        }
        bot.music.amplify = amplify;
        return Component.translatable("commands.music.amplify", bot.colorPalette.defaultColor, Component.text(amplify, bot.colorPalette.number));
    }

    public Component noteInstrument(CommandContext context) throws CommandException {
        String instrument;
        Bot bot = context.bot;
        bot.music.instrument = instrument = context.getString(true, true);
        if (instrument.equalsIgnoreCase("off")) {
            return Component.translatable("commands.music.noteinstrument.off", bot.colorPalette.defaultColor);
        }
        return Component.translatable("commands.music.noteinstrument.set", bot.colorPalette.defaultColor, Component.text(instrument));
    }

    public Component pause(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        Song currentSong = bot.music.currentSong;
        if (currentSong == null) {
            throw new CommandException(Component.translatable("commands.music.error.not_playing"));
        }
        if (currentSong.paused) {
            currentSong.play();
            return Component.translatable("commands.music.resumed", bot.colorPalette.defaultColor);
        }
        currentSong.pause();
        return Component.translatable("commands.music.paused", bot.colorPalette.defaultColor);
    }

    public Component info(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        Song currentSong = bot.music.currentSong;
        if (currentSong == null) {
            throw new CommandException(Component.translatable("commands.music.error.not_playing"));
        }
        ObjectArrayList components = new ObjectArrayList();
        TextColor keyColor = bot.colorPalette.secondary;
        TextColor valueColor = bot.colorPalette.string;
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedNotesCount = formatter.format(currentSong.size());
        if (StringUtilities.isNotNullAndNotBlank(currentSong.name)) {
            components.add(Component.translatable("commands.music.info.title", keyColor, Component.text(currentSong.name, valueColor)));
        }
        if (currentSong.context != null && StringUtilities.isNotNullAndNotBlank(currentSong.context.sender.profile.getName())) {
            components.add(Component.translatable("commands.music.info.requester", keyColor, Component.text(currentSong.context.sender.profile.getName(), valueColor)));
        }
        if (StringUtilities.isNotNullAndNotBlank(currentSong.songAuthor)) {
            components.add(Component.translatable("commands.music.info.author", keyColor, Component.text(currentSong.songAuthor, valueColor)));
        }
        if (StringUtilities.isNotNullAndNotBlank(currentSong.songOriginalAuthor)) {
            components.add(Component.translatable("commands.music.info.original_author", keyColor, Component.text(currentSong.songOriginalAuthor, valueColor)));
        }
        if (StringUtilities.isNotNullAndNotBlank(currentSong.tracks)) {
            components.add(Component.translatable("commands.music.info.tracks", keyColor, Component.text(currentSong.tracks, valueColor)));
        }
        components.add(Component.translatable("commands.music.info.notes", keyColor, Component.text(formattedNotesCount, valueColor)));
        if (StringUtilities.isNotNullAndNotBlank(currentSong.songDescription)) {
            components.add(Component.translatable("commands.music.info.description", keyColor, Component.text(currentSong.songDescription, valueColor)));
        }
        return Component.join(JoinConfiguration.newlines(), components);
    }

    public Component testSong(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        Song song = new Song("test_song", bot, I18nUtilities.get("commands.music.testsong.title"), "chayapak", "hhhzzzsss", I18nUtilities.get("commands.music.testsong.description"), null, false);
        song.context = context;
        int instrumentId = 0;
        int j = 0;
        for (int i = 0; i < 400; ++i) {
            song.add(new Note(Instrument.fromId(instrumentId), ++j, j, 1.0f, i * 50, Vector3d.ZERO, false));
            if (j <= 15) continue;
            if (++instrumentId > 15) {
                instrumentId = 0;
            }
            j = 0;
        }
        song.length = 20000.0;
        bot.music.songQueue.add(song);
        bot.music.addTag(context.sender.profile.getId());
        return Component.translatable("commands.music.testsong.output", bot.colorPalette.defaultColor);
    }
}

