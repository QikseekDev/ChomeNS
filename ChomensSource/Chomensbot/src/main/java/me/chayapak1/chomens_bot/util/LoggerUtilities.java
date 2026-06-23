/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.data.logging.LogType;
import me.chayapak1.chomens_bot.util.ComponentUtilities;
import me.chayapak1.chomens_bot.util.ExceptionUtilities;
import me.chayapak1.chomens_bot.util.FileLoggerUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class LoggerUtilities {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static Component getPrefix(Bot bot, Component prefix, Component message) {
        LocalDateTime dateTime = LocalDateTime.now();
        TranslatableComponent component = bot != null ? Component.translatable("[%s %s] [%s] [%s] %s", (TextColor)NamedTextColor.DARK_GRAY, Component.text(dateTime.format(dateTimeFormatter), (TextColor)NamedTextColor.GRAY), prefix, Component.text(Thread.currentThread().getName(), (TextColor)NamedTextColor.GRAY), Component.text(bot.options.serverName, (TextColor)NamedTextColor.GRAY), Component.empty().append(message.colorIfAbsent(NamedTextColor.WHITE))) : Component.translatable("[%s %s] [%s] %s", (TextColor)NamedTextColor.DARK_GRAY, Component.text(dateTime.format(dateTimeFormatter), (TextColor)NamedTextColor.GRAY), prefix, Component.text(Thread.currentThread().getName(), (TextColor)NamedTextColor.GRAY), Component.empty().append(message.colorIfAbsent(NamedTextColor.WHITE)));
        return component;
    }

    public static void log(String message) {
        LoggerUtilities.log(LogType.INFO, null, Component.text(message), true, true);
    }

    public static void log(Component message) {
        LoggerUtilities.log(LogType.INFO, null, message, true, true);
    }

    public static void log(LogType type, String message) {
        LoggerUtilities.log(type, null, Component.text(message), true, true);
    }

    public static void log(LogType type, Component message) {
        LoggerUtilities.log(type, null, message, true, true);
    }

    public static void log(Bot bot, Component message) {
        LoggerUtilities.log(LogType.INFO, bot, message, true, true);
    }

    public static void log(Bot bot, String message) {
        LoggerUtilities.log(LogType.INFO, bot, Component.text(message), true, true);
    }

    public static void log(LogType type, Bot bot, Component message, boolean logToFile, boolean logToConsole) {
        Component component = LoggerUtilities.getPrefix(bot, type.component, message);
        if (logToConsole) {
            LoggerUtilities.print(ComponentUtilities.stringifyAnsi(component));
        }
        if (logToFile) {
            String formattedMessage = bot == null ? ComponentUtilities.stringify(message) : String.format("[%s] %s", bot.getServerString(true), ComponentUtilities.stringify(message));
            FileLoggerUtilities.log(ComponentUtilities.stringify(type.component), formattedMessage);
        }
    }

    public static void error(String message) {
        LoggerUtilities.log(LogType.ERROR, message);
    }

    public static void error(Component message) {
        LoggerUtilities.log(LogType.ERROR, message);
    }

    public static void error(Throwable throwable) {
        LoggerUtilities.log(LogType.ERROR, ExceptionUtilities.getStacktrace(throwable));
    }

    private static void print(String message) {
        if (Main.console == null) {
            System.out.println(message);
        } else {
            Main.console.reader.printAbove(message);
        }
    }
}

