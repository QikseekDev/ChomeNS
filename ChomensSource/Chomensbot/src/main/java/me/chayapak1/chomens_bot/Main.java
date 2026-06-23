/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Configuration;
import me.chayapak1.chomens_bot.plugins.ConsolePlugin;
import me.chayapak1.chomens_bot.plugins.DatabasePlugin;
import me.chayapak1.chomens_bot.plugins.DiscordPlugin;
import me.chayapak1.chomens_bot.plugins.EvalPlugin;
import me.chayapak1.chomens_bot.plugins.IRCPlugin;
import me.chayapak1.chomens_bot.util.ArrayUtilities;
import me.chayapak1.chomens_bot.util.FileLoggerUtilities;
import me.chayapak1.chomens_bot.util.HashingUtilities;
import me.chayapak1.chomens_bot.util.HttpUtilities;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class Main {
    public static final Path stopReasonFilePath = Path.of("shutdown_reason.txt", new String[0]);
    public static final List<Bot> bots = new ObjectArrayList<Bot>();
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2), new ThreadFactoryBuilder().setNameFormat("ExecutorService #%d").build());
    public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2), new ThreadFactoryBuilder().setNameFormat("ScheduledExecutorService #%d").build());
    private static Configuration config;
    private static boolean alreadyStarted;
    public static boolean stopping;
    private static int backupFailTimes;
    public static ConsolePlugin console;
    public static DatabasePlugin database;
    public static DiscordPlugin discord;
    public static IRCPlugin irc;

    public static void main(String[] args2) throws IOException {
        Locale.setDefault(Locale.ROOT);
        Main.loadConfig();
        Thread shutdownThread = new Thread(Main::handleShutdown, "ChomeNS Bot Shutdown Thread");
        Runtime.getRuntime().addShutdownHook(shutdownThread);
        if (!Main.config.backup.enabled) {
            Main.initializeBots();
        } else {
            EXECUTOR.scheduleAtFixedRate(() -> {
                boolean reachable;
                try {
                    HttpUtilities.getRequest(new URI(Main.config.backup.address).toURL());
                    reachable = true;
                }
                catch (Exception e) {
                    reachable = false;
                }
                if (!reachable && !alreadyStarted) {
                    if (++backupFailTimes > Main.config.backup.failTimes) {
                        LoggerUtilities.log("Main instance is down! Starting backup instance");
                        Main.initializeBots();
                    }
                } else if (reachable && alreadyStarted) {
                    LoggerUtilities.log("Main instance is back up! Now stopping");
                    Main.stop(0);
                }
            }, 0L, Main.config.backup.interval, TimeUnit.MILLISECONDS);
        }
    }

    private static void loadConfig() throws IOException {
        BufferedReader reader;
        Path configPath = Path.of("config.yml", new String[0]);
        Constructor constructor = new Constructor(Configuration.class, new LoaderOptions());
        Yaml yaml = new Yaml(constructor);
        if (!Files.exists(configPath, new LinkOption[0])) {
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("default-config.yml");
            if (is == null) {
                System.exit(1);
            }
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();
            while (reader.ready()) {
                char character = (char)reader.read();
                stringBuilder.append(character);
            }
            String defaultConfig = stringBuilder.toString();
            BufferedWriter configWriter = Files.newBufferedWriter(configPath, new OpenOption[0]);
            configWriter.write(defaultConfig);
            configWriter.close();
            LoggerUtilities.log("config.yml file was not found, so the default one was created. Please modify it to your needs.");
            System.exit(1);
        }
        InputStream opt = Files.newInputStream(configPath, new OpenOption[0]);
        reader = new BufferedReader(new InputStreamReader(opt));
        config = (Configuration)yaml.load(reader);
    }

    private static void initializeBots() {
        alreadyStarted = true;
        try {
            if (Main.config.database.enabled) {
                database = new DatabasePlugin(config);
            }
            HashingUtilities.init();
            Configuration.BotOption[] botsOptions = Main.config.bots;
            for (Configuration.BotOption botOption : botsOptions) {
                Bot bot = new Bot(botOption, bots, config);
                bots.add(bot);
            }
            console = new ConsolePlugin(config);
            if (Main.config.discord.enabled) {
                discord = new DiscordPlugin(config);
            }
            if (Main.config.irc.enabled) {
                irc = new IRCPlugin(config);
            }
            EvalPlugin.connect(Main.config.eval.address);
            LoggerUtilities.log(I18nUtilities.get("initialized"));
            for (Bot bot : bots) {
                bot.connect();
            }
        }
        catch (Exception e) {
            LoggerUtilities.error(e);
            System.exit(1);
        }
    }

    private static void handleShutdown() {
        String reason = null;
        if (Files.exists(stopReasonFilePath, new LinkOption[0])) {
            try {
                reason = new String(Files.readAllBytes(stopReasonFilePath)).trim();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        Main.stop(0, reason, false);
    }

    public static void stop(int exitCode) {
        Main.stop(exitCode, null, null, true);
    }

    public static void stop(int exitCode, String reason) {
        Main.stop(exitCode, reason, null, true);
    }

    public static void stop(int exitCode, String reason, String type) {
        Main.stop(exitCode, reason, type, true);
    }

    public static void stop(int exitCode, String reason, boolean callSystemExit) {
        Main.stop(exitCode, reason, null, callSystemExit);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void stop(int exitCode, String reason, String type, boolean callSystemExit) {
        ArrayList<Bot> copiedList;
        if (stopping) {
            return;
        }
        stopping = true;
        String stoppingMessage = String.format(I18nUtilities.get("info.stopping.generic"), type != null ? type : I18nUtilities.get("info.stopping"), reason != null ? reason : I18nUtilities.get("info.no_reason"));
        LoggerUtilities.log(stoppingMessage);
        List<Bot> list = bots;
        synchronized (list) {
            copiedList = new ArrayList<Bot>(bots);
        }
        boolean ircEnabled = Main.config.irc.enabled;
        boolean discordEnabled = Main.config.discord.enabled;
        if (ircEnabled) {
            irc.quit(stoppingMessage);
        }
        boolean[] stoppedDiscord = new boolean[copiedList.size()];
        AtomicInteger botIndex = new AtomicInteger();
        for (Bot bot : copiedList) {
            try {
                if (discordEnabled) {
                    String channelId = bot.options.discordChannelId;
                    MessageCreateAction messageAction = discord.sendMessageInstantly(stoppingMessage, channelId, false);
                    int currentIndex = botIndex.get();
                    messageAction.queue(message -> {
                        stoppedDiscord[currentIndex] = true;
                    }, error -> {
                        stoppedDiscord[currentIndex] = true;
                    });
                }
                bot.stop();
            }
            catch (Exception exception) {
                // empty catch block
            }
            botIndex.getAndIncrement();
        }
        if (discordEnabled) {
            for (int i = 0; i < 150; ++i) {
                try {
                    if (ArrayUtilities.isAllTrue(stoppedDiscord)) break;
                    Thread.sleep(50L);
                    continue;
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
            }
            if (discord != null && Main.discord.jda != null) {
                Main.discord.jda.shutdown();
            }
        }
        EXECUTOR.shutdown();
        EXECUTOR_SERVICE.shutdown();
        FileLoggerUtilities.stop();
        if (database != null) {
            database.stop();
        }
        if (callSystemExit) {
            System.exit(exitCode);
        }
    }

    static {
        alreadyStarted = false;
        stopping = false;
        backupFailTimes = 0;
    }
}

