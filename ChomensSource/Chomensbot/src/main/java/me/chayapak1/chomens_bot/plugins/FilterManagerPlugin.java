/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.chat.PlayerMessage;
import me.chayapak1.chomens_bot.data.filter.FilteredPlayer;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.UUIDUtilities;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.jetbrains.annotations.Nullable;

public class FilterManagerPlugin
implements Listener {
    private final Bot bot;
    public final List<FilteredPlayer> list = Collections.synchronizedList(new ObjectArrayList());

    public FilterManagerPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
        bot.executor.scheduleAtFixedRate(this::kick, 0L, 10L, TimeUnit.SECONDS);
    }

    @Override
    public void onLocalSecondTick() {
        this.removeLeftPlayers();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void removeLeftPlayers() {
        List<FilteredPlayer> list = this.list;
        synchronized (list) {
            this.list.removeIf(filteredPlayer -> this.bot.players.getEntry(filteredPlayer.player().profile.getId()) == null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onTick() {
        List<FilteredPlayer> list = this.list;
        synchronized (list) {
            for (FilteredPlayer filtered : this.list) {
                PlayerEntry target = filtered.player();
                this.deOp(target);
                this.gameMode(target);
                this.clear(target);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void kick() {
        List<FilteredPlayer> list = this.list;
        synchronized (list) {
            for (FilteredPlayer filtered : this.list) {
                PlayerEntry playerEntry = filtered.player();
            }
        }
    }

    @Override
    public void onCommandSpyMessageReceived(PlayerEntry sender, String command) {
        FilteredPlayer filtered = this.getFilteredFromName(sender.profile.getName());
        if (filtered == null) {
            return;
        }
        if (command.startsWith("/mute") || command.startsWith("/emute") || command.startsWith("/silence") || command.startsWith("/esilence") || command.startsWith("/essentials:mute") || command.startsWith("/essentials:emute") || command.startsWith("/essentials:silence") || command.startsWith("/essentials:esilence")) {
            this.mute(sender, filtered.reason());
        }
        this.deOp(sender);
        this.gameMode(sender);
    }

    @Override
    public boolean onPlayerMessageReceived(PlayerMessage message, ChatPacketType packetType) {
        if (message.sender().profile.getName() == null) {
            return true;
        }
        FilteredPlayer filtered = this.getFilteredFromName(message.sender().profile.getName());
        if (filtered == null || message.sender().profile.getId().equals(new UUID(0L, 0L))) {
            return true;
        }
        this.doAll(message.sender(), filtered.reason());
        return true;
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.list.clear();
    }

    public void doAll(PlayerEntry entry) {
        this.doAll(entry, "");
    }

    public void doAll(PlayerEntry entry, String reason) {
        this.mute(entry, reason);
        this.deOp(entry);
        this.gameMode(entry);
        this.clear(entry);
    }

    public void mute(PlayerEntry target) {
        this.mute(target, "");
    }

    public void mute(PlayerEntry target, String reason) {
        this.bot.core.run("essentials:mute " + target.profile.getIdAsString() + " 10y " + reason);
    }

    public void deOp(PlayerEntry target) {
        this.bot.core.run("minecraft:execute run deop " + UUIDUtilities.selector(target.profile.getId()));
    }

    public void gameMode(PlayerEntry target) {
        this.bot.core.run("minecraft:gamemode spectator " + UUIDUtilities.selector(target.profile.getId()));
    }

    public void clear(PlayerEntry target) {
        this.bot.core.run("minecraft:clear " + UUIDUtilities.selector(target.profile.getId()));
    }

    public void add(PlayerEntry entry, String reason) {
        if (this.getFilteredFromName(entry.profile.getName()) != null || entry.profile.equals(this.bot.profile)) {
            return;
        }
        this.list.add(new FilteredPlayer(entry, reason));
        this.doAll(entry, reason);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void remove(String name) {
        List<FilteredPlayer> list = this.list;
        synchronized (list) {
            this.list.removeIf(filtered -> filtered.player().profile.getName().equals(name));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Nullable
    public FilteredPlayer getFilteredFromName(String name) {
        List<FilteredPlayer> list = this.list;
        synchronized (list) {
            for (FilteredPlayer filtered : this.list) {
                if (!filtered.player().profile.getName().equals(name)) continue;
                return filtered;
            }
        }
        return null;
    }
}

