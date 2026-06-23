/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.chomeNSMod;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public record PayloadMetadata(byte[] nonce, long timestamp) {
    public static PayloadMetadata deserialize(ByteBuf buf) {
        byte[] nonce = new byte[8];
        buf.readBytes(nonce);
        long timestamp = buf.readLong();
        return new PayloadMetadata(nonce, timestamp);
    }

    public void serialize(ByteBuf buf) {
        buf.writeBytes(this.nonce);
        buf.writeLong(this.timestamp);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        PayloadMetadata metadata = (PayloadMetadata)object;
        return this.timestamp == metadata.timestamp && Arrays.equals(this.nonce, metadata.nonce);
    }

    @Override
    @NotNull
    public String toString() {
        return "PayloadMetadata{nonce=" + Arrays.toString(this.nonce) + ", timestamp=" + this.timestamp + "}";
    }
}

