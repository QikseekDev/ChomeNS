/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot;

import it.unimi.dsi.fastutil.objects.ObjectList;
import java.lang.runtime.SwitchBootstraps;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import me.chayapak1.chomens_bot.Configuration;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.data.color.ColorPalette;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.plugins.AuthPlugin;
import me.chayapak1.chomens_bot.plugins.BossbarManagerPlugin;
import me.chayapak1.chomens_bot.plugins.BotSelectorBroadcasterPlugin;
import me.chayapak1.chomens_bot.plugins.BruhifyPlugin;
import me.chayapak1.chomens_bot.plugins.ChatCommandHandlerPlugin;
import me.chayapak1.chomens_bot.plugins.ChatPlugin;
import me.chayapak1.chomens_bot.plugins.ChomeNSModIntegrationPlugin;
import me.chayapak1.chomens_bot.plugins.ClearChatNameAnnouncerPlugin;
import me.chayapak1.chomens_bot.plugins.CloopPlugin;
import me.chayapak1.chomens_bot.plugins.CommandHandlerPlugin;
import me.chayapak1.chomens_bot.plugins.CommandSpyPlugin;
import me.chayapak1.chomens_bot.plugins.CommandSuggestionPlugin;
import me.chayapak1.chomens_bot.plugins.CorePlugin;
import me.chayapak1.chomens_bot.plugins.EvalPlugin;
import me.chayapak1.chomens_bot.plugins.ExtrasMessengerPlugin;
import me.chayapak1.chomens_bot.plugins.FilterManagerPlugin;
import me.chayapak1.chomens_bot.plugins.GrepLogPlugin;
import me.chayapak1.chomens_bot.plugins.IPFilterPlugin;
import me.chayapak1.chomens_bot.plugins.ListenerManagerPlugin;
import me.chayapak1.chomens_bot.plugins.LoggerPlugin;
import me.chayapak1.chomens_bot.plugins.MailPlugin;
import me.chayapak1.chomens_bot.plugins.MusicPlayerPlugin;
import me.chayapak1.chomens_bot.plugins.PacketSnifferPlugin;
import me.chayapak1.chomens_bot.plugins.PlayerFilterPlugin;
import me.chayapak1.chomens_bot.plugins.PlayersDatabasePlugin;
import me.chayapak1.chomens_bot.plugins.PlayersPlugin;
import me.chayapak1.chomens_bot.plugins.PositionPlugin;
import me.chayapak1.chomens_bot.plugins.QueryPlugin;
import me.chayapak1.chomens_bot.plugins.RainbowArmorPlugin;
import me.chayapak1.chomens_bot.plugins.ScreensharePlugin;
import me.chayapak1.chomens_bot.plugins.SelfCarePlugin;
import me.chayapak1.chomens_bot.plugins.ServerFeaturesPlugin;
import me.chayapak1.chomens_bot.plugins.TPSPlugin;
import me.chayapak1.chomens_bot.plugins.TabCompletePlugin;
import me.chayapak1.chomens_bot.plugins.TeamPlugin;
import me.chayapak1.chomens_bot.plugins.TickPlugin;
import me.chayapak1.chomens_bot.plugins.TrustedPlugin;
import me.chayapak1.chomens_bot.plugins.VoiceChatPlugin;
import me.chayapak1.chomens_bot.plugins.WhitelistPlugin;
import me.chayapak1.chomens_bot.plugins.WorldPlugin;
import me.chayapak1.chomens_bot.util.ComponentUtilities;
import me.chayapak1.chomens_bot.util.MathUtilities;
import me.chayapak1.chomens_bot.util.RandomStringUtilities;
import me.chayapak1.chomens_bot.util.UUIDUtilities;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.BuiltinFlags;
import org.geysermc.mcprotocollib.network.ClientSession;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.ConnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.DisconnectingEvent;
import org.geysermc.mcprotocollib.network.event.session.PacketErrorEvent;
import org.geysermc.mcprotocollib.network.event.session.PacketSendingEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.factory.ClientNetworkSessionFactory;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.session.ClientNetworkSession;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.HandPreference;
import org.geysermc.mcprotocollib.protocol.data.game.setting.ChatVisibility;
import org.geysermc.mcprotocollib.protocol.data.game.setting.ParticleStatus;
import org.geysermc.mcprotocollib.protocol.data.game.setting.SkinPart;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundStoreCookiePacket;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundTransferPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundClientInformationPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.cookie.clientbound.ClientboundCookieRequestPacket;
import org.geysermc.mcprotocollib.protocol.packet.cookie.serverbound.ServerboundCookieResponsePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundPlayerLoadedPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundCustomQueryPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundLoginCompressionPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundLoginFinishedPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.serverbound.ServerboundCustomQueryAnswerPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.serverbound.ServerboundLoginAcknowledgedPacket;

