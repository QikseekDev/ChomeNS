/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.logging.LogType;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class TrustedPlugin
implements Listener {
    private final Bot bot;
    public final List<String> list;

    public TrustedPlugin(Bot bot) {
        this.bot = bot;
        this.list = bot.config.trusted;
        bot.listener.addListener(this);
    }

    public void broadcast(Component message, UUID exceptTarget) {
        TranslatableComponent component = Component.translatable("[%s] [%s] %s", (TextColor)NamedTextColor.DARK_GRAY, Component.text("ChomeNS Bot", this.bot.colorPalette.primary), Component.text(this.bot.options.serverName, (TextColor)NamedTextColor.GRAY), message.colorIfAbsent(NamedTextColor.WHITE));
        LoggerUtilities.log(LogType.TRUSTED_BROADCAST, (Component)component);
        for (Bot bot : this.bot.bots) {
            if (bot == this.bot || !bot.loggedIn) continue;
            for (String player : this.list) {
                PlayerEntry entry = bot.players.getEntry(player);
                if (entry == null || entry.profile.getId() == exceptTarget) continue;
                bot.chat.tellraw((Component)component, entry.profile.getId());
            }
        }
    }

    public void broadcast(Component message) {
        this.broadcast(message, null);
    }

    @Override
    public void onPlayerJoined(PlayerEntry target) {
        TranslatableComponent component;
        if (!this.list.contains(target.profile.getName())) {
            return;
        }
        if (!target.profile.getName().equals(this.bot.config.ownerName)) {
            component = Component.translatable("Hello, %s!", (TextColor)NamedTextColor.GREEN, Component.text(target.profile.getName(), this.bot.colorPalette.username));
        } else {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
            String formattedTime = now.format(formatter);
            component = Component.translatable("Hello, %s!\nTime: %s\nOnline players: %s", (TextColor)NamedTextColor.GREEN, Component.text(target.profile.getName(), this.bot.colorPalette.username), Component.text(formattedTime, this.bot.colorPalette.string), Component.text(this.bot.players.list.size(), this.bot.colorPalette.number));
        }
        this.bot.chat.tellraw((Component)component, target.profile.getId());
        this.broadcast(Component.translatable(I18nUtilities.get("trusted_broadcast.online"), this.bot.colorPalette.defaultColor, Component.text(target.profile.getName(), this.bot.colorPalette.username)), target.profile.getId());
    }

    @Override
    public void onPlayerLeft(PlayerEntry target) {
        if (!this.list.contains(target.profile.getName())) {
            return;
        }
        this.broadcast(Component.translatable(I18nUtilities.get("trusted_broadcast.offline"), this.bot.colorPalette.defaultColor, Component.text(target.profile.getName(), this.bot.colorPalette.username)));
    }
}

