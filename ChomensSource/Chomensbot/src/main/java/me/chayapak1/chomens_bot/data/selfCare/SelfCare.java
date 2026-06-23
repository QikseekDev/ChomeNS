/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.selfCare;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.network.packet.Packet;

public abstract class SelfCare {
    public final Bot bot;
    public boolean needsRunning = false;

    public SelfCare(Bot bot) {
        this.bot = bot;
    }

    public abstract boolean shouldRun();

    public void onPacketReceived(Packet packet) {
    }

    public void onMessageReceived(Component component, String string) {
    }

    public void onCommandSpyMessageReceived(PlayerEntry sender, String command) {
    }

    public void onPlayerChangedUsername(PlayerEntry target, String from, String to) {
    }

    public void run() {
    }

    public void cleanup() {
    }
}

