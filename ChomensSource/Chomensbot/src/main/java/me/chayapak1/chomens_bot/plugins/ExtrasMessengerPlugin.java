/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;

public class ExtrasMessengerPlugin
implements Listener {
    private static final Key MINECRAFT_REGISTER_KEY = Key.key("minecraft", "register");
    private static final Key EXTRAS_REGISTER_KEY = Key.key("extras", "register");
    private static final Key EXTRAS_UNREGISTER_KEY = Key.key("extras", "unregister");
    private static final Key EXTRAS_MESSAGE_KEY = Key.key("extras", "message");
    private static final String MINECRAFT_CHANNEL_SEPARATOR = "\u0000";
    private static final byte END_CHAR_MASK = -128;
    private final Bot bot;
    private final String chomens_namespace;
    public final List<String> registeredChannels = new ArrayList<String>();
    public boolean isSupported = false;

    public ExtrasMessengerPlugin(Bot bot) {
        this.bot = bot;
        this.chomens_namespace = bot.config.namespace + ":";
        bot.listener.addListener(this);
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundCustomPayloadPacket) {
            ClientboundCustomPayloadPacket t_packet = (ClientboundCustomPayloadPacket)packet;
            this.packetReceived(t_packet);
        }
    }

    private void packetReceived(ClientboundCustomPayloadPacket packet) {
        Key packetChannel = packet.getChannel();
        if (packetChannel.equals(MINECRAFT_REGISTER_KEY)) {
            String[] availableChannels = new String(packet.getData()).split(MINECRAFT_CHANNEL_SEPARATOR);
            if (availableChannels.length == 0 || Arrays.stream(availableChannels).noneMatch(channel -> channel.equals(EXTRAS_REGISTER_KEY.asString()) || channel.equals(EXTRAS_UNREGISTER_KEY.asString()) || channel.equals(EXTRAS_MESSAGE_KEY.asString()))) {
                this.isSupported = false;
                return;
            }
            this.isSupported = true;
            ArrayList<String> channels = new ArrayList<String>();
            channels.add(EXTRAS_REGISTER_KEY.asString());
            channels.add(EXTRAS_UNREGISTER_KEY.asString());
            channels.add(EXTRAS_MESSAGE_KEY.asString());
            this.bot.session.send(new ServerboundCustomPayloadPacket(Key.key("minecraft", "register"), String.join((CharSequence)MINECRAFT_CHANNEL_SEPARATOR, channels).getBytes(StandardCharsets.UTF_8)));
            ArrayList<String> oldRegisteredChannels = new ArrayList<String>(this.registeredChannels);
            this.registeredChannels.clear();
            for (String channel2 : oldRegisteredChannels) {
                this.registerChannel(channel2);
            }
        } else if (packetChannel.equals(EXTRAS_MESSAGE_KEY)) {
            ByteBuf buf = Unpooled.wrappedBuffer(packet.getData());
            String channelName = this.readString(buf);
            if (!channelName.startsWith(this.chomens_namespace)) {
                return;
            }
            UUID uuid = this.readUUID(buf);
            byte[] data = this.readByteArrayToEnd(buf);
            this.bot.executorService.execute(() -> this.bot.listener.dispatch(listener -> listener.onExtrasMessageReceived(uuid, data)));
        }
    }

    public void sendPayload(String channel, byte[] data) {
        this.sendPayload(channel, true, data);
    }

    public void sendPayload(String channel, boolean withNamespace, byte[] data) {
        if (!this.bot.loggedIn) {
            return;
        }
        ByteBuf buf = Unpooled.buffer();
        if (withNamespace) {
            this.writeString(buf, this.chomens_namespace + channel);
        } else {
            this.writeString(buf, channel);
        }
        buf.writeBytes(data);
        byte[] byteArray = this.readByteArrayToEnd(buf);
        this.bot.session.send(new ServerboundCustomPayloadPacket(EXTRAS_MESSAGE_KEY, byteArray));
    }

    public void registerChannel(String channel) {
        this.registerChannel(channel, true);
    }

    public void registerChannel(String channel, boolean withNamespace) {
        if (!this.bot.loggedIn) {
            this.registeredChannels.add(channel);
            return;
        }
        ByteBuf buf = Unpooled.buffer();
        if (withNamespace) {
            this.writeString(buf, this.chomens_namespace + channel);
        } else {
            this.writeString(buf, channel);
        }
        this.bot.session.send(new ServerboundCustomPayloadPacket(EXTRAS_REGISTER_KEY, this.readByteArrayToEnd(buf)));
        this.registeredChannels.add(channel);
    }

    public void unregisterChannel(String channel) {
        boolean removed = this.registeredChannels.remove(channel);
        if (!removed || !this.bot.loggedIn) {
            return;
        }
        ByteBuf buf = Unpooled.buffer();
        this.writeString(buf, this.chomens_namespace + channel);
        this.bot.session.send(new ServerboundCustomPayloadPacket(EXTRAS_UNREGISTER_KEY, this.readByteArrayToEnd(buf)));
    }

    private void writeString(ByteBuf input, String string) {
        byte[] bytesString = string.getBytes(StandardCharsets.US_ASCII);
        int n = bytesString.length - 1;
        bytesString[n] = (byte)(bytesString[n] | 0xFFFFFF80);
        input.writeBytes(bytesString);
    }

    private String readString(ByteBuf byteBuf) {
        boolean isLast;
        byte[] buf = new byte[255];
        int idx = 0;
        do {
            byte input = byteBuf.readByte();
            if (idx == buf.length) break;
            isLast = (input & 0xFFFFFF80) == -128;
            buf[idx++] = (byte)(input & 0x7F);
        } while (!isLast);
        return new String(Arrays.copyOf(buf, idx), StandardCharsets.US_ASCII);
    }

    private UUID readUUID(ByteBuf input) {
        long mostSignificant = input.readLong();
        long leastSignificant = input.readLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    private byte[] readByteArrayToEnd(ByteBuf input) {
        byte[] bytes = new byte[input.readableBytes()];
        input.readBytes(bytes);
        return bytes;
    }
}

