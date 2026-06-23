/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.plugins.DatabasePlugin;
import me.chayapak1.chomens_bot.util.ComponentUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoRemovePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;

public class PlayersPlugin
implements Listener {
    private final Bot bot;
    public final List<PlayerEntry> list = Collections.synchronizedList(new ObjectArrayList());
    private final List<PlayerEntry> pendingLeftPlayers = Collections.synchronizedList(new ObjectArrayList());

    public PlayersPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
        bot.executor.scheduleAtFixedRate(this::onLastKnownNameTick, 0L, 5L, TimeUnit.SECONDS);
    }

    @Override
    public void onSecondTick() {
        this.queryPlayersIP();
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundPlayerInfoUpdatePacket) {
            ClientboundPlayerInfoUpdatePacket t_packet = (ClientboundPlayerInfoUpdatePacket)packet;
            this.packetReceived(t_packet);
        } else if (packet instanceof ClientboundPlayerInfoRemovePacket) {
            ClientboundPlayerInfoRemovePacket t_packet = (ClientboundPlayerInfoRemovePacket)packet;
            this.packetReceived(t_packet);
        }
    }

    private void packetReceived(ClientboundPlayerInfoUpdatePacket packet) {
        EnumSet<PlayerListEntryAction> actions = packet.getActions();
        for (PlayerListEntryAction action : actions) {
            block9: for (PlayerListEntry entry : packet.getEntries()) {
                switch (action) {
                    case ADD_PLAYER: {
                        this.addPlayer(entry);
                        continue block9;
                    }
                    case INITIALIZE_CHAT: {
                        this.initializeChat(entry);
                        continue block9;
                    }
                    case UPDATE_LISTED: {
                        this.updateListed(entry);
                        continue block9;
                    }
                    case UPDATE_GAME_MODE: {
                        this.updateGameMode(entry);
                        continue block9;
                    }
                    case UPDATE_LATENCY: {
                        this.updateLatency(entry);
                        continue block9;
                    }
                    case UPDATE_DISPLAY_NAME: {
                        this.updateDisplayName(entry);
                    }
                }
            }
        }
    }

    private void packetReceived(ClientboundPlayerInfoRemovePacket packet) {
        List<UUID> uuids = packet.getProfileIds();
        for (UUID uuid : uuids) {
            this.removePlayer(uuid);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void queryPlayersIP() {
        List<PlayerEntry> list = this.list;
        synchronized (list) {
            for (PlayerEntry target : this.list) {
                this.queryPlayersIP(target);
            }
        }
    }

    private void queryPlayersIP(PlayerEntry target) {
        if (target.persistingData.ip != null) {
            return;
        }
        CompletableFuture<String> future = this.getPlayerIP(target, false);
        future.thenApply(ip -> {
            if (ip == null) {
                return null;
            }
            target.persistingData.ip = ip;
            this.bot.listener.dispatch(listener -> listener.onQueriedPlayerIP(target, (String)ip));
            return null;
        });
    }

    public CompletableFuture<String> getPlayerIP(PlayerEntry target, boolean fallbackToDatabase) {
        CompletableFuture<String> seenFuture = this.getSeenPlayerIP(target);
        if (seenFuture != null) {
            return seenFuture;
        }
        if (fallbackToDatabase) {
            return this.getDatabasePlayerIP(target);
        }
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<String> getDatabasePlayerIP(PlayerEntry target) {
        CompletableFuture<String> outputFuture = new CompletableFuture<String>();
        DatabasePlugin.EXECUTOR_SERVICE.execute(() -> outputFuture.complete(this.bot.playersDatabase.getPlayerIP(target.profile.getName())));
        return outputFuture;
    }

    private CompletableFuture<String> getSeenPlayerIP(PlayerEntry target) {
        CompletableFuture<String> outputFuture = new CompletableFuture<String>();
        if (!this.bot.serverFeatures.hasEssentials || !this.bot.serverFeatures.serverHasCommand("essentials:seen") && !this.bot.serverFeatures.serverHasCommand("seen")) {
            return null;
        }
        CompletableFuture<Component> trackedCoreFuture = this.bot.core.runTracked("essentials:seen " + target.profile.getIdAsString());
        if (trackedCoreFuture == null) {
            return null;
        }
        trackedCoreFuture.completeOnTimeout(null, 5L, TimeUnit.SECONDS);
        trackedCoreFuture.thenApply(output -> {
            List<Component> children = output.children();
            String string = ComponentUtilities.stringify(Component.join(JoinConfiguration.separator(Component.empty()), children)).trim();
            if (!string.startsWith("- IP Address: ")) {
                return null;
            }
            if ((string = string.substring("- IP Address: ".length())).startsWith("/")) {
                string = string.substring(1);
            }
            outputFuture.complete(string);
            return null;
        });
        return outputFuture;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final PlayerEntry getEntry(UUID uuid) {
        List<PlayerEntry> list = this.list;
        synchronized (list) {
            for (PlayerEntry candidate : this.list) {
                if (candidate == null || !candidate.profile.getId().equals(uuid)) continue;
                return candidate;
            }
            return null;
        }
    }

    public final PlayerEntry getEntry(String username) {
        return this.getEntry(username, true, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final PlayerEntry getEntry(String username, boolean checkUUID, boolean checkPastUsernames) {
        List<PlayerEntry> list = this.list;
        synchronized (list) {
            for (PlayerEntry candidate : this.list) {
                if (candidate == null || !candidate.profile.getName().equals(username) && (!checkUUID || !candidate.profile.getIdAsString().equals(username)) && (!checkPastUsernames || !candidate.persistingData.usernames.contains(username))) continue;
                return candidate;
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final PlayerEntry getEntryTheBukkitWay(String username) {
        PlayerEntry found = this.getEntry(username, false, false);
        if (found != null) {
            return found;
        }
        String lowerName = username.toLowerCase(Locale.ROOT);
        int delta = Integer.MAX_VALUE;
        List<PlayerEntry> list = this.list;
        synchronized (list) {
            for (PlayerEntry player : this.list) {
                if (!player.profile.getName().toLowerCase(Locale.ROOT).startsWith(lowerName)) continue;
                int curDelta = Math.abs(player.profile.getName().length() - lowerName.length());
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }
                if (curDelta != 0) continue;
                break;
            }
        }
        return found;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final PlayerEntry getEntry(Component displayName) {
        List<PlayerEntry> list = this.list;
        synchronized (list) {
            for (PlayerEntry candidate : this.list) {
                if (candidate == null || candidate.displayName == null || !candidate.displayName.equals(displayName)) continue;
                return candidate;
            }
            return null;
        }
    }

    public PlayerEntry getBotEntry() {
        return this.getEntry(this.bot.username);
    }

    private PlayerEntry getEntry(PlayerListEntry other) {
        return this.getEntry(other.getProfileId());
    }

    private void initializeChat(PlayerListEntry newEntry) {
        PlayerEntry target = this.getEntry(newEntry);
        if (target == null) {
            return;
        }
        target.publicKey = newEntry.getPublicKey();
    }

    private void updateListed(PlayerListEntry newEntry) {
        PlayerEntry target = this.getEntry(newEntry);
        if (target == null) {
            return;
        }
        target.persistingData.listed = newEntry.isListed();
    }

    private void addPlayer(PlayerListEntry newEntry) {
        PlayerEntry duplicate = this.getEntry(newEntry);
        PlayerEntry target = new PlayerEntry(newEntry);
        if (duplicate != null && !duplicate.profile.getName().equals(target.profile.getName())) {
            return;
        }
        if (duplicate != null) {
            this.list.removeIf(entry -> entry.equals(duplicate));
            target.persistingData = new PlayerEntry.PersistingData(duplicate.persistingData);
            target.persistingData.listed = true;
            this.list.add(target);
            this.bot.listener.dispatch(listener -> listener.onPlayerUnVanished(target));
        } else {
            this.list.add(target);
            this.bot.listener.dispatch(listener -> listener.onPlayerJoined(target));
            this.queryPlayersIP(target);
        }
    }

    private void updateGameMode(PlayerListEntry newEntry) {
        GameMode gameMode;
        PlayerEntry target = this.getEntry(newEntry);
        if (target == null) {
            return;
        }
        target.gamemode = gameMode = newEntry.getGameMode();
        this.bot.listener.dispatch(listener -> listener.onPlayerGameModeUpdated(target, gameMode));
    }

    private void updateLatency(PlayerListEntry newEntry) {
        int ping;
        PlayerEntry target = this.getEntry(newEntry);
        if (target == null) {
            return;
        }
        target.latency = ping = newEntry.getLatency();
        this.bot.listener.dispatch(listener -> listener.onPlayerLatencyUpdated(target, ping));
    }

    private void updateDisplayName(PlayerListEntry newEntry) {
        Component displayName;
        PlayerEntry target = this.getEntry(newEntry);
        if (target == null) {
            return;
        }
        target.displayName = displayName = newEntry.getDisplayName();
        this.bot.listener.dispatch(listener -> listener.onPlayerDisplayNameUpdated(target, displayName));
    }

    private CompletableFuture<String> getLastKnownName(boolean useCargo, String uuid) {
        return this.bot.query.entity(useCargo, uuid, "bukkit.lastKnownName");
    }

    private void check(boolean useCargo, PlayerEntry target) {
        PlayerEntry pending = this.pendingLeftPlayers.stream().filter(player -> player.equals(target)).findAny().orElse(null);
        if (pending != null) {
            this.pendingLeftPlayers.remove(pending);
        }
        CompletableFuture<String> future = this.getLastKnownName(useCargo, target.profile.getIdAsString());
        future.thenApply(lastKnownName -> {
            if (lastKnownName == null && !target.profile.getName().isEmpty()) {
                boolean removed = this.list.remove(target);
                if (removed) {
                    this.bot.listener.dispatch(listener -> listener.onPlayerLeft(target));
                }
                return null;
            }
            if (lastKnownName != null && !lastKnownName.equals(target.profile.getName())) {
                PlayerEntry newTarget = new PlayerEntry(new GameProfile(target.profile.getId(), (String)lastKnownName), target.gamemode, target.latency, target.displayName, target.expiresAt, target.publicKey, target.keySignature, target.persistingData.listed);
                newTarget.persistingData = new PlayerEntry.PersistingData(target);
                this.list.removeIf(entry -> entry.profile.getId().equals(target.profile.getId()));
                this.list.add(newTarget);
                this.bot.listener.dispatch(listener -> listener.onPlayerChangedUsername(newTarget, target.profile.getName(), newTarget.profile.getName()));
            } else if (pending != null) {
                target.persistingData.listed = false;
                this.bot.listener.dispatch(listener -> listener.onPlayerVanished(target));
            }
            return null;
        });
    }

    private void onLastKnownNameTick() {
        if (!(this.bot.loggedIn && this.bot.core.ready && this.bot.serverFeatures.hasNamespaces)) {
            return;
        }
        for (PlayerEntry target : new ArrayList<PlayerEntry>(this.list)) {
            this.check(true, target);
        }
    }

    private void removePlayer(UUID uuid) {
        PlayerEntry target = this.getEntry(uuid);
        if (target == null) {
            return;
        }
        if (System.currentTimeMillis() - this.bot.loginTime > 5000L && !this.bot.serverFeatures.hasNamespaces || target.profile.getName().isEmpty()) {
            boolean removed = this.list.remove(target);
            if (removed) {
                this.bot.listener.dispatch(listener -> listener.onPlayerLeft(target));
            }
        } else {
            this.pendingLeftPlayers.add(target);
            this.check(false, target);
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.list.clear();
    }
}

