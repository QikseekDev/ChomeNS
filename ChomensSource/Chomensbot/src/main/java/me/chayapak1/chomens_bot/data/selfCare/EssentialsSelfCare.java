/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.selfCare;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Configuration;
import me.chayapak1.chomens_bot.data.selfCare.SelfCare;

public abstract class EssentialsSelfCare
extends SelfCare {
    public final Configuration.BotOption.EssentialsMessages messages;

    public EssentialsSelfCare(Bot bot) {
        super(bot);
        this.messages = bot.options.essentialsMessages;
        this.needsRunning = true;
    }

    @Override
    public void cleanup() {
        this.needsRunning = true;
    }

    private String getUseCoreValueOrBlank(String value) {
        return !this.bot.options.useChat ? this.bot.username + " " : "";
    }

    public String getUsernameOrBlank() {
        return this.getUseCoreValueOrBlank(this.bot.username);
    }

    public String getUUIDOrBlank() {
        return this.getUseCoreValueOrBlank(this.bot.profile.getIdAsString());
    }

    public void runCommand(String command) {
        if (this.bot.options.useChat) {
            this.bot.chat.sendCommandInstantly(command);
        } else {
            this.bot.core.run(command);
        }
    }
}

