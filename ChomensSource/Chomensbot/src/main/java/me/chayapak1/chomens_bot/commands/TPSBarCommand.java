/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class TPSBarCommand
extends Command {
    public TPSBarCommand() {
        super("tpsbar", new String[]{"<on|off>"}, new String[]{"tps"}, TrustLevel.PUBLIC);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        String action;
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        switch (action = context.getString(false, true, true)) {
            case "on": {
                bot.tps.on();
                return Component.translatable("commands.tpsbar.output", bot.colorPalette.defaultColor, Component.translatable("commands.generic.enabled", (TextColor)NamedTextColor.GREEN));
            }
            case "off": {
                bot.tps.off();
                return Component.translatable("commands.tpsbar.output", bot.colorPalette.defaultColor, Component.translatable("commands.generic.disabled", (TextColor)NamedTextColor.RED));
            }
        }
        throw new CommandException(Component.translatable("commands.generic.error.invalid_action"));
    }
}

