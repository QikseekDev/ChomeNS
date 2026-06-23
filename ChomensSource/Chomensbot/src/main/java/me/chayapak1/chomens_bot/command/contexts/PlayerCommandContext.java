/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.command.contexts;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import me.chayapak1.chomens_bot.util.UUIDUtilities;
import net.kyori.adventure.text.Component;

public class PlayerCommandContext
extends CommandContext {
    public final String playerName;
    public final String selector;
    public final ChatPacketType packetType;
    private final Bot bot;

    public PlayerCommandContext(Bot bot, String playerName, String prefix, String selector, PlayerEntry sender, ChatPacketType packetType) {
        super(bot, prefix, sender, true);
        this.bot = bot;
        this.playerName = playerName;
        this.selector = selector;
        this.packetType = packetType;
    }

    @Override
    public void sendOutput(Component component) {
        this.sendOutput(component, false);
    }

    public void sendOutput(Component component, boolean onlyToSender) {
        Component rendered = I18nUtilities.render(component);
        String selector = onlyToSender ? UUIDUtilities.selector(this.sender.profile.getId()) : this.selector;
        this.bot.chat.tellraw((Component)Component.translatable("%s", rendered, Component.text("chomens_bot_command_output" + (String)(this.commandName != null ? "_" + this.commandName : ""))), selector);
    }

    @Override
    public Component displayName() {
        return this.sender.displayName;
    }
}

