/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GrepLogCommand
extends Command {
    private Thread thread;

    public GrepLogCommand() {
        super("greplog", new String[]{"<input>", "-ignorecase ...", "-regex ...", "stop"}, new String[]{"logquery", "findlog"}, TrustLevel.PUBLIC);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        if (Main.discord == null || Main.discord.jda == null) {
            throw new CommandException(Component.translatable("commands.generic.error.discord_disabled"));
        }
        List<String> flags = context.getFlags(true, "ignorecase", "regex");
        boolean ignoreCase = flags.contains("ignorecase");
        boolean regex = flags.contains("regex");
        String input = context.getString(true, true);
        if (input.equalsIgnoreCase("stop")) {
            if (this.thread == null) {
                throw new CommandException(Component.translatable("commands.greplog.error.not_running"));
            }
            bot.grepLog.running = false;
            bot.grepLog.pattern = null;
            this.thread = null;
            return Component.translatable("commands.greplog.stopped", bot.colorPalette.defaultColor);
        }
        if (this.thread != null) {
            throw new CommandException(Component.translatable("commands.greplog.error.already_running"));
        }
        context.sendOutput(Component.translatable("commands.greplog.started", bot.colorPalette.defaultColor).arguments(Component.text(input, bot.colorPalette.string)));
        this.thread = new Thread(() -> {
            try {
                bot.grepLog.search(context, input, ignoreCase, regex);
            }
            catch (CommandException e) {
                context.sendOutput(e.message.color(NamedTextColor.RED));
            }
            this.thread = null;
        });
        this.thread.start();
        return null;
    }
}

