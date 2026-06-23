/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class ClearChatNameAnnouncerPlugin
implements Listener {
    private final Bot bot;

    public ClearChatNameAnnouncerPlugin(Bot bot) {
        this.bot = bot;
        if (!bot.config.announceClearChatUsername) {
            return;
        }
        bot.listener.addListener(this);
    }

    @Override
    public void onCommandSpyMessageReceived(PlayerEntry sender, String command) {
        if (!this.bot.config.announceClearChatUsername) {
            return;
        }
        if (command.equals("/clearchat") || command.equals("/cc") || command.equals("/extras:clearchat") || command.equals("/extras:cc")) {
            this.bot.chat.tellraw(Component.translatable("%s cleared the chat", (TextColor)NamedTextColor.DARK_GREEN, Component.selector(sender.profile.getIdAsString())));
        }
    }
}

