/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.logging.LogType;
import me.chayapak1.chomens_bot.util.ExceptionUtilities;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.geysermc.mcprotocollib.network.event.session.ConnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;

public class LoggerPlugin
implements Listener {
    private final Bot bot;
    public boolean logToConsole = true;

    public LoggerPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    public void log(LogType type, Component message) {
        LoggerUtilities.log(type, this.bot, message, true, this.logToConsole);
    }

    public void log(LogType type, Component message, boolean logToFile) {
        LoggerUtilities.log(type, this.bot, message, logToFile, this.logToConsole);
    }

    public void log(Component message) {
        this.log(LogType.INFO, message);
    }

    public void log(String message) {
        this.log(LogType.INFO, Component.text(message));
    }

    public void log(LogType type, String message) {
        this.log(type, Component.text(message));
    }

    public void error(Component message) {
        this.log(LogType.ERROR, message);
    }

    public void error(String message) {
        this.log(LogType.ERROR, Component.text(message));
    }

    public void error(Throwable throwable) {
        this.log(LogType.ERROR, ExceptionUtilities.getStacktrace(throwable));
    }

    @Override
    public void onConnecting() {
        if (!this.bot.options.logConnectionStatusMessages || this.bot.connectAttempts > 10) {
            return;
        }
        if (this.bot.connectAttempts == 10) {
            this.log(I18nUtilities.get("info.suppressing"));
            return;
        }
        this.log(String.format(I18nUtilities.get("info.connecting"), this.bot.getServerString(true)));
    }

    @Override
    public void connected(ConnectedEvent event) {
        this.log(String.format(I18nUtilities.get("info.connected"), this.bot.getServerString(true)));
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        if (!this.bot.options.logConnectionStatusMessages || this.bot.connectAttempts >= 10) {
            return;
        }
        TranslatableComponent message = Component.translatable(I18nUtilities.get("info.disconnected"), event.getReason());
        this.log(message);
    }

    @Override
    public boolean onSystemMessageReceived(Component component, ChatPacketType packetType, String string, String ansi) {
        this.log(LogType.CHAT, Component.translatable("[%s] %s", (TextColor)NamedTextColor.DARK_GRAY, Component.text(packetType.shortName, (TextColor)NamedTextColor.GRAY), component.colorIfAbsent(NamedTextColor.WHITE)));
        return true;
    }
}

