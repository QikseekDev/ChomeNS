/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.vanilla;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.selfCare.SelfCare;
import me.chayapak1.chomens_bot.data.selfCare.SelfData;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EntityEvent;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundEntityEventPacket;

public class OperatorSelfCare
extends SelfCare {
    public OperatorSelfCare(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return this.bot.config.selfCare.op;
    }

    @Override
    public void onPacketReceived(Packet packet) {
        if (packet instanceof ClientboundEntityEventPacket) {
            ClientboundEntityEventPacket t_packet = (ClientboundEntityEventPacket)packet;
            this.onPacketReceived(t_packet);
        }
    }

    private void onPacketReceived(ClientboundEntityEventPacket packet) {
        int permissionLevel;
        EntityEvent event = packet.getEvent();
        int id = packet.getEntityId();
        SelfData data = this.bot.selfCare.data;
        if (id != data.entityId) {
            return;
        }
        switch (event) {
            case PLAYER_OP_PERMISSION_LEVEL_0: {
                int n = 0;
                break;
            }
            case PLAYER_OP_PERMISSION_LEVEL_1: {
                int n = 1;
                break;
            }
            case PLAYER_OP_PERMISSION_LEVEL_2: {
                int n = 2;
                break;
            }
            case PLAYER_OP_PERMISSION_LEVEL_3: {
                int n = 3;
                break;
            }
            case PLAYER_OP_PERMISSION_LEVEL_4: {
                int n = 4;
                break;
            }
            default: {
                int n = permissionLevel = -1;
            }
        }
        if (permissionLevel != -1) {
            data.permissionLevel = permissionLevel;
        }
        this.needsRunning = data.permissionLevel < 2;
    }

    @Override
    public void run() {
        this.bot.chat.sendCommandInstantly("minecraft:op @s[type=player]");
    }
}

