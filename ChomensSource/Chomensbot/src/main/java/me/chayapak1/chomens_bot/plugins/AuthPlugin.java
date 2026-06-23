/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.util.concurrent.TimeUnit;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.logging.LogType;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;

public class AuthPlugin
implements Listener {
    private final Bot bot;
    public boolean isAuthenticating = false;
    public int seconds = 0;

    public AuthPlugin(Bot bot) {
        this.bot = bot;
        if (!bot.config.ownerAuthentication.enabled) {
            return;
        }
        bot.executor.scheduleAtFixedRate(() -> {
            if (!this.isAuthenticating || !bot.config.ownerAuthentication.enabled) {
                return;
            }
            this.checkAuthenticated();
            this.timeoutCheck();
        }, 500L, 500L, TimeUnit.MILLISECONDS);
        bot.listener.addListener(this);
    }

    private void checkAuthenticated() {
        PlayerEntry target = this.bot.players.getEntry(this.bot.config.ownerName);
        if (target == null) {
            return;
        }
        if (!this.bot.chomeNSMod.connectedPlayers.contains(target)) {
            return;
        }
        this.cleanup();
        target.persistingData.authenticatedTrustLevel = TrustLevel.MAX;
        this.bot.logger.log(LogType.AUTH, Component.text(I18nUtilities.get("auth.logger_verified"), (TextColor)NamedTextColor.GREEN));
        this.bot.chomeNSMod.sendMessage(target, Component.text(I18nUtilities.get("auth.player_verified"), (TextColor)NamedTextColor.GREEN));
    }

    private void timeoutCheck() {
        if (this.seconds < this.bot.config.ownerAuthentication.timeout) {
            return;
        }
        PlayerEntry target = this.bot.players.getEntry(this.bot.config.ownerName);
        if (target == null) {
            return;
        }
        this.bot.filterManager.add(target, I18nUtilities.get("auth.timed_out"));
    }

    private void cleanup() {
        this.isAuthenticating = false;
        this.seconds = 0;
    }

    @Override
    public void onSecondTick() {
        if (this.isAuthenticating) {
            ++this.seconds;
        }
    }

    @Override
    public void onPlayerJoined(PlayerEntry target) {
        if (!target.profile.getName().equals(this.bot.config.ownerName) || !this.bot.options.useCore) {
            return;
        }
        this.isAuthenticating = true;
    }

    @Override
    public void onPlayerLeft(PlayerEntry target) {
        if (!target.profile.getName().equals(this.bot.config.ownerName)) {
            return;
        }
        this.cleanup();
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.cleanup();
    }
}

