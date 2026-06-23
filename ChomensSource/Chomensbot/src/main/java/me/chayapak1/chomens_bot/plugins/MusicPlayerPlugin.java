/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.contexts.ChomeNSModCommandContext;
import me.chayapak1.chomens_bot.command.contexts.PlayerCommandContext;
import me.chayapak1.chomens_bot.data.bossbar.BotBossBar;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.song.Loop;
import me.chayapak1.chomens_bot.song.Note;
import me.chayapak1.chomens_bot.song.Song;
import me.chayapak1.chomens_bot.song.SongLoaderThread;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import me.chayapak1.chomens_bot.util.MathUtilities;
import me.chayapak1.chomens_bot.util.UUIDUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.cloudburstmc.math.vector.Vector3d;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.protocol.data.game.BossBarColor;
import org.geysermc.mcprotocollib.protocol.data.game.BossBarDivision;

public class MusicPlayerPlugin
implements Listener {
    public static final String SELECTOR = "@a[tag=%s,tag=!custompitch]";
    public static final String CUSTOM_PITCH_SELECTOR = "@a[tag=%s,tag=custompitch]";
    public static final String BOTH_SELECTOR = "@a[tag=%s]";
    public static final Path SONG_DIR = Path.of("songs", new String[0]);
    private static final String BOSS_BAR_NAME = "music";
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");
    private final Bot bot;
    public final String musicTag;
    public Song currentSong;
    public final List<Song> songQueue = Collections.synchronizedList(new ObjectArrayList());
    public SongLoaderThread loaderThread = null;
    public Loop loop = Loop.OFF;
    public double pitch = 0.0;
    public double speed = 1.0;
    public float volume = 0.0f;
    public int amplify = 1;
    public String instrument = "off";
    public boolean rainbow = false;
    private float rainbowHue = 0.0f;
    public BossBarColor bossBarColor = BossBarColor.YELLOW;
    private int urlLimit = 0;
    public boolean locked = false;
    private boolean isStopping = false;
    public String currentLyrics = "";

    public MusicPlayerPlugin(Bot bot) {
        this.bot = bot;
        this.musicTag = bot.config.namespace + "_music";
        bot.listener.addListener(this);
        bot.executor.scheduleAtFixedRate(() -> {
            try {
                this.checkTick();
            }
            catch (Exception e) {
                bot.logger.error(e);
            }
        }, 0L, 100L, TimeUnit.MILLISECONDS);
        bot.executor.scheduleAtFixedRate(() -> {
            try {
                if (this.currentSong == null || this.currentSong.paused || this.isStopping || bot.core.isRateLimited()) {
                    return;
                }
                this.handlePlaying();
            }
            catch (Exception e) {
                bot.logger.error(e);
            }
        }, 0L, 25L, TimeUnit.MILLISECONDS);
        bot.executor.scheduleAtFixedRate(() -> {
            this.urlLimit = 0;
        }, 0L, bot.config.music.urlRatelimit.seconds, TimeUnit.SECONDS);
    }

    public void loadSong(Path location, CommandContext context) {
        this.startLoadingSong(location.getFileName().toString(), new SongLoaderThread(location, this.bot, context));
    }

    public void loadSong(byte[] data, CommandContext context) {
        this.startLoadingSong(context.sender.profile.getName() + "'s song item", new SongLoaderThread(data, this.bot, context));
    }

    public void loadSong(URL location, CommandContext context) {
        if (this.urlLimit >= this.bot.config.music.urlRatelimit.limit) {
            this.sendOutput(context, Component.translatable("commands.music.error.url_ratelimited", (TextColor)NamedTextColor.RED));
            return;
        }
        ++this.urlLimit;
        this.startLoadingSong(location.toString(), new SongLoaderThread(location, this.bot, context));
    }

    private void startLoadingSong(String songName, SongLoaderThread loaderThread) {
        if (this.songQueue.size() > 500) {
            return;
        }
        this.loaderThread = loaderThread;
        this.sendOutput(loaderThread.context, Component.translatable("commands.music.loading", this.bot.colorPalette.defaultColor, Component.text(songName, this.bot.colorPalette.secondary)));
        this.loaderThread.start();
        if (loaderThread.context instanceof PlayerCommandContext || loaderThread.context instanceof ChomeNSModCommandContext) {
            this.addTag(loaderThread.context.sender.profile.getId());
        }
    }

    public void sendOutput(CommandContext context, Component component) {
        if (context instanceof PlayerCommandContext) {
            PlayerCommandContext playerContext = (PlayerCommandContext)context;
            playerContext.sendOutput(component, true);
        } else {
            context.sendOutput(component);
        }
    }

    public void addTag(UUID uuid) {
        this.bot.core.run(String.format("minecraft:tag %s add %s", UUIDUtilities.selector(uuid), this.musicTag));
    }

    public void removeTag(UUID uuid) {
        this.bot.core.run(String.format("minecraft:tag %s remove %s", UUIDUtilities.selector(uuid), this.musicTag));
    }

    @Override
    public void onCoreReady() {
        if (this.currentSong != null) {
            this.currentSong.play();
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        if (this.currentSong != null) {
            this.currentSong.pause();
        }
        this.loaderThread = null;
    }

    private void checkTick() {
        if (!this.bot.loggedIn) {
            return;
        }
        if (this.currentSong == null) {
            if (this.songQueue.isEmpty()) {
                return;
            }
            this.currentSong = this.songQueue.getFirst();
            this.sendOutput(this.currentSong.context, Component.translatable("commands.music.nowplaying", this.bot.colorPalette.defaultColor, Component.empty().append(Component.text(this.currentSong.name, this.bot.colorPalette.secondary))));
            this.currentSong.play();
            this.addBossBar();
        }
        if (this.isStopping) {
            this.currentSong = null;
            this.isStopping = false;
        } else if (!this.currentSong.finished()) {
            this.handleLyrics();
            BotBossBar bossBar = this.bot.bossbar.get(BOSS_BAR_NAME);
            if (bossBar == null) {
                bossBar = this.addBossBar();
            }
            if (bossBar != null && !bossBar.gotSecret) {
                this.addTag(this.bot.profile.getId());
            }
            if (bossBar != null && this.bot.options.useCore) {
                bossBar.setTitle(this.generateBossBar());
                bossBar.setColor(this.bossBarColor);
                bossBar.setValue((int)Math.floor(this.currentSong.time / this.speed / 1000.0));
                bossBar.setMax((long)(this.currentSong.length / this.speed) / 1000L);
            }
        } else {
            this.currentLyrics = "";
            if (this.loop == Loop.CURRENT) {
                this.currentSong.loop();
                return;
            }
            this.sendOutput(this.currentSong.context, Component.translatable("commands.music.finished", this.bot.colorPalette.defaultColor, Component.empty().append(Component.text(this.currentSong.name, this.bot.colorPalette.secondary))));
            if (this.loop == Loop.ALL) {
                this.skip();
                return;
            }
            this.songQueue.removeFirst();
            if (this.songQueue.isEmpty()) {
                this.stopPlaying();
                return;
            }
            if (this.currentSong.size() > 0) {
                this.currentSong = this.songQueue.getFirst();
                this.currentSong.setTime(0.0);
                this.currentSong.play();
            }
        }
    }

    public void skip() {
        if (this.loop == Loop.ALL) {
            this.songQueue.add(this.songQueue.removeFirst());
        } else {
            this.songQueue.removeFirst();
        }
        if (this.songQueue.isEmpty()) {
            this.stopPlaying();
            return;
        }
        this.currentSong = this.songQueue.getFirst();
        this.currentSong.setTime(0.0);
        this.currentSong.play();
    }

    public BotBossBar addBossBar() {
        this.rainbow = false;
        this.addTag(this.bot.profile.getId());
        BotBossBar bossBar = new BotBossBar(Component.empty(), String.format(BOTH_SELECTOR, this.musicTag), this.bossBarColor, BossBarDivision.NONE, true, (int)this.currentSong.length / 1000, 0, this.bot);
        this.bot.bossbar.add(BOSS_BAR_NAME, bossBar);
        return bossBar;
    }

    private void handleLyrics() {
    }

    public void removeBossBar() {
        BotBossBar bossBar = this.bot.bossbar.get(BOSS_BAR_NAME);
        if (bossBar != null) {
            bossBar.setTitle(Component.translatable("commands.music.error.not_playing"));
        }
        this.bot.bossbar.remove(BOSS_BAR_NAME);
    }

    public Component generateBossBar() {
        TextColor nameColor;
        if (this.rainbow) {
            int increment = 18;
            nameColor = TextColor.color(HSVLike.hsvLike(this.rainbowHue / 360.0f, 1.0f, 1.0f));
            this.rainbowHue = (this.rainbowHue + 18.0f) % 360.0f;
            this.bossBarColor = BossBarColor.YELLOW;
        } else if (this.pitch > 0.0) {
            nameColor = NamedTextColor.LIGHT_PURPLE;
            this.bossBarColor = BossBarColor.PURPLE;
        } else if (this.pitch < 0.0) {
            nameColor = NamedTextColor.AQUA;
            this.bossBarColor = BossBarColor.CYAN;
        } else {
            nameColor = NamedTextColor.GREEN;
            this.bossBarColor = BossBarColor.YELLOW;
        }
        TextComponent.Builder component = (TextComponent.Builder)((TextComponent.Builder)((TextComponent.Builder)Component.text().append(Component.empty().append(Component.text(this.currentSong.name, nameColor)))).append((Component)Component.text(" | ", (TextColor)NamedTextColor.DARK_GRAY))).append((Component)Component.translatable("%s / %s", (TextColor)NamedTextColor.DARK_GRAY, this.formatTime((long)(this.currentSong.time / this.speed)).colorIfAbsent(NamedTextColor.GRAY), this.formatTime((long)(this.currentSong.length / this.speed)).colorIfAbsent(NamedTextColor.GRAY)));
        if (!this.bot.core.hasRateLimit()) {
            ((TextComponent.Builder)component.append((Component)Component.text(" | ", (TextColor)NamedTextColor.DARK_GRAY))).append((Component)Component.translatable("%s / %s", (TextColor)NamedTextColor.DARK_GRAY, Component.text(FORMATTER.format(this.currentSong.position), (TextColor)NamedTextColor.GRAY), Component.text(FORMATTER.format(this.currentSong.size()), (TextColor)NamedTextColor.GRAY)));
            if (!this.currentLyrics.isBlank()) {
                ((TextComponent.Builder)component.append((Component)Component.text(" | ", (TextColor)NamedTextColor.DARK_GRAY))).append((Component)Component.text(this.currentLyrics, (TextColor)NamedTextColor.BLUE));
            }
        }
        if (this.currentSong.paused) {
            return ((TextComponent.Builder)((TextComponent.Builder)component.append((Component)Component.text(" | ", (TextColor)NamedTextColor.DARK_GRAY))).append((Component)Component.text("\u23f8", (TextColor)NamedTextColor.LIGHT_PURPLE))).build();
        }
        if (this.loop != Loop.OFF) {
            return ((TextComponent.Builder)((TextComponent.Builder)component.append((Component)Component.translatable(" | ", (TextColor)NamedTextColor.DARK_GRAY))).append((Component)Component.translatable("Looping " + (this.loop == Loop.CURRENT ? "current" : "all"), (TextColor)NamedTextColor.LIGHT_PURPLE))).build();
        }
        return component.build();
    }

    public Component formatTime(long millis) {
        int seconds = (int)millis / 1000;
        String minutePart = String.valueOf(seconds / 60);
        String unpaddedSecondPart = String.valueOf(seconds % 60);
        return Component.translatable("%s:%s", Component.text(minutePart), Component.text((String)(unpaddedSecondPart.length() < 2 ? "0" + unpaddedSecondPart : unpaddedSecondPart)));
    }

    public void stopPlaying() {
        this.removeBossBar();
        this.isStopping = true;
    }

    public void handlePlaying() {
        if (this.currentSong == null) {
            return;
        }
        this.currentSong.advanceTime();
        while (this.currentSong.reachedNextNote()) {
            Note note = this.currentSong.getNextNote();
            try {
                boolean shouldCustomPitch;
                boolean isMoreOrLessOctave;
                if (note.isRainbowToggle) {
                    this.rainbow = !this.rainbow;
                    continue;
                }
                double key = note.shiftedPitch;
                Vector3d blockPosition = note.position;
                double notShiftedFloatingPitch = 0.5 * Math.pow(2.0, (note.pitch + this.pitch / 10.0) / 12.0);
                boolean bl = isMoreOrLessOctave = (key += 33.0) < 33.0 || key > 57.0;
                boolean bl2 = this.currentSong.nbs ? isMoreOrLessOctave : (shouldCustomPitch = note.pitch != note.shiftedPitch || note.shiftedInstrument != note.instrument);
                double volume = note.volume + this.volume;
                if (volume == 0.0) continue;
                if (shouldCustomPitch) {
                    this.bot.core.run("minecraft:execute as " + String.format(CUSTOM_PITCH_SELECTOR, this.musicTag) + " at @s run playsound " + (!this.instrument.equals("off") ? this.instrument : note.instrument.sound) + ".pitch." + notShiftedFloatingPitch + " record @s ^" + blockPosition.getX() + " ^" + blockPosition.getY() + " ^" + blockPosition.getZ() + " " + volume + " 0");
                }
                while (key < 33.0) {
                    key += 12.0;
                }
                while (key > 57.0) {
                    key -= 12.0;
                }
                double floatingPitch = 0.5 * Math.pow(2.0, ((key -= 33.0) + this.pitch / 10.0) / 12.0);
                for (int i = 0; i < this.amplify; ++i) {
                    this.bot.core.run("minecraft:execute as " + String.format(shouldCustomPitch ? SELECTOR : BOTH_SELECTOR, this.musicTag) + " at @s run playsound " + (!this.instrument.equals("off") ? this.instrument : note.shiftedInstrument.sound) + " record @s ^" + blockPosition.getX() + " ^" + blockPosition.getY() + " ^" + blockPosition.getZ() + " " + volume + " " + MathUtilities.clamp(floatingPitch, 0.0, 2.0));
                }
            }
            catch (Exception e) {
                this.bot.logger.error(e);
            }
        }
    }

    static {
        try {
            if (!Files.exists(SONG_DIR, new LinkOption[0])) {
                Files.createDirectory(SONG_DIR, new FileAttribute[0]);
            }
        }
        catch (IOException e) {
            LoggerUtilities.error(e);
        }
    }
}

