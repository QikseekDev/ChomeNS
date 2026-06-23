/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.entity.EntityData;
import me.chayapak1.chomens_bot.data.entity.Rotation;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.MathUtilities;
import org.cloudburstmc.math.vector.Vector3d;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PositionElement;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundAddEntityPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundEntityPositionSyncPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundAcceptTeleportationPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;

public class PositionPlugin
implements Listener {
    private final Bot bot;
    public Vector3d position = Vector3d.from(0.0f, 0.0f, 0.0f);
    public boolean isGoingDownFromHeightLimit = false;
    private long tpCommandCooldownTime = 0L;
    public final Map<Integer, EntityData> entityIdToData = new ConcurrentHashMap<Integer, EntityData>();

    public PositionPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    @Override
    public void onTick() {
        this.handleHeightLimit();
    }

    @Override
    public void onSecondTick() {
        if (this.isGoingDownFromHeightLimit) {
            return;
        }
        this.bot.session.send(new ServerboundMovePlayerPosPacket(false, false, this.position.getX(), this.position.getY(), this.position.getZ()));
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        Packet packet2 = packet;
        Objects.requireNonNull(packet2);
        Packet packet3 = packet2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClientboundPlayerPositionPacket.class, ClientboundAddEntityPacket.class, ClientboundRemoveEntitiesPacket.class, ClientboundMoveEntityRotPacket.class, ClientboundMoveEntityPosPacket.class, ClientboundMoveEntityPosRotPacket.class, ClientboundEntityPositionSyncPacket.class}, (Object)packet3, n)) {
            case 0: {
                ClientboundPlayerPositionPacket t_packet = (ClientboundPlayerPositionPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 1: {
                ClientboundAddEntityPacket t_packet = (ClientboundAddEntityPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 2: {
                ClientboundRemoveEntitiesPacket t_packet = (ClientboundRemoveEntitiesPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 3: {
                ClientboundMoveEntityRotPacket t_packet = (ClientboundMoveEntityRotPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 4: {
                ClientboundMoveEntityPosPacket t_packet = (ClientboundMoveEntityPosPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 5: {
                ClientboundMoveEntityPosRotPacket t_packet = (ClientboundMoveEntityPosRotPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 6: {
                ClientboundEntityPositionSyncPacket t_packet = (ClientboundEntityPositionSyncPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
        }
    }

    private void packetReceived(ClientboundPlayerPositionPacket packet) {
        this.bot.session.send(new ServerboundAcceptTeleportationPacket(packet.getId()));
        List<PositionElement> relatives = packet.getRelatives();
        this.position = packet.getPosition().add(relatives.contains((Object)PositionElement.X) ? this.position.getX() : 0.0, relatives.contains((Object)PositionElement.Y) ? this.position.getY() : 0.0, relatives.contains((Object)PositionElement.Z) ? this.position.getZ() : 0.0);
        this.bot.listener.dispatch(listener -> listener.onPositionChange(this.position));
    }

    private void packetReceived(ClientboundAddEntityPacket packet) {
        if (packet.getType() != EntityType.PLAYER) {
            return;
        }
        PlayerEntry entry = this.bot.players.getEntry(packet.getUuid());
        if (entry == null) {
            return;
        }
        this.entityIdToData.remove(packet.getEntityId());
        this.entityIdToData.put(packet.getEntityId(), new EntityData(entry, Vector3d.from(packet.getX(), packet.getY(), packet.getZ()), new Rotation(packet.getYaw(), packet.getPitch())));
    }

    private void packetReceived(ClientboundRemoveEntitiesPacket packet) {
        for (int id : packet.getEntityIds()) {
            this.entityIdToData.remove(id);
        }
    }

    private void packetReceived(ClientboundEntityPositionSyncPacket packet) {
        EntityData data = this.entityIdToData.get(packet.getId());
        if (data == null) {
            return;
        }
        Vector3d position = packet.getPosition().add(packet.getDeltaMovement());
        Rotation rotation = new Rotation(packet.getXRot(), packet.getYRot());
        data.position = position;
        data.rotation = rotation;
        this.bot.listener.dispatch(listener -> listener.onPlayerMoved(data.player, position, rotation));
    }

    private void packetReceived(ClientboundMoveEntityRotPacket packet) {
        Rotation rotation;
        EntityData data = this.entityIdToData.get(packet.getEntityId());
        if (data == null) {
            return;
        }
        data.rotation = rotation = new Rotation(packet.getYaw(), packet.getPitch());
        this.bot.listener.dispatch(listener -> listener.onPlayerMoved(data.player, data.position, rotation));
    }

    private void packetReceived(ClientboundMoveEntityPosPacket packet) {
        Vector3d newPosition;
        EntityData data = this.entityIdToData.get(packet.getEntityId());
        if (data == null) {
            return;
        }
        Vector3d originalPosition = data.position;
        data.position = newPosition = originalPosition.add(packet.getMoveX(), packet.getMoveY(), packet.getMoveZ());
        this.bot.listener.dispatch(listener -> listener.onPlayerMoved(data.player, newPosition, data.rotation));
    }

    private void packetReceived(ClientboundMoveEntityPosRotPacket packet) {
        EntityData data = this.entityIdToData.get(packet.getEntityId());
        if (data == null) {
            return;
        }
        Vector3d originalPosition = data.position;
        Vector3d newPosition = originalPosition.add(packet.getMoveX(), packet.getMoveY(), packet.getMoveZ());
        Rotation rotation = new Rotation(packet.getYaw(), packet.getPitch());
        data.position = newPosition;
        data.rotation = rotation;
        this.bot.listener.dispatch(listener -> listener.onPlayerMoved(data.player, this.position, rotation));
    }

    private void handleHeightLimit() {
        Vector3d newPosition;
        double y = this.position.getY();
        int minY = this.bot.world.minY;
        int maxY = this.bot.world.maxY;
        if (y <= (double)maxY && y >= (double)minY) {
            if (this.isGoingDownFromHeightLimit) {
                this.isGoingDownFromHeightLimit = false;
                this.bot.listener.dispatch(listener -> listener.onPositionChange(this.position));
            }
            return;
        }
        this.isGoingDownFromHeightLimit = true;
        if (y > (double)(maxY + 500) || y < (double)minY) {
            if (System.currentTimeMillis() - this.tpCommandCooldownTime < 400L) {
                return;
            }
            this.tpCommandCooldownTime = System.currentTimeMillis();
            StringBuilder command = new StringBuilder();
            if (this.bot.serverFeatures.hasEssentials) {
                command.append("essentials:");
            }
            command.append(String.format("tp ~ %s ~", maxY));
            this.bot.chat.sendCommandInstantly(command.toString());
            return;
        }
        this.position = newPosition = Vector3d.from(this.position.getX(), MathUtilities.clamp(this.position.getY() - 2.0, (double)maxY, this.position.getY()), this.position.getZ());
        this.bot.session.send(new ServerboundMovePlayerPosPacket(false, false, newPosition.getX(), newPosition.getY(), newPosition.getZ()));
    }

    public Vector3d getPlayerPosition(String playerName) {
        for (EntityData data : this.entityIdToData.values()) {
            if (!data.player.profile.getName().equals(playerName)) continue;
            return data.position;
        }
        return null;
    }

    public Rotation getPlayerRotation(String playerName) {
        for (EntityData data : this.entityIdToData.values()) {
            if (!data.player.profile.getName().equals(playerName)) continue;
            return data.rotation;
        }
        return null;
    }
}

