/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.UUIDUtilities;
import net.kyori.adventure.text.Component;

public class BotSelectorBroadcasterPlugin
implements Listener {
    private final Bot bot;
    private final String id;

    public BotSelectorBroadcasterPlugin(Bot bot) {
        this.bot = bot;
        this.id = bot.config.namespace + "_selector";
        bot.listener.addListener(this);
    }

    @Override
    public void onPlayerJoined(PlayerEntry target) {
        this.sendSelector(UUIDUtilities.selector(target.profile.getId()));
    }

    @Override
    public void onCoreReady() {
        this.sendSelector("@a");
    }

    private void sendSelector(String playerSelector) {
        this.bot.chat.actionBar((Component)Component.translatable("", Component.text(this.id), Component.text(UUIDUtilities.selector(this.bot.profile.getId()))), playerSelector);
    }
}

