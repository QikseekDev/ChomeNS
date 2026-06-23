/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Configuration;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.discord.DirectMessageEventHandler;
import me.chayapak1.chomens_bot.discord.GuildMessageEventHandler;
import me.chayapak1.chomens_bot.discord.MessageLogger;
import me.chayapak1.chomens_bot.discord.SlashCommandHandler;
import me.chayapak1.chomens_bot.util.CodeBlockUtilities;
import me.chayapak1.chomens_bot.util.ComponentUtilities;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.geysermc.mcprotocollib.network.event.session.ConnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;

public class DiscordPlugin {
    private static final int MAX_ANSI_MESSAGE_LENGTH = 2000 - "```ansi\n\n```".length();
    private static final int LOG_DELAY = 2000;
    public JDA jda;
    public final Configuration.Discord options;
    public final String prefix;
    public final Component messagePrefix;
    public final String serverId;
    public final String discordUrl;
    private final Map<String, LogData> logData = new Object2ObjectOpenHashMap<String, LogData>();

    public DiscordPlugin(Configuration config) {
        this.options = config.discord;
        this.prefix = this.options.prefix;
        this.serverId = config.discord.serverId;
        this.discordUrl = config.discord.inviteLink;
        this.messagePrefix = ((TextComponent)((TextComponent)((TextComponent)Component.empty().append(Component.text("ChomeNS ", (TextColor)NamedTextColor.YELLOW))).append(Component.text("Discord", (TextColor)NamedTextColor.BLUE))).hoverEvent(HoverEvent.showText(Component.text("Click here to join the Discord server", (TextColor)NamedTextColor.GREEN)))).clickEvent(ClickEvent.openUrl(this.discordUrl));
        JDABuilder builder = JDABuilder.createDefault(config.discord.token);
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS);
        builder.setEnableShutdownHook(false);
        try {
            this.jda = builder.build();
            this.jda.awaitReady();
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
        if (this.jda == null) {
            return;
        }
        this.jda.getPresence().setPresence(Activity.playing(config.discord.statusMessage), false);
        new MessageLogger(this.jda);
        new GuildMessageEventHandler(this.jda, this.prefix, this.messagePrefix);
        new DirectMessageEventHandler(this.jda, this.options);
        new SlashCommandHandler(this.jda);
        Main.EXECUTOR.scheduleAtFixedRate(this::onDiscordTick, 0L, 50L, TimeUnit.MILLISECONDS);
        for (final Bot bot : Main.bots) {
            final String channelId = bot.options.discordChannelId;
            if (channelId == null) continue;
            this.logData.put(channelId, new LogData());
            bot.listener.addListener(new Listener(){

                @Override
                public boolean onSystemMessageReceived(Component component, ChatPacketType packetType, String string, String unusedAnsi) {
                    if (string.length() > MAX_ANSI_MESSAGE_LENGTH) {
                        DiscordPlugin.this.sendMessage(CodeBlockUtilities.escape(string), channelId);
                    } else {
                        String ansi = ComponentUtilities.stringifyDiscordAnsi(component);
                        DiscordPlugin.this.sendMessage(CodeBlockUtilities.escape(ansi), channelId);
                    }
                    return true;
                }

                @Override
                public void onConnecting() {
                    if (!bot.options.logConnectionStatusMessages || bot.connectAttempts > 6) {
                        return;
                    }
                    if (bot.connectAttempts == 6) {
                        DiscordPlugin.this.sendMessageInstantly(I18nUtilities.get("info.suppressing"), channelId);
                        return;
                    }
                    DiscordPlugin.this.sendMessageInstantly(String.format(I18nUtilities.get("info.connecting"), "`" + bot.getServerString().replace("`", "\\`") + "`"), channelId);
                }

                @Override
                public void connected(ConnectedEvent event) {
                    DiscordPlugin.this.sendMessageInstantly(String.format(I18nUtilities.get("info.connected"), "`" + bot.getServerString().replace("`", "\\`") + "`"), channelId);
                }

                @Override
                public void disconnected(DisconnectedEvent event) {
                    if (!bot.options.logConnectionStatusMessages || bot.connectAttempts >= 6) {
                        return;
                    }
                    String reason = ComponentUtilities.stringifyDiscordAnsi(event.getReason());
                    DiscordPlugin.this.sendMessageInstantly(String.format(I18nUtilities.get("info.disconnected"), "\n```ansi\n" + CodeBlockUtilities.escape(reason) + "\n```"), channelId);
                }
            });
        }
    }

    public void sendMessage(String message, String channelId) {
        LogData data = this.logData.get(channelId);
        if (data == null) {
            return;
        }
        StringBuilder logMessage = data.logMessages;
        if (logMessage.length() < 2000) {
            if (!logMessage.isEmpty()) {
                logMessage.append('\n');
            }
            logMessage.append(message);
        }
    }

    public void sendMessageInstantly(String message, String channelId) {
        this.sendMessageInstantly(message, channelId, true);
    }

    public MessageCreateAction sendMessageInstantly(String message, String channelId, boolean queue) {
        if (this.jda == null) {
            return null;
        }
        TextChannel logChannel = this.jda.getTextChannelById(channelId);
        if (logChannel == null) {
            LoggerUtilities.error("Log channel for " + channelId + " is null");
            return null;
        }
        if (queue) {
            LogData data = this.logData.get(channelId);
            if (data == null) {
                return null;
            }
            logChannel.sendMessage(message).queue(msg -> data.doneSendingInLog.set(true), e -> {
                LoggerUtilities.error(e);
                data.doneSendingInLog.set(false);
            });
            return null;
        }
        return logChannel.sendMessage(message);
    }

    public void onDiscordTick() {
        for (Bot bot : Main.bots) {
            String message;
            long currentTime;
            LogData data;
            String channelId = bot.options.discordChannelId;
            if (channelId == null || (data = this.logData.get(channelId)) == null || ((currentTime = System.currentTimeMillis()) < data.nextLogTime.get() || !data.doneSendingInLog.get()) && currentTime - data.nextLogTime.get() < 5000L) continue;
            data.nextLogTime.set(currentTime + 2000L);
            StringBuilder logMessages = data.logMessages;
            if (logMessages.length() < 4096) {
                StringBuilder messageBuilder = new StringBuilder();
                Matcher inviteMatcher = Message.INVITE_PATTERN.matcher(logMessages.toString());
                while (inviteMatcher.find()) {
                    inviteMatcher.appendReplacement(messageBuilder, Matcher.quoteReplacement(inviteMatcher.group().replace(".", "\u200b.")));
                }
                inviteMatcher.appendTail(messageBuilder);
                message = messageBuilder.substring(0, Math.min(messageBuilder.length(), MAX_ANSI_MESSAGE_LENGTH));
            } else {
                message = logMessages.toString().replace(".", "\u200b.").substring(0, Math.min(logMessages.length(), MAX_ANSI_MESSAGE_LENGTH));
            }
            logMessages.setLength(0);
            if (message.trim().isBlank()) continue;
            this.sendMessageInstantly("```ansi\n" + message + "\n```", channelId);
        }
    }

    private static class LogData {
        private final StringBuilder logMessages = new StringBuilder();
        private final AtomicLong nextLogTime = new AtomicLong(0L);
        private final AtomicBoolean doneSendingInLog = new AtomicBoolean(false);

        private LogData() {
        }
    }
}

