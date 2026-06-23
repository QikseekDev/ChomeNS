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
import net.kyori.adventure.text.TextComponent;

public class KickCommand
extends Command {
    private static final String PAYLOAD_LONGSTRING = "Hi\u00a7k" + "\u732b".repeat(31000) + "\u00a7r:>";

    public KickCommand() {
        super("kick", new String[]{"<player>"}, new String[0], TrustLevel.TRUSTED);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        String player = context.getString(false, true, true);
        bot.core.run("/title " + player + " title \"" + PAYLOAD_LONGSTRING + "\"");
        return ((TextComponent)Component.text("Executed longstring kick on ").append(Component.text(player, bot.colorPalette.string))).color(bot.colorPalette.defaultColor);
    }
}

