/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.voiceChat;

import io.netty.buffer.Unpooled;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import me.chayapak1.chomens_bot.data.voiceChat.RawUdpPacket;
import me.chayapak1.chomens_bot.util.AESUtilities;
import me.chayapak1.chomens_bot.util.FriendlyByteBuf;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import me.chayapak1.chomens_bot.voiceChat.InitializationData;
import me.chayapak1.chomens_bot.voiceChat.Packet;
import me.chayapak1.chomens_bot.voiceChat.packets.AuthenticateAckPacket;
import me.chayapak1.chomens_bot.voiceChat.packets.AuthenticatePacket;
import me.chayapak1.chomens_bot.voiceChat.packets.ConnectionAckPacket;
import me.chayapak1.chomens_bot.voiceChat.packets.ConnectionCheckPacket;
import me.chayapak1.chomens_bot.voiceChat.packets.KeepAlivePacket;
import me.chayapak1.chomens_bot.voiceChat.packets.MicPacket;
import me.chayapak1.chomens_bot.voiceChat.packets.PingPacket;

public class NetworkMessage {
    public static final byte MAGIC_BYTE = -1;
    public final long timestamp;
    public Packet<? extends Packet<?>> packet;
    public SocketAddress address;
    private static final Map<Byte, Class<? extends Packet<?>>> packetRegistry = new HashMap();

    public NetworkMessage(Packet<?> packet) {
        this(System.currentTimeMillis());
        this.packet = packet;
    }

    private NetworkMessage(long timestamp) {
        this.timestamp = timestamp;
    }

    public static NetworkMessage readPacket(RawUdpPacket packet, InitializationData initializationData) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        if (packet == null) {
            return null;
        }
        byte[] data = packet.data();
        FriendlyByteBuf b = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        if (b.readByte() != -1) {
            return null;
        }
        return NetworkMessage.readFromBytes(packet.socketAddress(), initializationData.secret, b.readByteArray(), System.currentTimeMillis());
    }

    private static NetworkMessage readFromBytes(SocketAddress socketAddress, UUID secret, byte[] encryptedPayload, long timestamp) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        byte[] decrypt;
        try {
            decrypt = AESUtilities.decrypt(secret, encryptedPayload);
        }
        catch (Exception e) {
            LoggerUtilities.error(e);
            return null;
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(decrypt));
        byte packetType = buffer.readByte();
        Class<Packet<?>> packetClass = packetRegistry.get(packetType);
        if (packetClass == null) {
            return null;
        }
        Packet<?> p = packetClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        NetworkMessage message = new NetworkMessage(timestamp);
        message.address = socketAddress;
        message.packet = p.fromBytes(buffer);
        return message;
    }

    private static byte getPacketType(Packet<? extends Packet<?>> packet) {
        for (Map.Entry<Byte, Class<Packet<?>>> entry : packetRegistry.entrySet()) {
            if (!packet.getClass().equals(entry.getValue())) continue;
            return entry.getKey();
        }
        return -1;
    }

    public byte[] writeClient(InitializationData data) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] payload = this.write(data.secret);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(17 + payload.length));
        buffer.writeByte(-1);
        buffer.writeUUID(data.playerUUID);
        buffer.writeByteArray(payload);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    public byte[] write(UUID secret) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        byte type = NetworkMessage.getPacketType(this.packet);
        if (type < 0) {
            throw new IllegalArgumentException("Packet type not found");
        }
        buffer.writeByte(type);
        this.packet.toBytes(buffer);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return AESUtilities.encrypt(secret, bytes);
    }

    static {
        packetRegistry.put((byte)1, MicPacket.class);
        packetRegistry.put((byte)5, AuthenticatePacket.class);
        packetRegistry.put((byte)6, AuthenticateAckPacket.class);
        packetRegistry.put((byte)7, PingPacket.class);
        packetRegistry.put((byte)8, KeepAlivePacket.class);
        packetRegistry.put((byte)9, ConnectionCheckPacket.class);
        packetRegistry.put((byte)10, ConnectionAckPacket.class);
    }
}

