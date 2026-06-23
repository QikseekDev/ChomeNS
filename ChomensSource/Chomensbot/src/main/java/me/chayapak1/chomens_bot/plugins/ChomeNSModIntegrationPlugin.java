/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.chomeNSMod.Encryptor;
import me.chayapak1.chomens_bot.chomeNSMod.Packet;
import me.chayapak1.chomens_bot.chomeNSMod.PacketHandler;
import me.chayapak1.chomens_bot.chomeNSMod.Types;
import me.chayapak1.chomens_bot.chomeNSMod.clientboundPackets.ClientboundHandshakePacket;
import me.chayapak1.chomens_bot.chomeNSMod.clientboundPackets.ClientboundMessagePacket;
import me.chayapak1.chomens_bot.chomeNSMod.serverboundPackets.ServerboundRunCommandPacket;
import me.chayapak1.chomens_bot.chomeNSMod.serverboundPackets.ServerboundRunCoreCommandPacket;
import me.chayapak1.chomens_bot.chomeNSMod.serverboundPackets.ServerboundSuccessfulHandshakePacket;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.chomeNSMod.PayloadMetadata;
import me.chayapak1.chomens_bot.data.chomeNSMod.PayloadState;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.logging.LogType;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.Ascii85;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;

public class ChomeNSModIntegrationPlugin
implements Listener {
    private static final String ID = "chomens_mod";
    private static final int EXTRAS_MESSAGING_CHUNK_SIZE = 32600;
    private static final int ACTION_BAR_CHUNK_SIZE = 15500;
    private static final long NONCE_EXPIRATION_MS = 30000L;
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final List<Class<? extends Packet>> SERVERBOUND_PACKETS = ObjectList.of(ServerboundSuccessfulHandshakePacket.class, ServerboundRunCoreCommandPacket.class, ServerboundRunCommandPacket.class);
    private final Bot bot;
    private final PacketHandler handler;
    public final List<PlayerEntry> connectedPlayers = Collections.synchronizedList(new ObjectArrayList());
    private final Map<PlayerEntry, Map<Integer, ByteBuf>> receivedParts = new ConcurrentHashMap<PlayerEntry, Map<Integer, ByteBuf>>();
    private final List<PayloadMetadata> seenMetadata = Collections.synchronizedList(new ObjectArrayList());

    public ChomeNSModIntegrationPlugin(Bot bot) {
        this.bot = bot;
        this.handler = new PacketHandler(bot);
        bot.extrasMessenger.registerChannel(ID);
        bot.listener.addListener(this);
    }

    @Override
    public void onSecondTick() {
        this.tryHandshaking();
        this.seenMetadata.removeIf(metadata -> System.currentTimeMillis() - metadata.timestamp() > 30000L);
    }

    public void send(PlayerEntry target, Packet packet) {
        if (!this.connectedPlayers.contains(target) && !(packet instanceof ClientboundHandshakePacket)) {
            return;
        }
        ByteBuf buf = Unpooled.buffer();
        PayloadMetadata metadata = this.generateMetadata();
        metadata.serialize(buf);
        buf.writeInt(packet.getId());
        packet.serialize(buf);
        byte[] rawBytes = new byte[buf.readableBytes()];
        buf.readBytes(rawBytes);
        boolean shouldUseExtrasMessenger = this.bot.extrasMessenger.isSupported;
        try {
            int i;
            int messageId = RANDOM.nextInt();
            ArrayList<byte[]> chunks = new ArrayList<byte[]>();
            int chunkSizeToUse = shouldUseExtrasMessenger ? 32600 : 15500;
            for (i = 0; i < rawBytes.length; i += chunkSizeToUse) {
                int end = Math.min(rawBytes.length, i + chunkSizeToUse);
                byte[] chunk = Arrays.copyOfRange(rawBytes, i, end);
                chunks.add(chunk);
            }
            if (chunks.size() > 512) {
                this.bot.logger.error(Component.translatable("Chunk is too large (%s) while trying to send packet %s to %s!", Component.text(chunks.size()), Component.text(packet.toString()), Component.text(target.profile.getIdAsString())));
                return;
            }
            i = 1;
            for (byte[] chunk : chunks) {
                PayloadState state = i == chunks.size() ? PayloadState.DONE : PayloadState.JOINING;
                ByteBuf toSendBuf = Unpooled.buffer();
                toSendBuf.writeInt(messageId);
                toSendBuf.writeShort(state.ordinal());
                toSendBuf.writeBytes(chunk);
                byte[] toSendBytes = new byte[toSendBuf.readableBytes()];
                toSendBuf.readBytes(toSendBytes);
                byte[] encrypted = Encryptor.encrypt(toSendBytes, this.bot.config.chomeNSMod.password);
                if (shouldUseExtrasMessenger) {
                    this.bot.extrasMessenger.sendPayload(ID, encrypted);
                } else {
                    String ascii85EncryptedPayload = Ascii85.encode(encrypted);
                    TranslatableComponent component = Component.translatable("", Component.text(ID), Component.text(ascii85EncryptedPayload));
                    this.bot.chat.actionBar((Component)component, target.profile.getId());
                }
                ++i;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private PayloadMetadata generateMetadata() {
        byte[] nonce = new byte[8];
        RANDOM.nextBytes(nonce);
        long timestamp = System.currentTimeMillis();
        return new PayloadMetadata(nonce, timestamp);
    }

    private boolean isValidPayload(PayloadMetadata metadata) {
        boolean valid;
        if (System.currentTimeMillis() - metadata.timestamp() > 30000L) {
            return false;
        }
        boolean bl = valid = !this.seenMetadata.contains(metadata);
        if (valid) {
            this.seenMetadata.add(metadata);
        }
        return valid;
    }

    private Packet deserialize(byte[] data) {
        ByteBuf buf = Unpooled.wrappedBuffer(data);
        PayloadMetadata metadata = PayloadMetadata.deserialize(buf);
        if (!this.isValidPayload(metadata)) {
            this.bot.logger.log(LogType.INFO, Component.translatable(I18nUtilities.get("chomens_mod.replay_attack"), Component.text(metadata.toString())));
            return null;
        }
        int id = buf.readInt();
        Class<? extends Packet> packetClass = SERVERBOUND_PACKETS.get(id);
        if (packetClass == null) {
            return null;
        }
        try {
            return packetClass.getDeclaredConstructor(ByteBuf.class).newInstance(buf);
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public boolean onSystemMessageReceived(Component component, ChatPacketType packetType, String string, String ansi) {
        TextComponent payloadTextComponent;
        block7: {
            block6: {
                Component component2;
                TranslatableComponent translatableComponent;
                if (packetType != ChatPacketType.SYSTEM || !(component instanceof TranslatableComponent) || !(translatableComponent = (TranslatableComponent)component).key().isEmpty()) {
                    return true;
                }
                List<TranslationArgument> arguments = translatableComponent.arguments();
                if (arguments.size() != 2 || !((component2 = arguments.get(0).asComponent()) instanceof TextComponent)) break block6;
                TextComponent idTextComponent = (TextComponent)component2;
                component2 = arguments.get(1).asComponent();
                if (!(component2 instanceof TextComponent)) break block6;
                payloadTextComponent = (TextComponent)component2;
                if (idTextComponent.content().equals(ID)) break block7;
            }
            return true;
        }
        try {
            byte[] decrypted = Encryptor.decrypt(Ascii85.decode(payloadTextComponent.content()), this.bot.config.chomeNSMod.password);
            this.handleData(decrypted, false, null);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    @Override
    public void onExtrasMessageReceived(UUID sender, byte[] message) {
        try {
            byte[] decrypted = Encryptor.decrypt(message, this.bot.config.chomeNSMod.password);
            this.handleData(decrypted, true, sender);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void handleData(byte[] data, boolean isFromExtrasMessenger, UUID extrasSenderUUID) {
        ByteBuf chunkBuf = Unpooled.wrappedBuffer(data);
        UUID uuid = isFromExtrasMessenger ? extrasSenderUUID : Types.readUUID(chunkBuf);
        PlayerEntry player = this.bot.players.getEntry(uuid);
        if (player == null) {
            return;
        }
        int messageId = chunkBuf.readInt();
        short payloadStateIndex = chunkBuf.readShort();
        PayloadState payloadState = PayloadState.values()[payloadStateIndex];
        this.receivedParts.putIfAbsent(player, new ConcurrentHashMap());
        Map<Integer, ByteBuf> playerReceivedParts = this.receivedParts.get(player);
        if (!playerReceivedParts.containsKey(messageId)) {
            playerReceivedParts.put(messageId, Unpooled.buffer());
        }
        ByteBuf buf = playerReceivedParts.get(messageId);
        buf.writeBytes(chunkBuf);
        playerReceivedParts.put(messageId, buf);
        if (payloadState == PayloadState.DONE) {
            playerReceivedParts.remove(messageId);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            Packet packet = this.deserialize(bytes);
            if (packet == null || !(packet instanceof ServerboundSuccessfulHandshakePacket) && !this.connectedPlayers.contains(player)) {
                return;
            }
            this.handlePacket(player, packet);
        }
    }

    private void tryHandshaking() {
        for (String username : this.bot.config.chomeNSMod.players) {
            PlayerEntry target = this.bot.players.getEntry(username);
            if (target == null || this.connectedPlayers.contains(target)) continue;
            this.send(target, new ClientboundHandshakePacket());
        }
    }

    private void handlePacket(PlayerEntry player, Packet packet) {
        if (packet instanceof ServerboundSuccessfulHandshakePacket) {
            this.connectedPlayers.removeIf(eachPlayer -> eachPlayer.equals(player));
            this.connectedPlayers.add(player);
        }
        this.handler.handlePacket(player, packet);
        this.bot.listener.dispatch(listener -> listener.onChomeNSModPacketReceived(player, packet));
    }

    public void sendMessage(PlayerEntry target, Component message) {
        this.send(target, new ClientboundMessagePacket(message));
    }

    @Override
    public void onPlayerLeft(PlayerEntry target) {
        this.connectedPlayers.removeIf(player -> player.equals(target));
        this.receivedParts.remove(target);
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.connectedPlayers.clear();
        this.receivedParts.clear();
        this.seenMetadata.clear();
    }
}

