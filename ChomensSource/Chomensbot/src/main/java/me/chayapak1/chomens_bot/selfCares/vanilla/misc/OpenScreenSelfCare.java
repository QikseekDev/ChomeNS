/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.vanilla.misc;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.selfCare.SelfCare;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClosePacket;

public class OpenScreenSelfCare
extends SelfCare {
    public OpenScreenSelfCare(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return false;
    }

    @Override
    public void onPacketReceived(Packet packet) {
        if (packet instanceof ClientboundOpenScreenPacket) {
            ClientboundOpenScreenPacket t_packet = (ClientboundOpenScreenPacket)packet;
            this.onPacketReceived(t_packet);
        }
    }

    private void onPacketReceived(ClientboundOpenScreenPacket packet) {
        this.bot.session.send(new ServerboundContainerClosePacket(packet.getContainerId()));
    }
}

