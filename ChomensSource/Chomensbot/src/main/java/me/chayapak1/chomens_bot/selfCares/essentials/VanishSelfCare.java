/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.essentials;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.selfCare.EssentialsSelfCare;
import net.kyori.adventure.text.Component;

public class VanishSelfCare
extends EssentialsSelfCare {
    public boolean visible;
    private boolean isVanished;

    public VanishSelfCare(Bot bot) {
        super(bot);
        this.visible = !this.bot.config.selfCare.vanish;
        this.isVanished = false;
    }

    @Override
    public boolean shouldRun() {
        return this.bot.serverFeatures.hasEssentials && this.bot.config.selfCare.vanish && this.visible == this.isVanished;
    }

    @Override
    public void onMessageReceived(Component component, String string) {
        if (string.equals(String.format(this.messages.vanishEnable1, this.bot.username)) || string.equals(this.messages.vanishEnable2)) {
            this.needsRunning = this.visible;
            this.isVanished = true;
        } else if (string.equals(String.format(this.messages.vanishDisable, this.bot.username))) {
            this.needsRunning = !this.visible;
            this.isVanished = false;
        }
    }

    @Override
    public void run() {
        this.runCommand("essentials:vanish " + this.getUsernameOrBlank() + (this.visible ? "disable" : "enable"));
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.isVanished = false;
    }
}

