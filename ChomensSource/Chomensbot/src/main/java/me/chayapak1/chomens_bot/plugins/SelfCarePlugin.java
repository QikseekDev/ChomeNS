/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.util.ArrayList;
import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.data.selfCare.SelfCare;
import me.chayapak1.chomens_bot.data.selfCare.SelfData;
import me.chayapak1.chomens_bot.selfCares.essentials.GodModeSelfCare;
import me.chayapak1.chomens_bot.selfCares.essentials.MuteSelfCare;
import me.chayapak1.chomens_bot.selfCares.essentials.NicknameSelfCare;
import me.chayapak1.chomens_bot.selfCares.essentials.SocialSpySelfCare;
import me.chayapak1.chomens_bot.selfCares.essentials.VanishSelfCare;
import me.chayapak1.chomens_bot.selfCares.kaboom.commandSpy.CommandSpySelfCare;
import me.chayapak1.chomens_bot.selfCares.kaboom.extras.PrefixSelfCare;
import me.chayapak1.chomens_bot.selfCares.kaboom.extras.UsernameSelfCare;
import me.chayapak1.chomens_bot.selfCares.kaboom.icu.IControlUSelfCare;
import me.chayapak1.chomens_bot.selfCares.vanilla.GameModeSelfCare;
import me.chayapak1.chomens_bot.selfCares.vanilla.OperatorSelfCare;
import me.chayapak1.chomens_bot.selfCares.vanilla.RespawnSelfCare;
import me.chayapak1.chomens_bot.selfCares.vanilla.misc.OpenScreenSelfCare;
import me.chayapak1.chomens_bot.selfCares.vanilla.misc.RideSelfCare;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;

public class SelfCarePlugin
implements Listener {
    private final Bot bot;
    public SelfData data;
    private final List<SelfCare> selfCares = new ArrayList<SelfCare>();
    private long lastTickTime = 0L;

    public SelfCarePlugin(Bot bot) {
        this.bot = bot;
        this.selfCares.add(new OperatorSelfCare(bot));
        this.selfCares.add(new GameModeSelfCare(bot));
        this.selfCares.add(new RespawnSelfCare(bot));
        this.selfCares.add(new OpenScreenSelfCare(bot));
        this.selfCares.add(new RideSelfCare(bot));
        this.selfCares.add(new CommandSpySelfCare(bot));
        this.selfCares.add(new PrefixSelfCare(bot));
        this.selfCares.add(new UsernameSelfCare(bot));
        this.selfCares.add(new IControlUSelfCare(bot));
        this.selfCares.add(new VanishSelfCare(bot));
        this.selfCares.add(new NicknameSelfCare(bot));
        this.selfCares.add(new GodModeSelfCare(bot));
        this.selfCares.add(new SocialSpySelfCare(bot));
        this.selfCares.add(new MuteSelfCare(bot));
        bot.listener.addListener(this);
    }

    @Override
    public boolean onSystemMessageReceived(Component component, ChatPacketType packetType, String string, String ansi) {
        for (SelfCare selfCare : this.selfCares) {
            selfCare.onMessageReceived(component, string);
        }
        return true;
    }

    @Override
    public void onTick() {
        if (System.currentTimeMillis() - this.lastTickTime < (long)this.bot.options.selfCareDelay) {
            return;
        }
        for (SelfCare selfCare : this.selfCares) {
            if (!selfCare.needsRunning || !selfCare.shouldRun()) continue;
            selfCare.run();
            this.lastTickTime = System.currentTimeMillis();
            break;
        }
    }

    @Override
    public void onCommandSpyMessageReceived(PlayerEntry sender, String command) {
        for (SelfCare selfCare : this.selfCares) {
            selfCare.onCommandSpyMessageReceived(sender, command);
        }
    }

    @Override
    public void onPlayerChangedUsername(PlayerEntry target, String from, String to) {
        for (SelfCare selfCare : this.selfCares) {
            selfCare.onPlayerChangedUsername(target, from, to);
        }
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundLoginPacket) {
            ClientboundLoginPacket t_packet = (ClientboundLoginPacket)packet;
            this.packetReceived(t_packet);
        }
        for (SelfCare selfCare : this.selfCares) {
            selfCare.onPacketReceived(packet);
        }
    }

    private void packetReceived(ClientboundLoginPacket packet) {
        this.data = new SelfData(packet.getEntityId());
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.data = null;
        for (SelfCare selfCare : this.selfCares) {
            selfCare.cleanup();
        }
    }

    public <T extends SelfCare> T find(Class<T> clazz) {
        for (SelfCare selfCare : this.selfCares) {
            if (!clazz.isInstance(selfCare)) continue;
            return (T)((SelfCare)clazz.cast(selfCare));
        }
        return null;
    }
}

