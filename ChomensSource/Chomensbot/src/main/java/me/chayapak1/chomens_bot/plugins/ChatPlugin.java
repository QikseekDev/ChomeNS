/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.runtime.SwitchBootstraps;
import java.time.Instant;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.chatParsers.KaboomChatParser;
import me.chayapak1.chomens_bot.chatParsers.MinecraftChatParser;
import me.chayapak1.chomens_bot.chatParsers.U203aChatParser;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.chat.ChatParser;
import me.chayapak1.chomens_bot.data.chat.PlayerMessage;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.ComponentUtilities;
import me.chayapak1.chomens_bot.util.IllegalCharactersUtilities;
import me.chayapak1.chomens_bot.util.SNBTUtilities;
import me.chayapak1.chomens_bot.util.StringUtilities;
import me.chayapak1.chomens_bot.util.UUIDUtilities;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.BuildableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.codec.NbtComponentSerializer;
import org.geysermc.mcprotocollib.protocol.data.DefaultComponentSerializer;
import org.geysermc.mcprotocollib.protocol.data.game.RegistryEntry;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundRegistryDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundDisguisedChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.jetbrains.annotations.NotNull;

public class ChatPlugin
implements Listener {
    public static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(&[a-f0-9rlonmk])", 8);
    public static final Pattern COLOR_CODE_END_PATTERN = Pattern.compile("^.*&[a-f0-9rlonmk]$", 8);
    public static final Pattern CHAT_SPLIT_PATTERN = Pattern.compile("\\G\\s*([^\\r\\n]{1,254}(?=\\s|$)|[^\\r\\n]{254})");
    private static final String CHAT_TYPE_REGISTRY_KEY = "minecraft:chat_type";
    private static final ChatTypeComponentRenderer CHAT_TYPE_COMPONENT_RENDERER = new ChatTypeComponentRenderer();
    private final Bot bot;
    private final List<ChatParser> chatParsers = new ObjectArrayList<ChatParser>();
    public final List<Component> chatTypes = new ObjectArrayList<Component>();
    private final Queue<String> queue = new ConcurrentLinkedQueue<String>();
    public final int queueDelay;

    public ChatPlugin(Bot bot) {
        this.bot = bot;
        this.queueDelay = bot.options.chatQueueDelay;
        bot.listener.addListener(this);
        this.chatParsers.add(new MinecraftChatParser(bot));
        this.chatParsers.add(new KaboomChatParser(bot));
        this.chatParsers.add(new U203aChatParser(bot));
        bot.executor.scheduleAtFixedRate(this::sendChatTick, 0L, this.queueDelay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        Packet packet2 = packet;
        Objects.requireNonNull(packet2);
        Packet packet3 = packet2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClientboundSystemChatPacket.class, ClientboundPlayerChatPacket.class, ClientboundDisguisedChatPacket.class, ClientboundRegistryDataPacket.class}, (Object)packet3, n)) {
            case 0: {
                ClientboundSystemChatPacket t_packet = (ClientboundSystemChatPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 1: {
                ClientboundPlayerChatPacket t_packet = (ClientboundPlayerChatPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 2: {
                ClientboundDisguisedChatPacket t_packet = (ClientboundDisguisedChatPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 3: {
                ClientboundRegistryDataPacket t_packet = (ClientboundRegistryDataPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
        }
    }

    private void packetReceived(ClientboundSystemChatPacket packet) {
        ChatParser parser;
        Object key;
        BuildableComponent<TextComponent, TextComponent.Builder> t_component;
        Component component = packet.getContent();
        if (packet.isOverlay() || component instanceof TextComponent && (t_component = (TextComponent)component).content().length() > 20000) {
            return;
        }
        if (component instanceof TranslatableComponent && (((String)(key = (t_component = (TranslatableComponent)component).key())).equals("advMode.setCommand.success") || ((String)key).equals("advMode.notAllowed") || ((String)key).equals("multiplayer.message_not_delivered") || ((String)key).equals("\u0642\u064a\u0627\u062f\u0629 \u0627\u0644\u0645\u062c\u0645\u0648\u0639\u0629: %s"))) {
            return;
        }
        PlayerMessage playerMessage = null;
        key = this.chatParsers.iterator();
        while (key.hasNext() && (playerMessage = (parser = (ChatParser)key.next()).parse(component)) == null) {
        }
        String string = ComponentUtilities.stringify(component);
        if (string.endsWith("\n".repeat(10) + "The chat has been cleared")) {
            return;
        }
        String ansi = ComponentUtilities.stringifyAnsi(component);
        PlayerMessage finalPlayerMessage = playerMessage;
        this.bot.listener.dispatchWithCheck(listener -> {
            if (!listener.onSystemMessageReceived(component, ChatPacketType.SYSTEM, string, ansi)) {
                return false;
            }
            return finalPlayerMessage == null || listener.onPlayerMessageReceived(finalPlayerMessage, ChatPacketType.SYSTEM);
        });
    }

    private void packetReceived(ClientboundRegistryDataPacket packet) {
        if (!packet.getRegistry().key().equals(Key.key(CHAT_TYPE_REGISTRY_KEY))) {
            return;
        }
        this.chatTypes.clear();
        for (RegistryEntry entry : packet.getEntries()) {
            NbtMap chat;
            NbtMap data = entry.getData();
            if (data == null || (chat = data.getCompound("chat")) == null) continue;
            String translation = chat.getString("translation_key");
            List<String> parameters = chat.getList("parameters", NbtType.STRING);
            NbtMap styleMap = chat.getCompound("style", null);
            Component style = Component.empty();
            if (styleMap != null) {
                JsonElement json = NbtComponentSerializer.tagComponentToJson(styleMap);
                if (json.isJsonObject() && json.getAsJsonObject().get("text") == null) {
                    JsonObject object = json.getAsJsonObject();
                    object.addProperty("text", "");
                    json = object;
                }
                style = DefaultComponentSerializer.get().deserializeFromTree(json);
            }
            Component component = Component.translatable(translation, parameters.stream().map(Component::text).toList()).mergeStyle(style);
            this.chatTypes.add(component);
        }
    }

    private Component getComponentByChatType(int chatType, Component target, Component sender, Component content) {
        Component type = this.chatTypes.get(chatType);
        if (type == null) {
            return null;
        }
        return CHAT_TYPE_COMPONENT_RENDERER.render(type, new ChatTypeContext(target, sender, content));
    }

    private void packetReceived(ClientboundPlayerChatPacket packet) {
        Component systemComponent;
        String ansi;
        String string;
        UUID senderUUID = packet.getSender();
        PlayerEntry entry = this.bot.players.getEntry(senderUUID);
        if (entry == null) {
            return;
        }
        PlayerMessage playerMessage = new PlayerMessage(entry, packet.getName(), Component.text(packet.getContent()));
        Component unsignedContent = packet.getUnsignedContent();
        Component chatTypeComponent = this.getComponentByChatType(packet.getChatType().id(), packet.getTargetName(), packet.getName(), playerMessage.contents());
        if (chatTypeComponent != null && unsignedContent == null) {
            string = ComponentUtilities.stringify(chatTypeComponent);
            ansi = ComponentUtilities.stringifyAnsi(chatTypeComponent);
            systemComponent = chatTypeComponent;
        } else {
            string = ComponentUtilities.stringify(unsignedContent);
            ansi = ComponentUtilities.stringifyAnsi(unsignedContent);
            systemComponent = unsignedContent;
        }
        this.bot.listener.dispatchWithCheck(listener -> {
            if (!listener.onPlayerMessageReceived(playerMessage, ChatPacketType.PLAYER)) {
                return false;
            }
            return listener.onSystemMessageReceived(systemComponent, ChatPacketType.PLAYER, string, ansi);
        });
    }

    private void packetReceived(ClientboundDisguisedChatPacket packet) {
        ChatParser parser;
        Component component = packet.getMessage();
        PlayerMessage parsedFromMessage = null;
        Iterator<ChatParser> iterator2 = this.chatParsers.iterator();
        while (iterator2.hasNext() && (parsedFromMessage = (parser = iterator2.next()).parse(component)) == null) {
        }
        Component chatTypeComponent = this.getComponentByChatType(packet.getChatType().id(), packet.getTargetName(), packet.getName(), packet.getMessage());
        if (chatTypeComponent != null && parsedFromMessage == null) {
            String string = ComponentUtilities.stringify(chatTypeComponent);
            String ansi = ComponentUtilities.stringifyAnsi(chatTypeComponent);
            this.bot.listener.dispatchWithCheck(listener -> listener.onSystemMessageReceived(chatTypeComponent, ChatPacketType.DISGUISED, string, ansi));
            for (ChatParser parser2 : this.chatParsers) {
                PlayerMessage parsed = parser2.parse(chatTypeComponent);
                if (parsed == null) continue;
                PlayerMessage playerMessage = new PlayerMessage(parsed.sender(), packet.getName(), parsed.contents());
                this.bot.listener.dispatchWithCheck(listener -> listener.onPlayerMessageReceived(playerMessage, ChatPacketType.DISGUISED));
            }
        } else {
            if (parsedFromMessage == null) {
                return;
            }
            PlayerMessage playerMessage = new PlayerMessage(parsedFromMessage.sender(), packet.getName(), parsedFromMessage.contents());
            String string = ComponentUtilities.stringify(component);
            String ansi = ComponentUtilities.stringifyAnsi(component);
            this.bot.listener.dispatchWithCheck(listener -> {
                if (!listener.onPlayerMessageReceived(playerMessage, ChatPacketType.DISGUISED)) {
                    return false;
                }
                return listener.onSystemMessageReceived(component, ChatPacketType.DISGUISED, string, ansi);
            });
        }
    }

    private void sendChatTick() {
        String message;
        if (this.queue.size() > 100) {
            this.queue.clear();
        }
        if ((message = this.queue.poll()) == null) {
            return;
        }
        if (message.startsWith("/")) {
            String slashRemoved = message.substring(1);
            this.sendCommandInstantly(slashRemoved);
        } else {
            this.sendChatInstantly(message);
        }
    }

    public void sendCommandInstantly(String command) {
        if (!this.bot.loggedIn) {
            return;
        }
        String namespaceSanitizedCommand = this.bot.serverFeatures.hasNamespaces ? command : StringUtilities.removeNamespace(command);
        this.bot.session.send(new ServerboundChatCommandPacket(namespaceSanitizedCommand));
    }

    public void sendChatInstantly(String message) {
        if (!this.bot.loggedIn) {
            return;
        }
        this.bot.session.send(new ServerboundChatPacket(StringUtilities.truncateToFitUtf8ByteLength(message, 256), Instant.now().toEpochMilli(), 0L, null, 0, new BitSet(), 0));
    }

    public void clearQueue() {
        this.queue.clear();
    }

    public void send(String message) {
        if (message.startsWith("/")) {
            this.queue.add(message);
            return;
        }
        Matcher colorCodeMatcher = COLOR_CODE_PATTERN.matcher(message);
        ObjectArrayList colorCodePositions = new ObjectArrayList();
        ObjectArrayList colorCodes = new ObjectArrayList();
        while (colorCodeMatcher.find()) {
            colorCodePositions.add(colorCodeMatcher.start());
            colorCodes.add(colorCodeMatcher.group());
        }
        String lastColor = "";
        int colorCodeIndex = 0;
        Matcher splitMatcher = CHAT_SPLIT_PATTERN.matcher(message);
        boolean isFirst = true;
        while (splitMatcher.find()) {
            String eachMessage = splitMatcher.group(1);
            String strippedMessage = IllegalCharactersUtilities.stripIllegalCharacters(eachMessage);
            if (strippedMessage.trim().isEmpty()) continue;
            if (COLOR_CODE_END_PATTERN.matcher(strippedMessage).find()) {
                strippedMessage = strippedMessage.substring(0, strippedMessage.length() - 2);
            }
            if (!isFirst) {
                int currentPos = splitMatcher.start(1);
                while (colorCodeIndex < colorCodePositions.size() && (Integer)colorCodePositions.get(colorCodeIndex) < currentPos) {
                    lastColor = (String)colorCodes.get(colorCodeIndex);
                    ++colorCodeIndex;
                }
            }
            this.queue.add(lastColor + strippedMessage);
            isFirst = false;
        }
    }

    public void tellraw(Component component, String targets) {
        if (this.bot.options.useChat) {
            if (!targets.equals("@a")) {
                return;
            }
            String stringified = ComponentUtilities.stringifyLegacy(component).replace("\u00a7", "&");
            this.send(stringified);
        } else {
            this.bot.core.run("minecraft:tellraw " + targets + " " + SNBTUtilities.fromComponent(this.bot.options.useSNBTComponents, component));
        }
    }

    public void tellraw(Component component, UUID uuid) {
        this.tellraw(component, UUIDUtilities.selector(uuid));
    }

    public void tellraw(Component component) {
        this.tellraw(component, "@a");
    }

    public void actionBar(Component component, String targets) {
        if (this.bot.options.useChat) {
            return;
        }
        this.bot.core.run("minecraft:title " + targets + " actionbar " + SNBTUtilities.fromComponent(this.bot.options.useSNBTComponents, component));
    }

    public void actionBar(Component component, UUID uuid) {
        this.actionBar(component, UUIDUtilities.selector(uuid));
    }

    public void actionBar(Component component) {
        this.actionBar(component, "@a");
    }

    private static class ChatTypeComponentRenderer
    extends TranslatableComponentRenderer<ChatTypeContext> {
        private ChatTypeComponentRenderer() {
        }

        @Override
        @NotNull
        protected Component renderText(@NotNull TextComponent component, @NotNull ChatTypeContext context) {
            return switch (component.content()) {
                case "target" -> context.target();
                case "sender" -> context.sender();
                case "content" -> context.content();
                default -> component;
            };
        }
    }

    private record ChatTypeContext(Component target, Component sender, Component content) {
    }
}

