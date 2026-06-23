/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.util.ArrayList;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class WhitelistCommand
extends Command {
    public WhitelistCommand() {
        super("whitelist", new String[]{"enable", "disable", "add <player>", "remove <index>", "clear", "list"}, new String[0], TrustLevel.ADMIN);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        String action;
        Bot bot = context.bot;
        switch (action = context.getAction()) {
            case "enable": {
                context.checkOverloadArgs(1);
                bot.whitelist.enable();
                return Component.translatable("commands.whitelist.enable", bot.colorPalette.defaultColor);
            }
            case "disable": {
                context.checkOverloadArgs(1);
                bot.whitelist.disable();
                return Component.translatable("commands.whitelist.disable", bot.colorPalette.defaultColor);
            }
            case "add": {
                String player = context.getString(true, true);
                bot.whitelist.add(player);
                return Component.translatable("commands.whitelist.add", bot.colorPalette.defaultColor, Component.text(player, bot.colorPalette.username));
            }
            case "remove": {
                try {
                    int index = context.getInteger(true);
                    String player = bot.whitelist.remove(index);
                    return Component.translatable("commands.whitelist.remove", bot.colorPalette.defaultColor, Component.text(player, bot.colorPalette.username));
                }
                catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
                    throw new CommandException(Component.translatable("commands.generic.error.invalid_index"));
                }
            }
            case "clear": {
                context.checkOverloadArgs(1);
                bot.whitelist.clear();
                return Component.translatable("commands.whitelist.clear", bot.colorPalette.defaultColor);
            }
            case "list": {
                context.checkOverloadArgs(1);
                ArrayList<TranslatableComponent> playersComponent = new ArrayList<TranslatableComponent>();
                int index = 0;
                for (String player : bot.whitelist.list) {
                    playersComponent.add(Component.translatable("%s \u203a %s", (TextColor)NamedTextColor.DARK_GRAY, Component.text(index, bot.colorPalette.number), Component.text(player, bot.colorPalette.username)));
                    ++index;
                }
                return ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().append(Component.translatable("commands.whitelist.whitelisted_players_text", (TextColor)NamedTextColor.GREEN))).append(Component.text("(", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.text(bot.whitelist.list.size(), (TextColor)NamedTextColor.GRAY))).append(Component.text(")", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.newline())).append(Component.join(JoinConfiguration.newlines(), playersComponent));
            }
        }
        throw new CommandException(Component.translatable("commands.generic.error.invalid_action"));
    }
}

