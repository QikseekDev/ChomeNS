/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.vanilla.misc;

import java.util.Arrays;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.selfCare.SelfCare;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.InteractAction;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetPassengersPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundPlayerInputPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundInteractPacket;

public class RideSelfCare
extends SelfCare {
    public RideSelfCare(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return false;
    }

    @Override
    public void onPacketReceived(Packet packet) {
        if (packet instanceof ClientboundSetPassengersPacket) {
            ClientboundSetPassengersPacket t_packet = (ClientboundSetPassengersPacket)packet;
            this.onPacketReceived(t_packet);
        }
    }

    private void onPacketReceived(ClientboundSetPassengersPacket packet) {
        int entityId = this.bot.selfCare.data.entityId;
        if (Arrays.stream(packet.getPassengerIds()).noneMatch(id -> id == entityId)) {
            return;
        }
        this.bot.session.send(new ServerboundPlayerInputPacket(false, false, false, false, false, true, false));
        this.bot.session.send(new ServerboundInteractPacket(-1, InteractAction.INTERACT, true));
    }
}

