/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.command.contexts;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.chomeNSMod.clientboundPackets.ClientboundMessagePacket;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import net.kyori.adventure.text.Component;

public class ChomeNSModCommandContext
extends CommandContext {
    public ChomeNSModCommandContext(Bot bot, PlayerEntry sender) {
        super(bot, ".cbot ", sender, true);
    }

    @Override
    public void sendOutput(Component component) {
        Component rendered = I18nUtilities.render(component);
        this.bot.chomeNSMod.send(this.sender, new ClientboundMessagePacket(rendered));
    }

    @Override
    public Component displayName() {
        return this.sender.displayName;
    }
}