public class Bot
extends SessionAdapter {
    private static final List<String> NEEDS_DELAY_DISCONNECT_REASON = ObjectList.of("Wait 5 seconds before connecting, thanks! :)", "You are logging in too fast, try again later.", "Connection throttled! Please wait before reconnecting.");
    public final String host;
    public final int port;
    public final Configuration.BotOption options;
    public final Configuration config;
    public final ColorPalette colorPalette;
    public final List<Bot> bots;
    public String username;
    public GameProfile profile;
    public ClientSession session;
    private final Map<Key, byte[]> cookies = new HashMap<Key, byte[]>();
    private boolean isTransferring = false;
    public boolean printDisconnectedCause = false;
    public int connectAttempts = 0;
    public boolean loggedIn = false;
    public long loginTime = System.currentTimeMillis();
    public final ExecutorService executorService = Main.EXECUTOR_SERVICE;
    public final ScheduledExecutorService executor = Main.EXECUTOR;
    public final ListenerManagerPlugin listener;
    public final LoggerPlugin logger;
    public final TickPlugin tick;
    public final ChatPlugin chat;
    public final CommandSpyPlugin commandSpy;
    public final PositionPlugin position;
    public final ServerFeaturesPlugin serverFeatures;
    public final SelfCarePlugin selfCare;
    public final QueryPlugin query;
    public final ExtrasMessengerPlugin extrasMessenger;
    public final WorldPlugin world;
    public final CorePlugin core;
    public final TeamPlugin team;
    public final PlayersPlugin players;
    public final TabCompletePlugin tabComplete;
    public final CommandHandlerPlugin commandHandler;
    public final ChatCommandHandlerPlugin chatCommandHandler;
    public final BossbarManagerPlugin bossbar;
    public final MusicPlayerPlugin music;
    public final TPSPlugin tps;
    public final EvalPlugin eval;
    public final TrustedPlugin trusted;
    public final GrepLogPlugin grepLog;
    public final BruhifyPlugin bruhify;
    public final CloopPlugin cloop;
    public final FilterManagerPlugin filterManager;
    public final PlayerFilterPlugin playerFilter;
    public final CommandSuggestionPlugin commandSuggestion;
    public final MailPlugin mail;
    public final PacketSnifferPlugin packetSniffer;
    public final VoiceChatPlugin voiceChat;
    public final BotSelectorBroadcasterPlugin selectorBroadcaster;
    public final ChomeNSModIntegrationPlugin chomeNSMod;
    public final AuthPlugin auth;
    public final ClearChatNameAnnouncerPlugin clearChatNameAnnouncer;
    public ScreensharePlugin screenshare;
    public final WhitelistPlugin whitelist;
    public final PlayersDatabasePlugin playersDatabase;
    public final IPFilterPlugin ipFilter;
    public final RainbowArmorPlugin rainbowArmor;

    public Bot(Configuration.BotOption botOption, List<Bot> bots, Configuration config) {
        this.host = botOption.host;
        this.port = botOption.port;
        this.options = botOption;
        this.bots = bots;
        this.config = config;
        this.colorPalette = new ColorPalette(config.colorPalette);
        this.listener = new ListenerManagerPlugin(this);
        this.tick = new TickPlugin(this);
        this.chat = new ChatPlugin(this);
        this.commandSpy = new CommandSpyPlugin(this);
        this.query = new QueryPlugin(this);
        this.extrasMessenger = new ExtrasMessengerPlugin(this);
        this.chomeNSMod = new ChomeNSModIntegrationPlugin(this);
        this.commandSuggestion = new CommandSuggestionPlugin(this);
        this.logger = new LoggerPlugin(this);
        this.position = new PositionPlugin(this);
        this.serverFeatures = new ServerFeaturesPlugin(this);
        this.selfCare = new SelfCarePlugin(this);
        this.world = new WorldPlugin(this);
        this.core = new CorePlugin(this);
        this.team = new TeamPlugin(this);
        this.playersDatabase = new PlayersDatabasePlugin(this);
        this.players = new PlayersPlugin(this);
        this.tabComplete = new TabCompletePlugin(this);
        this.commandHandler = new CommandHandlerPlugin(this);
        this.chatCommandHandler = new ChatCommandHandlerPlugin(this);
        this.bossbar = new BossbarManagerPlugin(this);
        this.music = new MusicPlayerPlugin(this);
        this.tps = new TPSPlugin(this);
        this.eval = new EvalPlugin(this);
        this.trusted = new TrustedPlugin(this);
        this.grepLog = new GrepLogPlugin(this);
        this.bruhify = new BruhifyPlugin(this);
        this.cloop = new CloopPlugin(this);
        this.filterManager = new FilterManagerPlugin(this);
        this.playerFilter = new PlayerFilterPlugin(this);
        this.mail = new MailPlugin(this);
        this.packetSniffer = new PacketSnifferPlugin(this);
        this.voiceChat = new VoiceChatPlugin(this);
        this.selectorBroadcaster = new BotSelectorBroadcasterPlugin(this);
        this.auth = new AuthPlugin(this);
        this.screenshare = new ScreensharePlugin(this);
        this.clearChatNameAnnouncer = new ClearChatNameAnnouncerPlugin(this);
        this.whitelist = new WhitelistPlugin(this);
        this.ipFilter = new IPFilterPlugin(this);
        this.rainbowArmor = new RainbowArmorPlugin(this);
    }

    protected void connect() {
        this.reconnect();
    }

    private void reconnect() {
        ++this.connectAttempts;
        this.listener.dispatch(Listener::onConnecting);
        if (!this.isTransferring) {
            this.username = this.options.username == null ? RandomStringUtilities.generate(MathUtilities.between(3, 16), RandomStringUtilities.ALPHANUMERIC) : this.options.username;
        }
        ClientNetworkSessionFactory factory2 = ClientNetworkSessionFactory.factory().setAddress(this.host, this.port).setProtocol(new MinecraftProtocol(new GameProfile(UUIDUtilities.getOfflineUUID(this.username), this.username), null));
        if (this.session != null) {
            factory2.setPacketHandlerExecutor(this.session.getPacketHandlerExecutor());
        }
        ClientNetworkSession session = factory2.create();
        this.session = session;
        session.setFlag(BuiltinFlags.CLIENT_TRANSFERRING, this.isTransferring);
        session.setFlag(MinecraftConstants.FOLLOW_TRANSFERS, false);
        session.setFlag(BuiltinFlags.ATTEMPT_SRV_RESOLVE, this.options.resolveSRV);
        session.addListener(this);
        session.connect(false);
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        Packet packet2 = packet;
        Objects.requireNonNull(packet2);
        Packet packet3 = packet2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClientboundLoginPacket.class, ClientboundLoginFinishedPacket.class, ClientboundCustomQueryPacket.class, ClientboundCookieRequestPacket.class, ClientboundTransferPacket.class, ClientboundStoreCookiePacket.class, ClientboundLoginCompressionPacket.class, ClientboundCustomPayloadPacket.class}, (Object)packet3, n)) {
            case 0: {
                ClientboundLoginPacket t_packet = (ClientboundLoginPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 1: {
                ClientboundLoginFinishedPacket t_packet = (ClientboundLoginFinishedPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 2: {
                ClientboundCustomQueryPacket t_packet = (ClientboundCustomQueryPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 3: {
                ClientboundCookieRequestPacket t_packet = (ClientboundCookieRequestPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 4: {
                ClientboundTransferPacket t_packet = (ClientboundTransferPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 5: {
                ClientboundStoreCookiePacket t_packet = (ClientboundStoreCookiePacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 6: {
                ClientboundLoginCompressionPacket t_packet = (ClientboundLoginCompressionPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
            case 7: {
                ClientboundCustomPayloadPacket t_packet = (ClientboundCustomPayloadPacket)packet3;
                this.packetReceived(t_packet);
                break;
            }
        }
        this.listener.dispatch(listener -> listener.packetReceived(session, packet));
    }

    private void packetReceived(ClientboundLoginFinishedPacket packet) {
        this.profile = packet.getProfile();
        this.session.setFlag(BuiltinFlags.CLIENT_TRANSFERRING, false);
    }

    private void packetReceived(ClientboundLoginPacket ignoredPacket) {
        if (this.loggedIn) {
            this.listener.dispatch(listener -> listener.disconnected(new DisconnectedEvent(this.session, Component.text("Server didn't send ClientboundDisconnectPacket"), null)));
        }
        this.loggedIn = true;
        this.loginTime = System.currentTimeMillis();
        this.connectAttempts = 0;
        this.listener.dispatch(listener -> listener.connected(new ConnectedEvent(this.session)));
        this.session.send(ServerboundPlayerLoadedPacket.INSTANCE);
    }

    private void packetReceived(ClientboundCustomQueryPacket packet) {
        this.session.send(new ServerboundCustomQueryAnswerPacket(packet.getMessageId(), null));
    }

    private void packetReceived(ClientboundCustomPayloadPacket packet) {
        if (!packet.getChannel().asString().equals("minecraft:register")) {
            return;
        }
        this.session.send(new ServerboundCustomPayloadPacket(Key.key("minecraft", "register"), "\u0000".getBytes(StandardCharsets.UTF_8)));
    }

    private void packetReceived(ClientboundCookieRequestPacket packet) {
        this.session.send(new ServerboundCookieResponsePacket(packet.getKey(), this.cookies.get(packet.getKey())));
    }

    private void packetReceived(ClientboundStoreCookiePacket packet) {
        this.cookies.put(packet.getKey(), packet.getPayload());
    }

    private void packetReceived(ClientboundTransferPacket ignoredPacket) {
        this.isTransferring = true;
        this.session.disconnect(Component.translatable("disconnect.transfer"));
    }

    private void packetReceived(ClientboundLoginCompressionPacket packet) {
        if (packet.getThreshold() < 0) {
            this.session.setCompression(null);
        }
    }

    @Override
    public void packetSending(PacketSendingEvent packetSendingEvent) {
        this.listener.dispatch(listener -> listener.packetSending(packetSendingEvent));
    }

    @Override
    public void packetSent(Session session, Packet packet) {
        this.listener.dispatch(listener -> listener.packetSent(session, packet));
        if (packet instanceof ServerboundLoginAcknowledgedPacket) {
            ServerboundLoginAcknowledgedPacket t_packet = (ServerboundLoginAcknowledgedPacket)packet;
            this.packetSent(t_packet);
        }
    }

    private void packetSent(ServerboundLoginAcknowledgedPacket ignoredPacket) {
        this.session.getPacketHandlerExecutor().execute(() -> {
            this.session.send(new ServerboundCustomPayloadPacket(Key.key("minecraft:brand"), "\u0006fabric".getBytes(StandardCharsets.UTF_8)));
            ArrayList<SkinPart> skinParts = new ArrayList<SkinPart>(Arrays.asList(SkinPart.VALUES));
            this.session.send(new ServerboundClientInformationPacket(ComponentUtilities.LANGUAGE.getOrDefault("language.code", "en_us"), 16, ChatVisibility.FULL, true, skinParts, HandPreference.RIGHT_HAND, false, false, ParticleStatus.ALL));
        });
    }

    @Override
    public void packetError(PacketErrorEvent packetErrorEvent) {
        this.listener.dispatch(listener -> listener.packetError(packetErrorEvent));
        packetErrorEvent.setSuppress(true);
    }

    @Override
    public void disconnecting(DisconnectingEvent disconnectingEvent) {
        this.listener.dispatch(listener -> listener.disconnecting(disconnectingEvent));
    }

    @Override
    public void disconnected(DisconnectedEvent disconnectedEvent) {
        this.loggedIn = false;
        Throwable cause = disconnectedEvent.getCause();
        if (this.printDisconnectedCause && cause != null) {
            this.logger.error(cause);
        }
        if (Main.stopping) {
            return;
        }
        if (!this.isTransferring) {
            this.cookies.clear();
        }
        if (this.isTransferring) {
            this.reconnect();
            this.isTransferring = false;
        } else {
            String stringMessage = ComponentUtilities.stringify(disconnectedEvent.getReason());
            long reconnectDelay = NEEDS_DELAY_DISCONNECT_REASON.contains(stringMessage) ? TimeUnit.SECONDS.toMillis(5L) : (long)this.options.reconnectDelay;
            this.executor.schedule(this::reconnect, reconnectDelay, TimeUnit.MILLISECONDS);
        }
        this.listener.dispatch(listener -> listener.disconnected(disconnectedEvent));
    }

    public String getServerString() {
        return this.getServerString(false);
    }

    public String getServerString(boolean bypassHidden) {
        return this.options.hidden && !bypassHidden ? this.options.serverName : this.host + ":" + this.port;
    }

    public void stop() {
        this.session.disconnect("Received stop signal");
        Main.bots.remove(this);
    }

    public String toString() {
        return "Bot{host='" + this.host + "', port=" + this.port + ", username='" + this.username + "', loggedIn=" + this.loggedIn + "}";
    }
}

