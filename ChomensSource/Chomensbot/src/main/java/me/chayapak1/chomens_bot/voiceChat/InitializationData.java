/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.voiceChat;

import java.util.UUID;
import me.chayapak1.chomens_bot.data.voiceChat.Codec;
import me.chayapak1.chomens_bot.voiceChat.customPayload.SecretPacket;

public class InitializationData {
    public final int serverPort;
    public final UUID playerUUID;
    public final UUID secret;
    public final Codec codec;
    public final int mtuSize;
    public final double voiceChatDistance;
    public final int keepAlive;
    public final boolean groupsEnabled;
    public final boolean allowRecording;

    public InitializationData(SecretPacket secretPacket) {
        this.serverPort = secretPacket.serverPort;
        this.playerUUID = secretPacket.playerUUID;
        this.secret = secretPacket.secret;
        this.codec = secretPacket.codec;
        this.mtuSize = secretPacket.mtuSize;
        this.voiceChatDistance = secretPacket.voiceChatDistance;
        this.keepAlive = secretPacket.keepAlive;
        this.groupsEnabled = secretPacket.groupsEnabled;
        this.allowRecording = secretPacket.allowRecording;
    }
}

