/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.voiceChat.packets;

import java.util.UUID;
import me.chayapak1.chomens_bot.util.FriendlyByteBuf;
import me.chayapak1.chomens_bot.voiceChat.Packet;

public class PingPacket
implements Packet<PingPacket> {
    public UUID id;
    public long timestamp;

    @Override
    public PingPacket fromBytes(FriendlyByteBuf buf) {
        PingPacket pingPacket = new PingPacket();
        pingPacket.id = buf.readUUID();
        pingPacket.timestamp = buf.readLong();
        return pingPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.id);
        buf.writeLong(this.timestamp);
    }
}

