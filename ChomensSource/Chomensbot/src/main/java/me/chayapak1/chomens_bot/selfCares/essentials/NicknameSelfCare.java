/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.essentials;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.selfCare.EssentialsSelfCare;
import net.kyori.adventure.text.Component;

public class NicknameSelfCare
extends EssentialsSelfCare {
    public NicknameSelfCare(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return this.bot.serverFeatures.hasEssentials && this.bot.config.selfCare.nickname;
    }

    @Override
    public void onMessageReceived(Component component, String string) {
        if (string.equals(this.messages.nickNameRemove)) {
            this.needsRunning = false;
        } else if (string.startsWith(this.messages.nickNameSet)) {
            this.needsRunning = true;
        }
    }

    @Override
    public void run() {
        this.runCommand("essentials:nickname " + this.getUUIDOrBlank() + "off");
    }
}

