/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandSuggestionsPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundCommandSuggestionPacket;

public class TabCompletePlugin
implements Listener {
    private final Bot bot;
    private final AtomicInteger nextTransactionId = new AtomicInteger();
    private final Map<Integer, CompletableFuture<ClientboundCommandSuggestionsPacket>> transactions = new Object2ObjectOpenHashMap<Integer, CompletableFuture<ClientboundCommandSuggestionsPacket>>();

    public TabCompletePlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    public CompletableFuture<ClientboundCommandSuggestionsPacket> complete(String command) {
        if (!this.bot.loggedIn) {
            return null;
        }
        int transactionId = this.nextTransactionId.getAndIncrement();
        this.bot.session.send(new ServerboundCommandSuggestionPacket(transactionId, command));
        CompletableFuture<ClientboundCommandSuggestionsPacket> future = new CompletableFuture<ClientboundCommandSuggestionsPacket>();
        this.transactions.put(transactionId, future);
        return future;
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundCommandSuggestionsPacket) {
            ClientboundCommandSuggestionsPacket t_packet = (ClientboundCommandSuggestionsPacket)packet;
            this.packetReceived(t_packet);
        }
    }

    private void packetReceived(ClientboundCommandSuggestionsPacket packet) {
        int id = packet.getTransactionId();
        CompletableFuture<ClientboundCommandSuggestionsPacket> future = this.transactions.remove(id);
        if (future != null) {
            future.complete(packet);
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.nextTransactionId.set(0);
        this.transactions.clear();
    }
}

