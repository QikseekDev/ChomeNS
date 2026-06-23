/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.cloop.CommandLoop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class CloopCommand
extends Command {
    public CloopCommand() {
        super("cloop", new String[]{"add <interval> <ChronoUnit> <command>", "remove <index>", "clear", "list"}, new String[]{"commandloop"}, TrustLevel.TRUSTED);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        String action;
        Bot bot = context.bot;
        switch (action = context.getAction()) {
            case "add": {
                ChronoUnit unit;
                long interval = context.getLong(true);
                if (interval < 1L) {
                    interval = 1L;
                }
                if ((unit = context.getEnum(true, ChronoUnit.class)) == ChronoUnit.NANOS && interval < 1000L) {
                    throw new CommandException(Component.translatable("commands.cloop.add.error.too_low_nanoseconds"));
                }
                String command = context.getString(true, true);
                try {
                    bot.cloop.add(unit, interval, command);
                }
                catch (Exception e) {
                    throw new CommandException(Component.text(e.toString()));
                }
                return Component.translatable("commands.cloop.add.output", bot.colorPalette.defaultColor, Component.text(command, bot.colorPalette.string), Component.text(interval, bot.colorPalette.number), Component.text(unit.toString(), bot.colorPalette.string));
            }
            case "remove": {
                context.checkOverloadArgs(2);
                try {
                    int index = context.getInteger(true);
                    CommandLoop cloop = bot.cloop.remove(index);
                    return Component.translatable("commands.cloop.remove.output", bot.colorPalette.defaultColor, Component.text(cloop.command(), bot.colorPalette.string));
                }
                catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException ignored) {
                    throw new CommandException(Component.translatable("commands.generic.error.invalid_index"));
                }
            }
            case "clear": {
                context.checkOverloadArgs(1);
                bot.cloop.clear();
                return Component.translatable("commands.cloop.clear.output", bot.colorPalette.defaultColor);
            }
            case "list": {
                context.checkOverloadArgs(1);
                ArrayList<TranslatableComponent> cloopsComponent = new ArrayList<TranslatableComponent>();
                int index = 0;
                for (CommandLoop command : bot.cloop.loops) {
                    cloopsComponent.add(Component.translatable("%s \u203a %s (%s %s)", (TextColor)NamedTextColor.DARK_GRAY, Component.text(index, bot.colorPalette.number), Component.text(command.command(), bot.colorPalette.string), Component.text(command.interval(), bot.colorPalette.number), Component.text(command.unit().toString(), bot.colorPalette.string)));
                    ++index;
                }
                return ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().append(Component.translatable("commands.cloop.list.cloops_text", (TextColor)NamedTextColor.GREEN))).append(Component.text("(", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.text(bot.cloop.loops.size(), (TextColor)NamedTextColor.GRAY))).append(Component.text(")", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.newline())).append(Component.join(JoinConfiguration.newlines(), cloopsComponent));
            }
        }
        throw new CommandException(Component.translatable("commands.generic.error.invalid_action"));
    }
}

