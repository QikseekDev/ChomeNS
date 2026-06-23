/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.essentials;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.selfCare.EssentialsSelfCare;
import net.kyori.adventure.text.Component;

public class MuteSelfCare
extends EssentialsSelfCare {
    public MuteSelfCare(Bot bot) {
        super(bot);
        this.needsRunning = false;
    }

    @Override
    public boolean shouldRun() {
        return this.bot.serverFeatures.hasEssentials && this.bot.config.selfCare.mute;
    }

    @Override
    public void onMessageReceived(Component component, String string) {
        if (string.startsWith(this.messages.muted)) {
            this.needsRunning = true;
        } else if (string.equals(this.messages.unmuted)) {
            this.needsRunning = false;
        }
    }

    @Override
    public void run() {
        this.runCommand("essentials:mute " + this.getUUIDOrBlank());
        this.needsRunning = false;
    }

    @Override
    public void cleanup() {
        this.needsRunning = false;
    }
}

