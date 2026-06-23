/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.bossbar;

import java.util.UUID;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.bossbar.BossBar;
import me.chayapak1.chomens_bot.util.SNBTUtilities;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.game.BossBarColor;
import org.geysermc.mcprotocollib.protocol.data.game.BossBarDivision;

public class BotBossBar
extends BossBar {
    public UUID uuid = UUID.randomUUID();
    public final Component secret = Component.translatable("", Component.text(UUID.randomUUID().toString()));
    public boolean gotSecret = false;
    private final Bot bot;
    public String onlyName;
    public String id;
    private String players;
    private boolean visible;
    private long max;
    private int value;

    public BotBossBar(Component title, String players, BossBarColor color, BossBarDivision division, boolean visible, long max, int value, Bot bot) {
        super(null, title, color, division, value);
        this.players = players;
        this.visible = visible;
        this.max = max;
        this.bot = bot;
    }

    public Component title() {
        return this.title;
    }

    public void setTitle(Component title) {
        this.setTitle(title, false);
    }

    public void setTitle(Component title, boolean force) {
        if (!(!this.title.equals(title) && this.gotSecret || force)) {
            return;
        }
        if (this.bot.bossbar.actionBar) {
            this.bot.chat.actionBar(title, this.players);
            return;
        }
        this.title = title;
        String serialized = SNBTUtilities.fromComponent(this.bot.options.useSNBTComponents, title);
        this.bot.core.run("minecraft:bossbar set " + this.id + " name " + serialized);
        if (!this.bot.core.hasRateLimit()) {
            this.bot.core.run(String.format("minecraft:execute as @e[type=minecraft:text_display,tag=%s_%s] run data modify entity @s text set value %s", this.bot.config.namespace, this.onlyName, serialized));
        }
    }

    public BossBarColor color(BossBarColor color) {
        return color;
    }

    public void setColor(BossBarColor color) {
        this.setColor(color, false);
    }

    public void setColor(BossBarColor color, boolean force) {
        if (!(this.color != color && this.gotSecret || force)) {
            return;
        }
        this.color = color;
        if (this.bot.bossbar.actionBar) {
            return;
        }
        this.bot.core.run("minecraft:bossbar set " + this.id + " color " + (color == BossBarColor.LIME ? "green" : (color == BossBarColor.CYAN ? "blue" : color.name().toLowerCase())));
    }

    public String players() {
        return this.players;
    }

    public void setPlayers(String players) {
        this.setPlayers(players, false);
    }

    public void setPlayers(String players, boolean force) {
        if (!(!this.players.equals(players) && this.gotSecret || force)) {
            return;
        }
        this.players = players;
        if (this.bot.bossbar.actionBar) {
            return;
        }
        this.bot.core.run("minecraft:bossbar set " + this.id + " players " + players);
    }

    public BossBarDivision division() {
        return this.division;
    }

    public void setDivision(BossBarDivision division) {
        this.setDivision(division, false);
    }

    public void setDivision(BossBarDivision _division, boolean force) {
        if (!(this.division != _division && this.gotSecret || force)) {
            return;
        }
        this.division = _division;
        String division = null;
        switch (_division) {
            case NONE: {
                division = "progress";
                break;
            }
            case NOTCHES_20: {
                division = "notched_20";
                break;
            }
            case NOTCHES_6: {
                division = "notched_6";
                break;
            }
            case NOTCHES_12: {
                division = "notched_12";
                break;
            }
            case NOTCHES_10: {
                division = "notched_10";
            }
        }
        if (this.bot.bossbar.actionBar) {
            return;
        }
        this.bot.core.run("minecraft:bossbar set " + this.id + " style " + division);
    }

    public int value() {
        return this.value;
    }

    public void setValue(int value) {
        this.setValue(value, false);
    }

    public void setValue(int value, boolean force) {
        if (!(this.value != value && this.gotSecret || force)) {
            return;
        }
        this.value = value;
        if (this.bot.bossbar.actionBar) {
            return;
        }
        this.bot.core.run("minecraft:bossbar set " + this.id + " value " + value);
    }

    public boolean visible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.setVisible(visible, false);
    }

    public void setVisible(boolean visible, boolean force) {
        if (!(this.visible != visible && this.gotSecret || force)) {
            return;
        }
        this.visible = visible;
        if (this.bot.bossbar.actionBar) {
            return;
        }
        this.bot.core.run("minecraft:bossbar set " + this.id + " visible " + visible);
    }

    public long max() {
        return this.max;
    }

    public void setMax(long max) {
        this.setMax(max, false);
    }

    public void setMax(long max, boolean force) {
        if (!(this.max != max && this.gotSecret || force)) {
            return;
        }
        this.max = max;
        if (this.bot.bossbar.actionBar) {
            return;
        }
        this.bot.core.run("minecraft:bossbar set " + this.id + " max " + max);
    }
}

