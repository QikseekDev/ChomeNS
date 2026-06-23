/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.chomeNSMod;

import io.netty.buffer.ByteBuf;

public interface Packet {
    public int getId();

    public void serialize(ByteBuf var1);
}

