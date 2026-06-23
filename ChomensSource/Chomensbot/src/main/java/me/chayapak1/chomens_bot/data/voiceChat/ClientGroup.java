/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.voiceChat;

import java.util.Arrays;
import java.util.UUID;
import me.chayapak1.chomens_bot.data.voiceChat.GroupType;
import me.chayapak1.chomens_bot.util.FriendlyByteBuf;

public record ClientGroup(UUID id, String name, boolean hasPassword, boolean persistent, GroupType type) {
    public static ClientGroup fromBytes(FriendlyByteBuf buf) {
        return new ClientGroup(buf.readUUID(), buf.readUtf(512), buf.readBoolean(), buf.readBoolean(), GroupType.values()[buf.readShort()]);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.id);
        buf.writeUtf(this.name, 512);
        buf.writeBoolean(this.hasPassword);
        buf.writeBoolean(this.persistent);
        buf.writeShort(Arrays.stream(GroupType.values()).toList().indexOf((Object)this.type));
    }
}

