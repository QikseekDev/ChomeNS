/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.ConnectedEvent;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.RemoteDebugSampleType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundDebugSamplePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetTimePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundDebugSampleSubscriptionPacket;

public class TickPlugin
implements Listener {
    private final Bot bot;
    public final AtomicLong lastTickTime = new AtomicLong();
    public final AtomicLong lastSecondTickTime = new AtomicLong();
    private boolean receivedDebugSample = false;
    private final AtomicLong lastDebugSubscriptionTime = new AtomicLong();

    public TickPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
        bot.executor.scheduleAtFixedRate(this::tick, 0L, 50L, TimeUnit.MILLISECONDS);
        bot.executor.scheduleAtFixedRate(this::tickLocalSecond, 0L, 1L, TimeUnit.SECONDS);
    }

    @Override
    public void connected(ConnectedEvent event) {
        this.resubscribeDebug();
    }

    private void tick() {
        if (!this.bot.loggedIn) {
            return;
        }
        this.bot.listener.dispatch(listener -> {
            try {
                listener.onLocalTick();
            }
            catch (Throwable e) {
                this.bot.logger.error("Caught exception in a local tick listener!");
                this.bot.logger.error(e);
            }
        });
        if (!this.receivedDebugSample || this.bot.selfCare.data.permissionLevel < 2) {
            this.dispatchTick();
        }
    }

    private void tickLocalSecond() {
        if (!this.bot.loggedIn) {
            return;
        }
        if (System.currentTimeMillis() - this.lastDebugSubscriptionTime.get() >= 5000L) {
            this.resubscribeDebug();
            this.lastDebugSubscriptionTime.set(System.currentTimeMillis());
        }
        this.bot.listener.dispatch(listener -> {
            try {
                listener.onLocalSecondTick();
            }
            catch (Throwable e) {
                this.bot.logger.error("Caught exception in a local second tick listener!");
                this.bot.logger.error(e);
            }
        });
    }

    private void resubscribeDebug() {
        this.bot.session.send(new ServerboundDebugSampleSubscriptionPacket(RemoteDebugSampleType.TICK_TIME));
    }

    private void dispatchTick() {
        this.bot.listener.dispatch(listener -> {
            try {
                listener.onTick();
            }
            catch (Throwable e) {
                this.bot.logger.error("Caught exception in a tick listener!");
                this.bot.logger.error(e);
            }
        });
        this.lastTickTime.set(System.currentTimeMillis());
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundSetTimePacket) {
            ClientboundSetTimePacket t_packet = (ClientboundSetTimePacket)packet;
            this.packetReceived(t_packet);
        } else if (packet instanceof ClientboundDebugSamplePacket) {
            ClientboundDebugSamplePacket t_packet = (ClientboundDebugSamplePacket)packet;
            this.packetReceived(t_packet);
        }
    }

    private void packetReceived(ClientboundSetTimePacket ignoredPacket) {
        this.bot.listener.dispatch(listener -> {
            try {
                listener.onSecondTick();
            }
            catch (Throwable e) {
                this.bot.logger.error("Caught exception in a server time update listener!");
                this.bot.logger.error(e);
            }
        });
        this.lastSecondTickTime.set(System.currentTimeMillis());
    }

    private void packetReceived(ClientboundDebugSamplePacket packet) {
        if (packet.getDebugSampleType() != RemoteDebugSampleType.TICK_TIME) {
            return;
        }
        this.receivedDebugSample = true;
        this.dispatchTick();
    }
}

