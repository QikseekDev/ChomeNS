/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.voiceChat.customPayload;

import java.util.UUID;
import me.chayapak1.chomens_bot.util.FriendlyByteBuf;
import me.chayapak1.chomens_bot.voiceChat.Packet;

public class JoinGroupPacket
implements Packet<JoinGroupPacket> {
    public UUID group;
    public String password;

    public JoinGroupPacket() {
    }

    public JoinGroupPacket(UUID group, String password) {
        this.group = group;
        this.password = password;
    }

    @Override
    public JoinGroupPacket fromBytes(FriendlyByteBuf buf) {
        this.group = buf.readUUID();
        if (buf.readBoolean()) {
            this.password = buf.readUtf(512);
        }
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.group);
        buf.writeBoolean(this.password != null);
        if (this.password != null) {
            buf.writeUtf(this.password, 512);
        }
    }
}

