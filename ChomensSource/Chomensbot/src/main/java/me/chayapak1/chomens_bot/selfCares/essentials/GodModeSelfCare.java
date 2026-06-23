/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.essentials;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.selfCare.EssentialsSelfCare;
import net.kyori.adventure.text.Component;

public class GodModeSelfCare
extends EssentialsSelfCare {
    public GodModeSelfCare(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return this.bot.serverFeatures.hasEssentials && this.bot.config.selfCare.god;
    }

    @Override
    public void onMessageReceived(Component component, String string) {
        if (string.equals(this.messages.godModeEnable)) {
            this.needsRunning = false;
        } else if (string.startsWith(this.messages.godModeDisable)) {
            this.needsRunning = true;
        }
    }

    @Override
    public void run() {
        this.runCommand("essentials:godmode " + this.getUsernameOrBlank() + "enable");
    }
}

