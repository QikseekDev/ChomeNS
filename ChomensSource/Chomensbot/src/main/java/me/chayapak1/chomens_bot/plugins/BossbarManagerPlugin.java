/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.bossbar.BossBar;
import me.chayapak1.chomens_bot.data.bossbar.BotBossBar;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.SNBTUtilities;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundBossEventPacket;

public class BossbarManagerPlugin
implements Listener {
    private final Bot bot;
    public final Map<UUID, BossBar> serverBossBars = new ConcurrentHashMap<UUID, BossBar>();
    private final Map<UUID, BotBossBar> bossBars = new ConcurrentHashMap<UUID, BotBossBar>();
    public boolean enabled = true;
    public boolean actionBar = false;
    public final String bossBarPrefix;

    public BossbarManagerPlugin(Bot bot) {
        this.bot = bot;
        this.bossBarPrefix = bot.config.namespace + ":";
        bot.listener.addListener(this);
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundBossEventPacket) {
            ClientboundBossEventPacket t_packet = (ClientboundBossEventPacket)packet;
            this.packetReceived(t_packet);
        }
    }

    private void packetReceived(ClientboundBossEventPacket packet) {
        if (!this.enabled || this.actionBar || !this.bot.options.useCore) {
            return;
        }
        try {
            switch (packet.getAction()) {
                case ADD: {
                    this.serverBossBars.put(packet.getUuid(), new BossBar(packet.getUuid(), packet.getTitle(), packet.getColor(), packet.getDivision(), packet.getHealth()));
                    HashMap<UUID, BotBossBar> mapCopy = new HashMap<UUID, BotBossBar>(this.bossBars);
                    for (Map.Entry _bossBar : mapCopy.entrySet()) {
                        BotBossBar bossBar = (BotBossBar)_bossBar.getValue();
                        if (!bossBar.secret.equals(packet.getTitle())) continue;
                        this.bossBars.remove(_bossBar.getKey());
                        BotBossBar newBossBar = new BotBossBar(bossBar.title(), bossBar.players(), bossBar.color, bossBar.division, bossBar.visible(), bossBar.max(), bossBar.value(), this.bot);
                        newBossBar.gotSecret = true;
                        this.bossBars.put(packet.getUuid(), newBossBar);
                        this.bossBars.get((Object)packet.getUuid()).id = bossBar.id;
                        this.bossBars.get((Object)packet.getUuid()).onlyName = bossBar.onlyName;
                        this.bossBars.get((Object)packet.getUuid()).uuid = packet.getUuid();
                        newBossBar.setTitle(bossBar.title);
                    }
                    break;
                }
                case REMOVE: {
                    this.serverBossBars.remove(packet.getUuid());
                    break;
                }
                case UPDATE_STYLE: {
                    BossBar bossBar = this.serverBossBars.get(packet.getUuid());
                    if (bossBar == null) {
                        return;
                    }
                    bossBar.color = packet.getColor();
                    bossBar.division = packet.getDivision();
                    break;
                }
                case UPDATE_TITLE: {
                    BossBar bossBar = this.serverBossBars.get(packet.getUuid());
                    if (bossBar == null) {
                        return;
                    }
                    BotBossBar botBossBar = this.get(bossBar.uuid);
                    if (botBossBar != null && botBossBar.secret.equals(packet.getTitle())) {
                        botBossBar.uuid = packet.getUuid();
                        botBossBar.gotSecret = true;
                    }
                    bossBar.title = packet.getTitle();
                    break;
                }
                case UPDATE_HEALTH: {
                    BossBar bossBar = this.serverBossBars.get(packet.getUuid());
                    if (bossBar == null) {
                        return;
                    }
                    bossBar.health = packet.getHealth();
                }
            }
        }
        catch (Exception e) {
            this.bot.logger.error(e);
        }
    }

    @Override
    public void onSecondTick() {
        for (Map.Entry<UUID, BotBossBar> _bossBar : this.bossBars.entrySet()) {
            UUID uuid = _bossBar.getKey();
            BotBossBar bossBar = _bossBar.getValue();
            BossBar serverBossBar = this.serverBossBars.get(uuid);
            if (serverBossBar == null) {
                bossBar.gotSecret = false;
                this.addBossBar(bossBar.id, bossBar);
                continue;
            }
            if (!serverBossBar.title.equals(bossBar.title)) {
                bossBar.setTitle(bossBar.title, true);
                continue;
            }
            if ((float)bossBar.value() != serverBossBar.health * (float)bossBar.max()) {
                bossBar.setValue(bossBar.value(), true);
                bossBar.setMax(bossBar.max(), true);
                continue;
            }
            if (bossBar.color != serverBossBar.color) {
                bossBar.setColor(bossBar.color, true);
                continue;
            }
            if (bossBar.division == serverBossBar.division) continue;
            bossBar.setDivision(bossBar.division, true);
        }
    }

    @Override
    public void onCoreReady() {
        for (Map.Entry<UUID, BotBossBar> _bossBar : this.bossBars.entrySet()) {
            BotBossBar bossBar = _bossBar.getValue();
            this.addBossBar(bossBar.id, bossBar);
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.serverBossBars.clear();
    }

    @Override
    public void onPlayerJoined(PlayerEntry target) {
        this.refreshPlayers();
    }

    public void refreshPlayers() {
        if (!this.enabled || this.actionBar || !this.bot.options.useCore) {
            return;
        }
        for (Map.Entry<UUID, BotBossBar> _bossBar : this.bossBars.entrySet()) {
            BotBossBar bossBar = _bossBar.getValue();
            bossBar.setPlayers(bossBar.players(), true);
        }
    }

    public void add(String name, BotBossBar bossBar) {
        if (!this.enabled || !this.bot.options.useCore) {
            return;
        }
        bossBar.onlyName = name;
        bossBar.id = this.bossBarPrefix + name;
        this.bossBars.put(bossBar.uuid, bossBar);
        this.addBossBar(bossBar.id, bossBar);
    }

    private void addBossBar(String name, BotBossBar bossBar) {
        if (!this.enabled || this.actionBar) {
            return;
        }
        Component title = bossBar.secret;
        String stringTitle = SNBTUtilities.fromComponent(this.bot.options.useSNBTComponents, title);
        this.bot.core.run("minecraft:bossbar add " + name + " " + stringTitle);
        bossBar.setTitle(title, true);
        bossBar.setPlayers(bossBar.players(), true);
        bossBar.setVisible(bossBar.visible(), true);
        bossBar.setColor(bossBar.color, true);
        bossBar.setDivision(bossBar.division(), true);
        bossBar.setMax(bossBar.max(), true);
        bossBar.setValue(bossBar.value(), true);
    }

    public void remove(String name) {
        if (!this.enabled || this.actionBar || !this.bot.options.useCore) {
            return;
        }
        HashMap<UUID, BotBossBar> mapCopy = new HashMap<UUID, BotBossBar>(this.bossBars);
        for (Map.Entry bossBar : mapCopy.entrySet()) {
            if (!((BotBossBar)bossBar.getValue()).id.equals(this.bossBarPrefix + name)) continue;
            this.bossBars.remove(((BotBossBar)bossBar.getValue()).uuid);
        }
        this.bot.core.run("minecraft:bossbar remove " + this.bossBarPrefix + name);
    }

    public BotBossBar get(String name) {
        if (!this.enabled) {
            return null;
        }
        for (Map.Entry<UUID, BotBossBar> _bossBar : this.bossBars.entrySet()) {
            BotBossBar bossBar = _bossBar.getValue();
            if (bossBar.id == null || !bossBar.id.equals(this.bossBarPrefix + name)) continue;
            return this.bossBars.get(bossBar.uuid);
        }
        return null;
    }

    public BotBossBar get(UUID uuid) {
        if (!this.enabled) {
            return null;
        }
        for (Map.Entry<UUID, BotBossBar> bossBar : this.bossBars.entrySet()) {
            if (bossBar.getValue().uuid != uuid) continue;
            return bossBar.getValue();
        }
        return null;
    }
}

