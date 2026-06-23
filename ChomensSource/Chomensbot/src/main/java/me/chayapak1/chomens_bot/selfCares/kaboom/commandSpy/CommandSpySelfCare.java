/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.kaboom.commandSpy;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.selfCare.SelfCare;
import net.kyori.adventure.text.Component;

public class CommandSpySelfCare
extends SelfCare {
    public CommandSpySelfCare(Bot bot) {
        super(bot);
        this.needsRunning = true;
    }

    @Override
    public boolean shouldRun() {
        return this.bot.serverFeatures.hasCommandSpy && this.bot.config.selfCare.cspy;
    }

    @Override
    public void onMessageReceived(Component component, String string) {
        if (string.equals("Successfully enabled CommandSpy")) {
            this.needsRunning = false;
        } else if (string.equals("Successfully disabled CommandSpy")) {
            this.needsRunning = true;
        }
    }

    @Override
    public void run() {
        if (this.bot.options.useChat || !this.bot.options.coreCommandSpy) {
            this.bot.chat.sendCommandInstantly("commandspy:commandspy on");
        } else {
            this.bot.core.run("commandspy:commandspy " + this.bot.profile.getIdAsString() + " on");
        }
    }

    @Override
    public void cleanup() {
        this.needsRunning = true;
    }
}

