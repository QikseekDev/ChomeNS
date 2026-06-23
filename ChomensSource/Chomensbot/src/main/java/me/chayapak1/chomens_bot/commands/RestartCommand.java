/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import net.kyori.adventure.text.Component;

public class RestartCommand
extends Command {
    public RestartCommand() {
        super("restart", new String[]{"[reason]"}, new String[0], TrustLevel.OWNER);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        String reason = context.getString(true, false);
        new Thread(() -> Main.stop(12, reason.isEmpty() ? null : reason, I18nUtilities.get("info.restarting")), "ChomeNS Bot Shutdown Thread").start();
        return Component.translatable("commands.restart.output", bot.colorPalette.defaultColor);
    }
}

