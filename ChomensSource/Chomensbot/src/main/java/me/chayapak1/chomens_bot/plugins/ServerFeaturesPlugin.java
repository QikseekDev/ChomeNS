/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;

public class ServerFeaturesPlugin
implements Listener {
    private final Bot bot;
    private final List<String> commands = new ObjectArrayList<String>();
    public boolean hasNamespaces = false;
    public boolean hasEssentials = false;
    public boolean hasExtras = false;
    public boolean hasIControlU = false;
    public boolean hasCommandSpy = false;

    public ServerFeaturesPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundCommandsPacket) {
            ClientboundCommandsPacket t_packet = (ClientboundCommandsPacket)packet;
            this.packetReceived(t_packet);
        }
    }

    private void packetReceived(ClientboundCommandsPacket packet) {
        this.commands.clear();
        block12: for (CommandNode node : packet.getNodes()) {
            String key;
            String name;
            if (!node.isExecutable() || node.getType() != CommandType.LITERAL || (name = node.getName()) == null) continue;
            this.commands.add(name);
            if (name.contains(":")) {
                this.hasNamespaces = true;
            }
            if (!name.contains(":")) continue;
            if (this.bot.selfCare.data.permissionLevel < 4) {
                if (!name.equals("minecraft:op")) continue;
                this.hasExtras = true;
                continue;
            }
            String[] split = name.split(":");
            if (split.length < 2) continue;
            switch (key = split[0].toLowerCase()) {
                case "extras": {
                    this.hasExtras = true;
                    continue block12;
                }
                case "essentials": {
                    this.hasEssentials = true;
                    continue block12;
                }
                case "icontrolu": {
                    this.hasIControlU = true;
                    continue block12;
                }
                case "commandspy": {
                    this.hasCommandSpy = true;
                }
            }
        }
    }

    public boolean serverHasCommand(String command) {
        return this.commands.contains(command);
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.hasNamespaces = false;
        this.hasEssentials = false;
        this.hasExtras = false;
        this.hasIControlU = false;
        this.hasCommandSpy = false;
    }
}

