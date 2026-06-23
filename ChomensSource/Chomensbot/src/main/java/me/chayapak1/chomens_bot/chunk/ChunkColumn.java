/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.chunk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.chayapak1.chomens_bot.data.chunk.ChunkPos;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;

public class ChunkColumn {
    public final ChunkPos pos;
    public final ChunkSection[] chunks;
    private final int minY;

    public ChunkColumn(ChunkPos chunkPos, byte[] data, int worldHeight, int minY) {
        this.pos = chunkPos;
        this.minY = minY;
        int absoluteWorldHeight = Math.abs(worldHeight);
        int absoluteMinY = Math.abs(minY);
        ByteBuf in = Unpooled.wrappedBuffer(data);
        int numSections = -Math.floorDiv(-(absoluteWorldHeight + absoluteMinY), 16);
        this.chunks = new ChunkSection[numSections];
        for (int i = 0; i < numSections; ++i) {
            this.chunks[i] = MinecraftTypes.readChunkSection(in);
        }
    }

    public int getBlock(int x, int y, int z) {
        if (this.chunks == null) {
            return 0;
        }
        int yIndex = y - this.minY >> 4;
        if (yIndex >= this.chunks.length) {
            return 0;
        }
        return this.chunks[yIndex].getBlock(x, y & 0xF, z);
    }

    public void setBlock(int x, int y, int z, int id) {
        int yIndex = y - this.minY >> 4;
        if (yIndex >= this.chunks.length) {
            return;
        }
        try {
            if (this.chunks[yIndex] == null) {
                this.chunks[yIndex] = new ChunkSection();
                this.chunks[yIndex].setBlock(0, 0, 0, 0);
            }
            this.chunks[yIndex].setBlock(x, y & 0xF, z, id);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

