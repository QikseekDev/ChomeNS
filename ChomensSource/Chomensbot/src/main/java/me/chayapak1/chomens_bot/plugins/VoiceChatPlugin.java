/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import io.netty.buffer.Unpooled;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.logging.LogType;
import me.chayapak1.chomens_bot.data.voiceChat.ClientGroup;
import me.chayapak1.chomens_bot.data.voiceChat.RawUdpPacket;
import me.chayapak1.chomens_bot.util.FriendlyByteBuf;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import me.chayapak1.chomens_bot.voiceChat.InitializationData;
import me.chayapak1.chomens_bot.voiceChat.NetworkMessage;
import me.chayapak1.chomens_bot.voiceChat.Packet;
import me.chayapak1.chomens_bot.voiceChat.customPayload.JoinGroupPacket;
import me.chayapak1.chomens_bot.voiceChat.customPayload.SecretPacket;
import me.chayapak1.chomens_bot.voiceChat.packets.AuthenticateAckPacket;
import me.chayapak1.chomens_bot.voiceChat.packets.AuthenticatePacket;
import me.chayapak1.chomens_bot.voiceChat.packets.ConnectionCheckPacket;
import me.chayapak1.chomens_bot.voiceChat.packets.KeepAlivePacket;
import me.chayapak1.chomens_bot.voiceChat.packets.PingPacket;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;

