/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.chunk.ChunkColumn;
import me.chayapak1.chomens_bot.data.chunk.ChunkPos;
import me.chayapak1.chomens_bot.data.listener.Listener;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.RegistryEntry;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundRegistryDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundRespawnPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundBlockUpdatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSectionBlocksUpdatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetSimulationDistancePacket;

public class WorldPlugin
implements Listener {
    private final Bot bot;
    public int minY = 0;
    public int maxY = 256;
    public int simulationDistance = 8;
    public String currentDimension = "";
    private final Map<ChunkPos, ChunkColumn> chunks = new Object2ObjectOpenHashMap<ChunkPos, ChunkColumn>();
    public List<RegistryEntry> registry = null;

    public WorldPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        Packet packet2 = packet;
        Objects.requireNonNull(packet2);
        Packet packet3 = packet2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClientboundLevelChunkWithLightPacket.class, ClientboundForgetLevelChunkPacket.class, ClientboundBlockUpdatePacket.class, ClientboundSectionBlocksUpdatePacket.class, ClientboundLoginPacket.class, ClientboundRespawnPacket.class, ClientboundRegistryDataPacket.class, ClientboundSetSimulationDistancePacket.class}, (Object)packet3, n)) {
            case 0: {
                ClientboundLevelChunkWithLightPacket t_packet = (ClientboundLevelChunkWithLightPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 1: {
                ClientboundForgetLevelChunkPacket t_packet = (ClientboundForgetLevelChunkPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 2: {
                ClientboundBlockUpdatePacket t_packet = (ClientboundBlockUpdatePacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 3: {
                ClientboundSectionBlocksUpdatePacket t_packet = (ClientboundSectionBlocksUpdatePacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 4: {
                ClientboundLoginPacket t_packet = (ClientboundLoginPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 5: {
                ClientboundRespawnPacket t_packet = (ClientboundRespawnPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 6: {
                ClientboundRegistryDataPacket t_packet = (ClientboundRegistryDataPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 7: {
                ClientboundSetSimulationDistancePacket t_packet = (ClientboundSetSimulationDistancePacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.chunks.clear();
    }

    private void worldChanged(String dimension) {
        this.currentDimension = dimension;
        RegistryEntry currentDimension = this.registry.stream().filter(eachDimension -> eachDimension.getId().asString().equals(dimension)).findFirst().orElse(null);
        if (currentDimension == null) {
            return;
        }
        NbtMap data = currentDimension.getData();
        if (data == null) {
            return;
        }
        this.minY = data.getInt("min_y");
        this.maxY = data.getInt("height") + this.minY;
        this.bot.listener.dispatch(listener -> listener.onWorldChanged(dimension));
    }

    private void packetReceived(ClientboundRegistryDataPacket packet) {
        if (!packet.getRegistry().value().equals("dimension_type")) {
            return;
        }
        this.registry = packet.getEntries();
    }

    private void packetReceived(ClientboundLoginPacket packet) {
        this.simulationDistance = packet.getSimulationDistance();
        this.worldChanged(packet.getCommonPlayerSpawnInfo().getWorldName().asString());
    }

    private void packetReceived(ClientboundRespawnPacket packet) {
        this.worldChanged(packet.getCommonPlayerSpawnInfo().getWorldName().asString());
    }

    private void packetReceived(ClientboundSetSimulationDistancePacket packet) {
        this.simulationDistance = packet.getSimulationDistance();
    }

    private void packetReceived(ClientboundLevelChunkWithLightPacket packet) {
        ChunkPos pos = new ChunkPos(packet.getX(), packet.getZ());
        ChunkColumn column = new ChunkColumn(pos, packet.getChunkData(), this.maxY, this.minY);
        this.chunks.put(pos, column);
    }

    private void packetReceived(ClientboundForgetLevelChunkPacket packet) {
        this.chunks.remove(new ChunkPos(packet.getX(), packet.getZ()));
    }

    private void packetReceived(ClientboundBlockUpdatePacket packet) {
        Vector3i position = packet.getEntry().getPosition();
        int id = packet.getEntry().getBlock();
        this.setBlock(position.getX(), position.getY(), position.getZ(), id);
    }

    private void packetReceived(ClientboundSectionBlocksUpdatePacket packet) {
        for (BlockChangeEntry entry : packet.getEntries()) {
            Vector3i position = entry.getPosition();
            int id = entry.getBlock();
            this.setBlock(position.getX(), position.getY(), position.getZ(), id);
        }
    }

    public ChunkColumn getChunk(int x, int z) {
        return this.chunks.get(new ChunkPos(x, z));
    }

    public ChunkColumn getChunk(ChunkPos pos) {
        return this.chunks.get(pos);
    }

    public Collection<ChunkColumn> getChunks() {
        return this.chunks.values();
    }

    public int getBlock(int x, int y, int z) {
        ChunkPos chunkPos = new ChunkPos(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        ChunkColumn chunk = this.chunks.get(chunkPos);
        try {
            return chunk == null ? 0 : this.chunks.get(chunkPos).getBlock(x & 0xF, y, z & 0xF);
        }
        catch (Exception e) {
            return 0;
        }
    }

    public void setBlock(int x, int y, int z, int id) {
        ChunkPos chunkPos = new ChunkPos(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        if (!this.chunks.containsKey(chunkPos)) {
            return;
        }
        this.chunks.get(chunkPos).setBlock(x & 0xF, y, z & 0xF, id);
    }
}

