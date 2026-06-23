/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.player;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.security.PublicKey;
import java.util.List;
import me.chayapak1.chomens_bot.command.TrustLevel;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.jetbrains.annotations.NotNull;

public class PlayerEntry {
    public final GameProfile profile;
    public GameMode gamemode;
    public int latency;
    public Component displayName;
    public final long expiresAt;
    public PublicKey publicKey;
    public final byte[] keySignature;
    public PersistingData persistingData;

    public PlayerEntry(GameProfile profile, GameMode gamemode, int latency, Component displayName, long expiresAt, PublicKey publicKey, byte[] keySignature, boolean listed) {
        this.profile = profile;
        this.gamemode = gamemode;
        this.latency = latency;
        this.displayName = displayName;
        this.expiresAt = expiresAt;
        this.publicKey = publicKey;
        this.keySignature = keySignature;
        this.persistingData = new PersistingData(listed);
    }

    public PlayerEntry(PlayerListEntry entry) {
        this(entry.getProfile(), entry.getGameMode(), entry.getLatency(), entry.getDisplayName(), entry.getExpiresAt(), entry.getPublicKey(), entry.getKeySignature(), entry.isListed());
    }

    public String toString() {
        return "PlayerEntry{profile=" + String.valueOf(this.profile) + ", gamemode=" + String.valueOf(this.gamemode) + ", latency=" + this.latency + ", persistingData=" + String.valueOf(this.persistingData) + "}";
    }

    public static final class PersistingData {
        public final List<String> usernames = new ObjectArrayList<String>();
        public boolean listed;
        public String ip = null;
        public TrustLevel authenticatedTrustLevel = TrustLevel.PUBLIC;

        public PersistingData(boolean listed) {
            this.listed = listed;
        }

        public PersistingData(PersistingData friend) {
            this.usernames.addAll(friend.usernames);
            this.listed = friend.listed;
            this.ip = friend.ip;
            this.authenticatedTrustLevel = friend.authenticatedTrustLevel;
        }

        public PersistingData(PlayerEntry oldEntry) {
            PersistingData friend = oldEntry.persistingData;
            this.usernames.addAll(oldEntry.persistingData.usernames);
            this.usernames.addLast(oldEntry.profile.getName());
            this.listed = friend.listed;
            this.ip = friend.ip;
            this.authenticatedTrustLevel = friend.authenticatedTrustLevel;
        }

        @NotNull
        public String toString() {
            return "PersistingData{usernames=" + String.valueOf(this.usernames) + ", listed=" + this.listed + ", ip='" + this.ip + "', authenticatedTrustLevel=" + String.valueOf((Object)this.authenticatedTrustLevel) + "}";
        }
    }
}

