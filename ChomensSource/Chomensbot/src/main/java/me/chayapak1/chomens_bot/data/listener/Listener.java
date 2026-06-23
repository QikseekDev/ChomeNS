/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.listener;

import java.util.UUID;
import me.chayapak1.chomens_bot.chomeNSMod.Packet;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.chat.PlayerMessage;
import me.chayapak1.chomens_bot.data.entity.Rotation;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import net.kyori.adventure.text.Component;
import org.cloudburstmc.math.vector.Vector3d;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.ConnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.DisconnectingEvent;
import org.geysermc.mcprotocollib.network.event.session.PacketErrorEvent;
import org.geysermc.mcprotocollib.network.event.session.PacketSendingEvent;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;

public interface Listener {
    default public void packetReceived(Session session, org.geysermc.mcprotocollib.network.packet.Packet packet) {
    }

    default public void packetSending(PacketSendingEvent event) {
    }

    default public void packetSent(Session session, org.geysermc.mcprotocollib.network.packet.Packet packet) {
    }

    default public void packetError(PacketErrorEvent event) {
    }

    default public void connected(ConnectedEvent event) {
    }

    default public void disconnecting(DisconnectingEvent event) {
    }

    default public void disconnected(DisconnectedEvent event) {
    }

    default public void onConnecting() {
    }

    default public void onTick() {
    }

    default public void onLocalTick() {
    }

    default public void onSecondTick() {
    }

    default public void onLocalSecondTick() {
    }

    default public void onCoreReady() {
    }

    default public void onCoreRefilled() {
    }

    default public void onPositionChange(Vector3d position) {
    }

    default public void onPlayerMoved(PlayerEntry player, Vector3d position, Rotation rotation) {
    }

    default public void onWorldChanged(String dimension) {
    }

    default public void onPlayerJoined(PlayerEntry target) {
    }

    default public void onPlayerUnVanished(PlayerEntry target) {
    }

    default public void onPlayerGameModeUpdated(PlayerEntry target, GameMode gameMode) {
    }

    default public void onPlayerLatencyUpdated(PlayerEntry target, int ping) {
    }

    default public void onPlayerDisplayNameUpdated(PlayerEntry target, Component displayName) {
    }

    default public void onPlayerLeft(PlayerEntry target) {
    }

    default public void onPlayerVanished(PlayerEntry target) {
    }

    default public void onPlayerChangedUsername(PlayerEntry target, String from, String to) {
    }

    default public void onQueriedPlayerIP(PlayerEntry target, String ip) {
    }

    default public void onChomeNSModPacketReceived(PlayerEntry player, Packet packet) {
    }

    default public boolean onPlayerMessageReceived(PlayerMessage message, ChatPacketType packetType) {
        return true;
    }

    default public boolean onSystemMessageReceived(Component component, ChatPacketType packetType, String string, String ansi) {
        return true;
    }

    default public void onCommandSpyMessageReceived(PlayerEntry sender, String command) {
    }

    default public void onExtrasMessageReceived(UUID sender, byte[] message) {
    }
}

