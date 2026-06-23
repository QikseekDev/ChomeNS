/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.essentials;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.selfCare.EssentialsSelfCare;
import net.kyori.adventure.text.Component;

public class SocialSpySelfCare
extends EssentialsSelfCare {
    public SocialSpySelfCare(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return this.bot.serverFeatures.hasEssentials && this.bot.config.selfCare.socialspy;
    }

    @Override
    public void onMessageReceived(Component component, String string) {
        if (string.equals(String.format(this.messages.socialSpyEnable, this.bot.username))) {
            this.needsRunning = false;
        } else if (string.equals(String.format(this.messages.socialSpyDisable, this.bot.username))) {
            this.needsRunning = true;
        }
    }

    @Override
    public void run() {
        this.runCommand("essentials:socialspy " + this.getUsernameOrBlank() + "enable");
    }
}

