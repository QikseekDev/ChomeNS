/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.chomeNSMod;

import java.util.concurrent.CompletableFuture;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.chomeNSMod.Packet;
import me.chayapak1.chomens_bot.chomeNSMod.clientboundPackets.ClientboundCoreOutputPacket;
import me.chayapak1.chomens_bot.chomeNSMod.serverboundPackets.ServerboundRunCommandPacket;
import me.chayapak1.chomens_bot.chomeNSMod.serverboundPackets.ServerboundRunCoreCommandPacket;
import me.chayapak1.chomens_bot.command.contexts.ChomeNSModCommandContext;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import net.kyori.adventure.text.Component;

public class PacketHandler {
    private final Bot bot;

    public PacketHandler(Bot bot) {
        this.bot = bot;
    }

    public void handlePacket(PlayerEntry player, Packet packet) {
        if (packet instanceof ServerboundRunCoreCommandPacket) {
            ServerboundRunCoreCommandPacket t_packet = (ServerboundRunCoreCommandPacket)packet;
            this.handlePacket(player, t_packet);
        } else if (packet instanceof ServerboundRunCommandPacket) {
            ServerboundRunCommandPacket t_packet = (ServerboundRunCommandPacket)packet;
            this.handlePacket(player, t_packet);
        }
    }

    private void handlePacket(PlayerEntry player, ServerboundRunCoreCommandPacket packet) {
        CompletableFuture<Component> future = this.bot.core.runTracked(packet.command);
        if (future == null) {
            this.bot.chomeNSMod.send(player, new ClientboundCoreOutputPacket(packet.runID, Component.empty()));
            return;
        }
        future.thenApply(output -> {
            this.bot.chomeNSMod.send(player, new ClientboundCoreOutputPacket(packet.runID, (Component)output));
            return null;
        });
    }

    private void handlePacket(PlayerEntry player, ServerboundRunCommandPacket packet) {
        String input = packet.input;
        ChomeNSModCommandContext context = new ChomeNSModCommandContext(this.bot, player);
        this.bot.commandHandler.executeCommand(input, context);
    }
}

