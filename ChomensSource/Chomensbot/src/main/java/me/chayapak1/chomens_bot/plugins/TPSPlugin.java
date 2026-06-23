/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.text.DecimalFormat;
import java.util.Arrays;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.bossbar.BotBossBar;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.util.MathUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.geysermc.mcprotocollib.network.event.session.ConnectedEvent;
import org.geysermc.mcprotocollib.protocol.data.game.BossBarColor;
import org.geysermc.mcprotocollib.protocol.data.game.BossBarDivision;

public class TPSPlugin
implements Listener {
    private final Bot bot;
    private boolean enabled = false;
    private final double[] tickRates = new double[20];
    private int nextIndex = 0;
    private long timeLastTimeUpdate = -1L;
    private long timeGameJoined;
    private final String bossbarName = "tpsbar";

    public TPSPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    private void createBossBar() {
        BotBossBar bossBar = new BotBossBar(Component.empty(), "@a", this.getBossBarColor(this.getTickRate()), BossBarDivision.NOTCHES_20, true, 20L, 0, this.bot);
        this.bot.bossbar.add("tpsbar", bossBar);
    }

    public void on() {
        if (this.enabled) {
            return;
        }
        this.enabled = true;
        this.createBossBar();
    }

    public void off() {
        this.enabled = false;
        BotBossBar bossBar = this.bot.bossbar.get("tpsbar");
        if (bossBar != null) {
            bossBar.setTitle(Component.text("TPSBar is currently disabled"));
        }
        this.bot.bossbar.remove("tpsbar");
    }

    @Override
    public void onTick() {
        this.updateTPSBar();
    }

    private void updateTPSBar() {
        if (!this.enabled) {
            return;
        }
        try {
            double tickRate = this.getTickRate();
            DecimalFormat formatter = new DecimalFormat("##.##");
            TranslatableComponent component = Component.translatable("%s - %s", (TextColor)NamedTextColor.DARK_GRAY, Component.text("TPS", (TextColor)NamedTextColor.GRAY), Component.text(formatter.format(tickRate), (TextColor)this.getColor(tickRate)));
            BotBossBar bossBar = this.bot.bossbar.get("tpsbar");
            if (bossBar == null) {
                this.createBossBar();
                return;
            }
            bossBar.setTitle(component);
            bossBar.setColor(this.getBossBarColor(tickRate));
            bossBar.setValue((int)Math.round(tickRate));
        }
        catch (Exception e) {
            this.bot.logger.error(e);
        }
    }

    private NamedTextColor getColor(double tickRate) {
        if (tickRate > 15.0) {
            return NamedTextColor.GREEN;
        }
        if (tickRate == 15.0) {
            return NamedTextColor.YELLOW;
        }
        if (tickRate < 15.0 && tickRate > 10.0) {
            return NamedTextColor.RED;
        }
        return NamedTextColor.DARK_RED;
    }

    private BossBarColor getBossBarColor(double tickRate) {
        if (tickRate > 15.0) {
            return BossBarColor.LIME;
        }
        if (tickRate == 15.0) {
            return BossBarColor.YELLOW;
        }
        if (tickRate < 15.0 && tickRate > 10.0) {
            return BossBarColor.RED;
        }
        return BossBarColor.PURPLE;
    }

    @Override
    public void onSecondTick() {
        long now = System.currentTimeMillis();
        float timeElapsed = (float)(now - this.timeLastTimeUpdate) / 1000.0f;
        this.tickRates[this.nextIndex] = MathUtilities.clamp(20.0f / timeElapsed, 0.0f, 20.0f);
        this.nextIndex = (this.nextIndex + 1) % this.tickRates.length;
        this.timeLastTimeUpdate = now;
    }

    @Override
    public void connected(ConnectedEvent event) {
        Arrays.fill(this.tickRates, 0.0);
        this.nextIndex = 0;
        this.timeGameJoined = this.timeLastTimeUpdate = System.currentTimeMillis();
    }

    public double getTickRate() {
        if (System.currentTimeMillis() - this.timeGameJoined < 4000L) {
            return 20.0;
        }
        int numTicks = 0;
        float sumTickRates = 0.0f;
        for (double tickRate : this.tickRates) {
            if (!(tickRate > 0.0)) continue;
            sumTickRates += (float)tickRate;
            ++numTicks;
        }
        return sumTickRates / (float)numTicks;
    }
}

