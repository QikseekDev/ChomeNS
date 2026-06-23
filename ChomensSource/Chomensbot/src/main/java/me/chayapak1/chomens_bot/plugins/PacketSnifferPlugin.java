/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.PacketSendingEvent;
import org.geysermc.mcprotocollib.network.packet.Packet;

public class PacketSnifferPlugin
implements Listener {
    private final Bot bot;
    public boolean enabled = false;
    private BufferedWriter writer;

    public PacketSnifferPlugin(Bot bot) {
        this.bot = bot;
        if (this.enabled) {
            this.enable();
        }
        bot.listener.addListener(this);
    }

    public void enable() {
        this.enabled = true;
        String name = String.format("packets-%s-%s.log", this.bot.options.host, this.bot.options.port);
        Path path = Path.of(name, new String[0]);
        try {
            if (!Files.exists(path, new LinkOption[0])) {
                Files.createFile(path, new FileAttribute[0]);
            }
            this.writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            this.bot.logger.error(e);
        }
    }

    public void disable() {
        this.enabled = false;
        try {
            this.writer.flush();
            this.writer.close();
        }
        catch (IOException e) {
            this.bot.logger.error(e);
        }
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (!this.enabled) {
            return;
        }
        try {
            this.writer.write(packet.toString() + "\n");
            this.writer.flush();
        }
        catch (IOException e) {
            this.bot.logger.error(e);
        }
    }

    @Override
    public void packetSending(PacketSendingEvent event) {
        if (!this.enabled) {
            return;
        }
        try {
            this.writer.write(event.getPacket().toString() + "\n");
            this.writer.flush();
        }
        catch (IOException e) {
            this.bot.logger.error(e);
        }
    }
}

