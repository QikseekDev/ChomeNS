/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.voiceChat.customPayload;

import java.util.UUID;
import me.chayapak1.chomens_bot.data.voiceChat.Codec;
import me.chayapak1.chomens_bot.util.FriendlyByteBuf;
import me.chayapak1.chomens_bot.voiceChat.Packet;

public class SecretPacket
implements Packet<SecretPacket> {
    public UUID secret;
    public int serverPort;
    public UUID playerUUID;
    public Codec codec;
    public int mtuSize;
    public double voiceChatDistance;
    public int keepAlive;
    public boolean groupsEnabled;
    public String voiceHost;
    public boolean allowRecording;

    @Override
    public SecretPacket fromBytes(FriendlyByteBuf buf) {
        this.secret = buf.readUUID();
        this.serverPort = buf.readInt();
        this.playerUUID = buf.readUUID();
        this.codec = Codec.values()[buf.readByte()];
        this.mtuSize = buf.readInt();
        this.voiceChatDistance = buf.readDouble();
        this.keepAlive = buf.readInt();
        this.groupsEnabled = buf.readBoolean();
        this.voiceHost = buf.readUtf();
        this.allowRecording = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.secret);
        buf.writeInt(this.serverPort);
        buf.writeUUID(this.playerUUID);
        buf.writeByte(this.codec.ordinal());
        buf.writeInt(this.mtuSize);
        buf.writeDouble(this.voiceChatDistance);
        buf.writeInt(this.keepAlive);
        buf.writeBoolean(this.groupsEnabled);
        buf.writeUtf(this.voiceHost);
        buf.writeBoolean(this.allowRecording);
    }
}