public class VoiceChatPlugin
implements Listener,
Runnable {
    private static final Key SECRET_KEY = Key.key("voicechat:secret");
    private static final Key ADD_GROUP_KEY = Key.key("voicechat:add_group");
    private static final Key REMOVE_GROUP_KEY = Key.key("voicechat:remove_group");
    private final Bot bot;
    private InitializationData initializationData;
    private ClientVoiceChatSocket socket;
    private InetSocketAddress socketAddress;
    private boolean running = false;
    public final List<ClientGroup> groups = new ArrayList<ClientGroup>();

    public VoiceChatPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    @Override
    public void packetReceived(Session session, org.geysermc.mcprotocollib.network.packet.Packet packet) {
        if (packet instanceof ClientboundLoginPacket) {
            ClientboundLoginPacket t_packet = (ClientboundLoginPacket)packet;
            this.packetReceived(t_packet);
        } else if (packet instanceof ClientboundCustomPayloadPacket) {
            ClientboundCustomPayloadPacket t_packet = (ClientboundCustomPayloadPacket)packet;
            this.packetReceived(t_packet);
        }
    }

    private void packetReceived(ClientboundLoginPacket ignored) {
        this.bot.session.send(new ServerboundCustomPayloadPacket(Key.key("voicechat:request_secret"), new FriendlyByteBuf(Unpooled.buffer()).writeInt(18).array()));
        this.bot.session.send(new ServerboundCustomPayloadPacket(Key.key("voicechat:update_state"), new FriendlyByteBuf(Unpooled.buffer()).writeBoolean(false).array()));
        this.running = true;
    }

    private void packetReceived(ClientboundCustomPayloadPacket packet) {
        if (packet.getChannel().equals(SECRET_KEY)) {
            byte[] bytes = packet.getData();
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
            SecretPacket secretPacket = new SecretPacket().fromBytes(buf);
            this.initializationData = new InitializationData(secretPacket);
            this.socketAddress = new InetSocketAddress(secretPacket.voiceHost.isBlank() ? this.bot.options.host : secretPacket.voiceHost, this.initializationData.serverPort);
            this.socket = new ClientVoiceChatSocket(this);
            try {
                this.socket.open();
            }
            catch (Exception e) {
                this.bot.logger.error(I18nUtilities.get("voicechat.failed_connecting"));
                this.bot.logger.error(e);
                return;
            }
            Thread thread2 = new Thread((Runnable)this, "Simple Voice Chat Thread");
            thread2.start();
        } else if (packet.getChannel().equals(ADD_GROUP_KEY)) {
            byte[] bytes = packet.getData();
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
            ClientGroup group2 = ClientGroup.fromBytes(buf);
            this.groups.add(group2);
        } else if (packet.getChannel().equals(REMOVE_GROUP_KEY)) {
            byte[] bytes = packet.getData();
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
            UUID id = buf.readUUID();
            this.groups.removeIf(group -> group.id().equals(id));
        }
    }

    @Override
    public void run() {
        this.sendToServer(new NetworkMessage(new AuthenticatePacket(this.initializationData.playerUUID, this.initializationData.secret)));
        while (this.running) {
            try {
                NetworkMessage message = NetworkMessage.readPacket(this.socket.read(), this.initializationData);
                if (message == null) continue;
                Packet<? extends Packet<?>> packet = message.packet;
                if (packet instanceof PingPacket) {
                    PingPacket pingPacket = (PingPacket)packet;
                    this.sendToServer(new NetworkMessage(pingPacket));
                    continue;
                }
                if (message.packet instanceof KeepAlivePacket) {
                    this.sendToServer(new NetworkMessage(new KeepAlivePacket()));
                    continue;
                }
                if (!(message.packet instanceof AuthenticateAckPacket)) continue;
                this.sendToServer(new NetworkMessage(new ConnectionCheckPacket()));
                this.bot.logger.log(LogType.SIMPLE_VOICE_CHAT, Component.translatable(I18nUtilities.get("voicechat.connected"), Component.text(this.socketAddress.toString())));
            }
            catch (Exception e) {
                if (!this.running) break;
                this.bot.logger.error(e);
            }
        }
    }

    public void joinGroup(String group, String password) {
        ClientGroup[] clientGroups = (ClientGroup[])this.groups.stream().filter(eachGroup -> eachGroup.name().equals(group)).toArray(ClientGroup[]::new);
        if (clientGroups.length == 0) {
            throw new RuntimeException("Group " + group + " doesn't exist");
        }
        ClientGroup clientGroup = clientGroups[0];
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        new JoinGroupPacket(clientGroup.id(), password).toBytes(buf);
        this.bot.session.send(new ServerboundCustomPayloadPacket(Key.key("voicechat:set_group"), buf.array()));
    }

    public void sendToServer(NetworkMessage message) {
        try {
            this.socket.send(message.writeClient(this.initializationData), this.socketAddress);
        }
        catch (Exception e) {
            this.bot.logger.error(e);
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        if (this.socket != null) {
            this.socket.close();
        }
        this.groups.clear();
        this.running = false;
    }

    private class ClientVoiceChatSocket
    extends VoiceChatSocketBase {
        private DatagramSocket socket;

        private ClientVoiceChatSocket(VoiceChatPlugin voiceChatPlugin) {
        }

        public void open() throws SocketException {
            this.socket = new DatagramSocket();
        }

        public RawUdpPacket read() {
            if (this.socket == null) {
                throw new IllegalStateException("Socket not opened yet");
            }
            return this.read(this.socket);
        }

        public void send(byte[] data, SocketAddress address) throws Exception {
            if (this.socket == null) {
                return;
            }
            this.socket.send(new DatagramPacket(data, data.length, address));
        }

        public void close() {
            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
        }
    }

    private class VoiceChatSocketBase {
        private final byte[] BUFFER = new byte[4096];

        private VoiceChatSocketBase() {
        }

        public RawUdpPacket read(DatagramSocket socket) {
            if (socket.isClosed()) {
                return null;
            }
            try {
                DatagramPacket packet = new DatagramPacket(this.BUFFER, this.BUFFER.length);
                socket.receive(packet);
                long timestamp = System.currentTimeMillis();
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                return new RawUdpPacket(data, packet.getSocketAddress(), timestamp);
            }
            catch (Exception e) {
                if (!VoiceChatPlugin.this.running) {
                    return null;
                }
                VoiceChatPlugin.this.bot.logger.error(e);
                return null;
            }
        }
    }
}

