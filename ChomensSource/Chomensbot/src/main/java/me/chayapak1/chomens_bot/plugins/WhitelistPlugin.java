/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.util.ArrayList;
import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;

public class WhitelistPlugin
implements Listener {
    private final Bot bot;
    public final List<String> list = new ArrayList<String>();
    private boolean enabled = false;

    public WhitelistPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void enable() {
        this.enabled = true;
        List<PlayerEntry> list = this.bot.players.list;
        synchronized (list) {
            for (PlayerEntry entry : this.bot.players.list) {
                if (this.list.contains(entry.profile.getName())) continue;
                this.list.add(entry.profile.getName());
                this.bot.filterManager.remove(entry.profile.getName());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void disable() {
        this.enabled = false;
        List<PlayerEntry> list = this.bot.players.list;
        synchronized (list) {
            for (PlayerEntry entry : this.bot.players.list) {
                this.bot.filterManager.remove(entry.profile.getName());
            }
        }
    }

    public void add(String player) {
        this.list.add(player);
        this.bot.filterManager.remove(player);
    }

    public String remove(int index) {
        String removed = this.list.remove(index);
        this.checkAndAddToFilterManager(removed);
        return removed;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void clear() {
        this.list.removeIf(eachPlayer -> !eachPlayer.equals(this.bot.profile.getName()));
        List<PlayerEntry> list = this.bot.players.list;
        synchronized (list) {
            for (PlayerEntry entry : this.bot.players.list) {
                if (entry.profile.equals(this.bot.profile)) continue;
                this.bot.filterManager.add(entry, "");
            }
        }
    }

    public boolean isBlacklisted(String name) {
        return !this.list.contains(name);
    }

    private void checkAndAddToFilterManager(String player) {
        PlayerEntry entry = this.bot.players.getEntry(player);
        if (entry == null) {
            return;
        }
        this.bot.filterManager.add(entry, "");
    }

    @Override
    public void onPlayerJoined(PlayerEntry target) {
        if (!this.enabled) {
            return;
        }
        if (this.isBlacklisted(target.profile.getName())) {
            this.bot.filterManager.add(target, "");
        }
    }
}

