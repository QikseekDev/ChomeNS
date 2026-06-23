/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.filter.PlayerFilter;
import me.chayapak1.chomens_bot.plugins.DatabasePlugin;
import me.chayapak1.chomens_bot.plugins.PlayerFilterPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class FilterCommand
extends Command {
    public FilterCommand() {
        super("filter", new String[]{"add <player> [reason]", "-ignorecase add <player> [reason]", "-regex add <player> [reason]", "remove <index>", "clear", "list"}, new String[]{"filterplayer", "ban", "blacklist"}, TrustLevel.ADMIN);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        String action;
        Bot bot = context.bot;
        List<String> flags = context.getFlags(true, "ignorecase", "regex");
        boolean ignoreCase = flags.contains("ignorecase");
        boolean regex = flags.contains("regex");
        switch (action = context.getString(false, true, true)) {
            case "add": {
                String player = context.getString(false, true);
                String reason = context.getString(true, false);
                if (PlayerFilterPlugin.localList.stream().map(PlayerFilter::playerName).toList().contains(player)) {
                    throw new CommandException(Component.translatable("commands.filter.add.error.already_exists", Component.text(player)));
                }
                if (regex) {
                    try {
                        Pattern.compile(player);
                    }
                    catch (PatternSyntaxException e) {
                        throw new CommandException(Component.translatable("commands.filter.error.invalid_regex", Component.text(e.toString())));
                    }
                }
                DatabasePlugin.EXECUTOR_SERVICE.execute(() -> bot.playerFilter.add(player, reason, regex, ignoreCase));
                if (reason.isEmpty()) {
                    return Component.translatable("commands.filter.add.no_reason", bot.colorPalette.defaultColor, Component.text(player, bot.colorPalette.username));
                }
                return Component.translatable("commands.filter.add.reason", bot.colorPalette.defaultColor, Component.text(player, bot.colorPalette.username), Component.text(reason, bot.colorPalette.string));
            }
            case "remove": {
                context.checkOverloadArgs(2);
                int index = context.getInteger(true);
                try {
                    PlayerFilter player = PlayerFilterPlugin.localList.get(index);
                    if (player == null) {
                        throw new IllegalArgumentException();
                    }
                    DatabasePlugin.EXECUTOR_SERVICE.execute(() -> bot.playerFilter.remove(player.playerName()));
                    return Component.translatable("commands.filter.remove.output", bot.colorPalette.defaultColor, Component.text(player.playerName(), bot.colorPalette.username));
                }
                catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
                    throw new CommandException(Component.translatable("commands.generic.error.invalid_index"));
                }
            }
            case "clear": {
                context.checkOverloadArgs(1);
                DatabasePlugin.EXECUTOR_SERVICE.execute(bot.playerFilter::clear);
                return Component.translatable("commands.filter.clear.output", bot.colorPalette.defaultColor);
            }
            case "list": {
                context.checkOverloadArgs(1);
                ArrayList<TranslatableComponent> filtersComponents = new ArrayList<TranslatableComponent>();
                int index = 0;
                for (PlayerFilter player : PlayerFilterPlugin.localList) {
                    Component options = Component.empty().color(NamedTextColor.DARK_GRAY);
                    if (player.ignoreCase() || player.regex()) {
                        ArrayList<TranslatableComponent> args2 = new ArrayList<TranslatableComponent>();
                        if (player.ignoreCase()) {
                            args2.add(Component.translatable("commands.filter.list.ignore_case"));
                        }
                        if (player.regex()) {
                            args2.add(Component.translatable("commands.filter.list.regex"));
                        }
                        options = options.append(Component.text("(")).append(Component.join(JoinConfiguration.commas(true), args2).color(bot.colorPalette.string)).append(Component.text(")")).append(Component.space());
                    }
                    if (!player.reason().isEmpty()) {
                        options = options.append(Component.text("(")).append(Component.translatable("commands.filter.list.reason", (TextColor)NamedTextColor.GRAY, Component.text(player.reason(), bot.colorPalette.string))).append(Component.text(")"));
                    }
                    filtersComponents.add(Component.translatable("%s \u203a %s %s", (TextColor)NamedTextColor.DARK_GRAY, Component.text(index, bot.colorPalette.number), Component.text(player.playerName(), bot.colorPalette.username), options));
                    ++index;
                }
                return ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().append(Component.translatable("commands.filter.list.filtered_players_text", (TextColor)NamedTextColor.GREEN))).append(Component.text("(", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.text(PlayerFilterPlugin.localList.size(), (TextColor)NamedTextColor.GRAY))).append(Component.text(")", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.newline())).append(Component.join(JoinConfiguration.newlines(), filtersComponents));
            }
        }
        throw new CommandException(Component.translatable("commands.generic.error.invalid_action"));
    }
}

