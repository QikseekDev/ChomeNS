/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.voiceChat;

import me.chayapak1.chomens_bot.util.FriendlyByteBuf;

public interface Packet<T extends Packet<T>> {
    public T fromBytes(FriendlyByteBuf var1);

    public void toBytes(FriendlyByteBuf var1);
}

