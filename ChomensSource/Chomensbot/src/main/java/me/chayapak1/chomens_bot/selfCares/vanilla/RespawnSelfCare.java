/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.vanilla;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.selfCare.SelfCare;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.ClientCommand;
import org.geysermc.mcprotocollib.protocol.data.game.level.notify.GameEvent;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerCombatKillPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundClientCommandPacket;

public class RespawnSelfCare
extends SelfCare {
    public RespawnSelfCare(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return this.bot.config.selfCare.respawn;
    }

    @Override
    public void onPacketReceived(Packet packet) {
        if (packet instanceof ClientboundGameEventPacket) {
            ClientboundGameEventPacket t_packet = (ClientboundGameEventPacket)packet;
            this.onPacketReceived(t_packet);
        } else if (packet instanceof ClientboundPlayerCombatKillPacket) {
            ClientboundPlayerCombatKillPacket t_packet = (ClientboundPlayerCombatKillPacket)packet;
            this.onPacketReceived(t_packet);
        }
    }

    private void onPacketReceived(ClientboundGameEventPacket packet) {
        if (packet.getNotification() == GameEvent.WIN_GAME) {
            this.needsRunning = true;
        }
    }

    private void onPacketReceived(ClientboundPlayerCombatKillPacket packet) {
        if (packet.getPlayerId() == this.bot.selfCare.data.entityId) {
            this.needsRunning = true;
        }
    }

    @Override
    public void run() {
        this.bot.session.send(new ServerboundClientCommandPacket(ClientCommand.RESPAWN));
    }
}

