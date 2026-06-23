/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
import me.chayapak1.chomens_bot.data.logging.LogType;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import net.kyori.adventure.text.Component;

public class FileLoggerUtilities {
    public static final Path logDirectory = Path.of("logs", new String[0]);
    public static final Path logPath = Paths.get(logDirectory.toString(), "log.txt");
    public static OutputStreamWriter logWriter;
    public static LocalDate currentLogDate;
    public static final ZoneId zone;
    public static final DateTimeFormatter dateTimeFormatter;
    public static String prevEntry;
    public static int duplicateCounter;
    public static final ScheduledExecutorService executor;
    public static int spamLevel;
    public static long freezeTime;

    public static void init() {
        try {
            if (!Files.exists(logDirectory, new LinkOption[0])) {
                Files.createDirectory(logDirectory, new FileAttribute[0]);
            }
            if (!Files.exists(logPath, new LinkOption[0])) {
                FileLoggerUtilities.makeNewLogFile();
            } else if (!FileLoggerUtilities.logIsCurrent(logPath)) {
                FileLoggerUtilities.compressLogFile();
                FileLoggerUtilities.makeNewLogFile();
            } else {
                FileLoggerUtilities.openLogFile();
            }
            executor.scheduleAtFixedRate(() -> {
                try {
                    FileLoggerUtilities.tick();
                }
                catch (Exception e) {
                    LoggerUtilities.error(e);
                }
            }, 0L, 50L, TimeUnit.MILLISECONDS);
        }
        catch (IOException e) {
            LoggerUtilities.error(e);
        }
    }

    public static void stop() {
        executor.shutdown();
    }

    private static void tick() {
        if (freezeTime <= System.currentTimeMillis() && spamLevel > 0) {
            --spamLevel;
        }
        if (!currentLogDate.equals(LocalDate.now())) {
            try {
                FileLoggerUtilities.compressLogFile();
                FileLoggerUtilities.makeNewLogFile();
            }
            catch (IOException e) {
                LoggerUtilities.error(e);
            }
        }
    }

    public static synchronized void makeNewLogFile() throws IOException {
        currentLogDate = LocalDate.now();
        logWriter = new OutputStreamWriter(Files.newOutputStream(logPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), StandardCharsets.UTF_8);
        logWriter.write(currentLogDate.toString() + "\n");
        logWriter.flush();
    }

    public static synchronized void openLogFile() throws IOException {
        currentLogDate = LocalDate.parse(FileLoggerUtilities.getLogDate(logPath));
        logWriter = new OutputStreamWriter(Files.newOutputStream(logPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND), StandardCharsets.UTF_8);
    }

    public static synchronized void compressLogFile() throws IOException {
        if (Files.size(logPath) > 524288000L) {
            LoggerUtilities.log(LogType.INFO, null, Component.text("Not archiving log file since it's too big!"), false, true);
            return;
        }
        Path path = Paths.get(logDirectory.toString(), FileLoggerUtilities.getLogDate(logPath) + ".txt.gz");
        try (InputStream in = Files.newInputStream(logPath, StandardOpenOption.READ);
             GZIPOutputStream out = new GZIPOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE));){
            int size;
            byte[] buffer = new byte[1024];
            while ((size = in.read(buffer)) > 0) {
                out.write(buffer, 0, size);
            }
        }
    }

    public static synchronized String getLogDate(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);){
            String string = reader.readLine();
            return string;
        }
    }

    public static synchronized boolean logIsCurrent(Path path) throws IOException {
        LocalDate date = LocalDate.now();
        return FileLoggerUtilities.getLogDate(path).equals(date.toString());
    }

    public static synchronized void log(String type, String str) {
        if (freezeTime > System.currentTimeMillis()) {
            return;
        }
        try {
            if (spamLevel >= 100) {
                spamLevel = 80;
                freezeTime = System.currentTimeMillis() + 20000L;
                if (duplicateCounter > 1) {
                    logWriter.write(String.format(" [%sx]\n", duplicateCounter));
                } else {
                    logWriter.write("\n");
                }
                logWriter.write("Spam detected, logs temporarily frozen");
                logWriter.flush();
                duplicateCounter = 1;
                prevEntry = "";
                return;
            }
            if (str.equalsIgnoreCase(prevEntry)) {
                ++duplicateCounter;
            } else {
                if (duplicateCounter > 1) {
                    logWriter.write(String.format(" [%sx]\n", duplicateCounter));
                } else {
                    logWriter.write("\n");
                }
                logWriter.write(FileLoggerUtilities.getPrefix(type) + str.replaceAll("\\[(\\d+?)x](?=$|[\r\n])", "[/$1x]"));
                logWriter.flush();
                duplicateCounter = 1;
                prevEntry = str;
                spamLevel += 2;
            }
        }
        catch (IOException e) {
            LoggerUtilities.error(e);
        }
    }

    public static String getPrefix(String type) {
        LocalDateTime dateTime = LocalDateTime.now(zone);
        return String.format("[%s %s] ", dateTime.format(dateTimeFormatter), type);
    }

    static {
        zone = ZoneId.of("UTC");
        dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        prevEntry = "";
        duplicateCounter = 1;
        executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("ScheduledExecutorService (logger)").build());
        spamLevel = 0;
        freezeTime = 0L;
        FileLoggerUtilities.init();
    }
}

