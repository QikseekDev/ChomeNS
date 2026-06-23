/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.command.contexts;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class RemoteCommandContext
extends CommandContext {
    public final Bot targetBot;
    public final CommandContext source;

    public RemoteCommandContext(Bot targetBot, CommandContext source2) {
        super(targetBot, source2.prefix, source2.sender, false);
        this.targetBot = targetBot;
        this.source = source2;
    }

    @Override
    public void sendOutput(Component component) {
        this.source.sendOutput(Component.translatable("[%s] %s", Component.text(this.targetBot.getServerString(), (TextColor)NamedTextColor.GRAY), ((TextComponent)Component.empty().color(NamedTextColor.WHITE)).append(component)).color(NamedTextColor.DARK_GRAY));
    }
}

