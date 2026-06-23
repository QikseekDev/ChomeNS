/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;

public class BruhifyPlugin
implements Listener {
    private final Bot bot;
    public String bruhifyText = "";
    private int startHue = 0;

    public BruhifyPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    @Override
    public void onLocalTick() {
        if (this.bruhifyText.isBlank()) {
            return;
        }
        int increment = 360 / Math.max(this.bruhifyText.length(), 20);
        ArrayList components = new ArrayList();
        AtomicInteger hue = new AtomicInteger(this.startHue);
        this.bruhifyText.codePoints().forEach(character -> {
            components.add(Component.text(new String(Character.toChars(character))).color(TextColor.color(HSVLike.hsvLike((float)hue.get() / 360.0f, 1.0f, 1.0f))));
            hue.set((hue.get() + increment) % 360);
        });
        this.bot.chat.actionBar(Component.join(JoinConfiguration.noSeparators(), components));
        this.startHue = (this.startHue + increment) % 360;
    }
}

