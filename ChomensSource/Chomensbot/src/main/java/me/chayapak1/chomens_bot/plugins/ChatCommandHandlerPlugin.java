/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.contexts.PlayerCommandContext;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.chat.PlayerMessage;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.ComponentUtilities;
import me.chayapak1.chomens_bot.util.UUIDUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class ChatCommandHandlerPlugin
implements Listener {
    public final Bot bot;
    public final List<String> prefixes;
    public final List<String> commandSpyPrefixes;

    public ChatCommandHandlerPlugin(Bot bot) {
        this.bot = bot;
        this.prefixes = bot.config.prefixes;
        this.commandSpyPrefixes = bot.config.commandSpyPrefixes;
        bot.listener.addListener(this);
    }

    @Override
    public boolean onPlayerMessageReceived(PlayerMessage message, ChatPacketType packetType) {
        if (message.sender() != null && this.bot.profile != null && message.sender().profile.getId().equals(this.bot.profile.getId())) {
            return true;
        }
        Component displayNameComponent = message.displayName();
        Component messageComponent = message.contents();
        if (displayNameComponent == null || messageComponent == null) {
            return true;
        }
        this.handle(displayNameComponent, messageComponent, message.sender(), "@a", this.prefixes, packetType);
        return true;
    }

    @Override
    public void onCommandSpyMessageReceived(PlayerEntry sender, String command) {
        if (sender.profile != null && this.bot.profile != null && sender.profile.getId().equals(this.bot.profile.getId())) {
            return;
        }
        if (sender.profile == null) {
            return;
        }
        TextComponent displayNameComponent = Component.text(sender.profile.getName());
        TextComponent messageComponent = Component.text(command);
        this.handle(displayNameComponent, messageComponent, sender, UUIDUtilities.selector(sender.profile.getId()), this.commandSpyPrefixes, ChatPacketType.SYSTEM);
    }

    private void handle(Component displayNameComponent, Component messageComponent, PlayerEntry sender, String selector, List<String> prefixes, ChatPacketType packetType) {
        String displayName = ComponentUtilities.stringify(displayNameComponent);
        String contents = ComponentUtilities.stringify(messageComponent);
        String prefix = prefixes.stream().filter(eachPrefix -> contents.toLowerCase().startsWith((String)eachPrefix)).findFirst().orElse(null);
        if (prefix == null) {
            return;
        }
        String commandString = contents.substring(prefix.length());
        PlayerCommandContext context = new PlayerCommandContext(this.bot, displayName, prefix, selector, sender, packetType);
        this.bot.commandHandler.executeCommand(commandString, context);
    }
}

