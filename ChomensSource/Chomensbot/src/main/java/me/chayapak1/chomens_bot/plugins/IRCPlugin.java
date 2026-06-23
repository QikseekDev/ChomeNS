/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocketFactory;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Configuration;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.contexts.IRCCommandContext;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.util.ColorUtilities;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.geysermc.mcprotocollib.network.event.session.ConnectedEvent;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.cap.SASLCapHandler;
import org.pircbotx.delay.StaticReadonlyDelay;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class IRCPlugin
extends ListenerAdapter {
    private final Configuration.IRC ircConfig;
    public final Map<String, List<String>> messageQueue = new HashMap<String, List<String>>();
    private PircBotX bot;

    public IRCPlugin(Configuration config) {
        this.ircConfig = config.irc;
        if (!this.ircConfig.enabled) {
            return;
        }
        Configuration.Builder builder = new Configuration.Builder().setName(this.ircConfig.name).setLogin("bot@chomens-bot").addServer(this.ircConfig.host, this.ircConfig.port).setSocketFactory(SSLSocketFactory.getDefault()).setAutoReconnect(true).setMessageDelay(new StaticReadonlyDelay(50L)).addListener(this);
        if (!this.ircConfig.password.isEmpty()) {
            builder.addCapHandler(new SASLCapHandler(this.ircConfig.name, this.ircConfig.password, true));
        }
        for (Bot bot : Main.bots) {
            String channel = bot.options.ircChannel;
            if (channel == null) continue;
            builder.addAutoJoinChannel(channel);
        }
        org.pircbotx.Configuration configuration = builder.buildConfiguration();
        this.bot = new PircBotX(configuration);
        new Thread(() -> {
            try {
                this.bot.startBot();
            }
            catch (Exception e) {
                LoggerUtilities.error(e);
            }
        }).start();
        for (final Bot bot : Main.bots) {
            bot.listener.addListener(new Listener(){

                @Override
                public void connected(ConnectedEvent event) {
                    IRCPlugin.this.connected(bot);
                }

                @Override
                public boolean onSystemMessageReceived(Component component, ChatPacketType packetType, String string, String ansi) {
                    IRCPlugin.this.systemMessageReceived(bot, ansi);
                    return true;
                }
            });
        }
        Main.EXECUTOR.scheduleAtFixedRate(this::queueTick, 0L, 100L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onMessage(MessageEvent event) {
        for (Bot bot : Main.bots) {
            if (!bot.options.ircChannel.equals(event.getChannel().getName())) continue;
            String commandPrefix = this.ircConfig.prefix;
            User user = event.getUser();
            if (user == null) {
                return;
            }
            String name = user.getRealName().isBlank() ? user.getNick() : user.getRealName();
            String message = event.getMessage();
            if (message.startsWith(commandPrefix)) {
                String noPrefix = message.substring(commandPrefix.length());
                IRCCommandContext context = new IRCCommandContext(bot, commandPrefix, name);
                bot.commandHandler.executeCommand(noPrefix, context);
                return;
            }
            Component prefix = ((TextComponent)Component.text(event.getChannel().getName()).hoverEvent(HoverEvent.showText(((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().append(Component.text("on "))).append(Component.text(this.ircConfig.host))).append(Component.text(":"))).append(Component.text(this.ircConfig.port))).color(NamedTextColor.GRAY)))).color(NamedTextColor.BLUE);
            Component username = ((TextComponent)Component.text(name).hoverEvent(HoverEvent.showText(Component.text(event.getUser().getHostname(), (TextColor)NamedTextColor.RED)))).color(NamedTextColor.RED);
            Component messageComponent = Component.text(message).color(NamedTextColor.GRAY);
            TranslatableComponent component = Component.translatable("[%s] %s \u203a %s", (TextColor)NamedTextColor.DARK_GRAY, prefix, username, messageComponent);
            bot.chat.tellraw(component);
        }
    }

    private void systemMessageReceived(Bot bot, String ansi) {
        this.sendMessage(bot, ansi);
    }

    public void quit(String reason) {
        if (this.bot.isConnected()) {
            this.bot.sendIRC().quitServer(reason);
        }
    }

    private void connected(Bot bot) {
        this.sendMessage(bot, String.format(I18nUtilities.get("info.connected"), bot.getServerString()));
    }

    private void queueTick() {
        if (!this.bot.isConnected() || this.messageQueue.isEmpty()) {
            return;
        }
        try {
            HashMap<String, List<String>> clonedMap = new HashMap<String, List<String>>(this.messageQueue);
            for (Map.Entry entry : clonedMap.entrySet()) {
                List logs = (List)entry.getValue();
                if (logs.isEmpty()) continue;
                String firstLog = (String)logs.getFirst();
                logs.removeFirst();
                String withIRCColors = ColorUtilities.convertAnsiToIrc(firstLog);
                this.bot.sendIRC().message((String)entry.getKey(), withIRCColors);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void addMessageToQueue(String channel, String message) {
        ArrayList<String> split = new ArrayList<String>(Arrays.asList(message.split("\n")));
        if (!this.messageQueue.containsKey(channel)) {
            this.messageQueue.put(channel, split);
        } else {
            if (this.messageQueue.get(channel).size() > 10) {
                return;
            }
            this.messageQueue.get(channel).addAll(split);
        }
    }

    public void sendMessage(Bot bot, String message) {
        String channel = bot.options.ircChannel;
        if (channel != null) {
            this.addMessageToQueue(channel, message);
        }
    }
}

