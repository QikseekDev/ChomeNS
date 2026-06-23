/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.selfCares.kaboom.extras;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.data.selfCare.SelfCare;
import me.chayapak1.chomens_bot.util.IllegalCharactersUtilities;

public class UsernameSelfCare
extends SelfCare {
    private long usernameStartTime;
    private long successTime;

    public UsernameSelfCare(Bot bot) {
        super(bot);
    }

    @Override
    public void onPlayerChangedUsername(PlayerEntry target, String from, String to) {
        if (target.profile.getId().equals(this.bot.profile.getId())) {
            boolean bl = this.needsRunning = !to.equals(this.bot.username);
            if (this.needsRunning) {
                this.usernameStartTime = System.currentTimeMillis();
            } else {
                this.successTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public boolean shouldRun() {
        String username = this.bot.username.replace("\u00a7", "&");
        return System.currentTimeMillis() - this.usernameStartTime >= 2000L && System.currentTimeMillis() - this.successTime >= 4000L && IllegalCharactersUtilities.isValidChatString(username) && this.bot.serverFeatures.hasExtras && this.bot.config.selfCare.username;
    }

    @Override
    public void run() {
        this.bot.chat.sendCommandInstantly("extras:username " + this.bot.username);
    }
}

