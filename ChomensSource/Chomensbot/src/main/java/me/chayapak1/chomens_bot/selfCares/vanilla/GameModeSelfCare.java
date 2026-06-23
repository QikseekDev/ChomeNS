/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.vanilla;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.selfCare.SelfCare;
import me.chayapak1.chomens_bot.data.selfCare.SelfData;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.level.notify.GameEvent;
import org.geysermc.mcprotocollib.protocol.data.game.level.notify.GameEventValue;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundChangeGameModePacket;

public class GameModeSelfCare
extends SelfCare {
    public GameModeSelfCare(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return this.bot.config.selfCare.gamemode;
    }

    @Override
    public void onPacketReceived(Packet packet) {
        if (packet instanceof ClientboundLoginPacket) {
            ClientboundLoginPacket t_packet = (ClientboundLoginPacket)packet;
            this.onPacketReceived(t_packet);
        } else if (packet instanceof ClientboundGameEventPacket) {
            ClientboundGameEventPacket t_packet = (ClientboundGameEventPacket)packet;
            this.onPacketReceived(t_packet);
        }
    }

    private void onPacketReceived(ClientboundLoginPacket packet) {
        SelfData data = this.bot.selfCare.data;
        data.gameMode = packet.getCommonPlayerSpawnInfo().getGameMode();
        this.needsRunning = data.gameMode != GameMode.CREATIVE;
    }

    private void onPacketReceived(ClientboundGameEventPacket packet) {
        GameEvent notification = packet.getNotification();
        GameEventValue value = packet.getValue();
        SelfData data = this.bot.selfCare.data;
        if (notification == GameEvent.CHANGE_GAME_MODE) {
            data.gameMode = (GameMode)value;
        }
        this.needsRunning = data.gameMode != GameMode.CREATIVE;
    }

    @Override
    public void run() {
        this.bot.session.send(new ServerboundChangeGameModePacket(GameMode.CREATIVE));
    }
}

