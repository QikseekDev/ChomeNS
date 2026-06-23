/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.command.contexts.ConsoleCommandContext;
import me.chayapak1.chomens_bot.plugins.CommandHandlerPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class HelpCommand
extends Command {
    public HelpCommand() {
        super("help", new String[]{"[command]"}, new String[]{"heko", "cmds", "commands"}, TrustLevel.PUBLIC);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        String commandName = context.getString(false, false);
        if (commandName.isBlank()) {
            return this.getCommandList(context);
        }
        return this.getUsages(context, commandName);
    }

    public Component getCommandList(CommandContext context) {
        ObjectArrayList list = new ObjectArrayList();
        for (TrustLevel level2 : TrustLevel.values()) {
            list.addAll(this.getCommandListByTrustLevel(context, level2));
        }
        Component trustLevels = Component.join(JoinConfiguration.spaces(), Arrays.stream(TrustLevel.values()).map(level -> level.component).toList());
        return ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().append(Component.translatable("commands.help.commands_text", (TextColor)NamedTextColor.GRAY))).append(Component.text("(", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.text(list.size(), (TextColor)NamedTextColor.GREEN))).append(Component.text(") ", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.text("(", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.translatable("%s", trustLevels))).append(Component.text(") - ", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.join(JoinConfiguration.separator(Component.space()), list));
    }

    public List<Component> getCommandListByTrustLevel(CommandContext context, TrustLevel trustLevel) {
        Bot bot = context.bot;
        ObjectArrayList<Component> list = new ObjectArrayList<Component>();
        ObjectArrayList commandNames = new ObjectArrayList();
        for (Command command : CommandHandlerPlugin.COMMANDS) {
            if (command.trustLevel != trustLevel || command.consoleOnly && !(context instanceof ConsoleCommandContext)) continue;
            commandNames.add(command.name);
        }
        Collections.sort(commandNames);
        for (String name : commandNames) {
            String clickSuggestion = context.prefix + name;
            String insertionSuggestion = context.prefix + this.name + " " + name;
            list.add(((TextComponent)((TextComponent)((TextComponent)Component.text(name).color(trustLevel.component.color())).clickEvent(ClickEvent.suggestCommand(clickSuggestion))).insertion(insertionSuggestion)).hoverEvent(HoverEvent.showText(((TextComponent)((TextComponent)((TextComponent)Component.empty().color(NamedTextColor.GREEN)).append(Component.translatable("commands.help.hover.click_to_command", Component.text(clickSuggestion, bot.colorPalette.string)))).append(Component.newline())).append(Component.translatable("commands.help.hover.shift_click_to_help_command", Component.text(insertionSuggestion, bot.colorPalette.string))))));
        }
        return list;
    }

    public Component getUsages(CommandContext context, String commandName) throws CommandException {
        Bot bot = context.bot;
        String prefix = context.prefix;
        for (Command command : CommandHandlerPlugin.COMMANDS) {
            if (!command.name.equalsIgnoreCase(commandName) && !Arrays.stream(command.aliases).toList().contains(commandName.toLowerCase())) continue;
            String actualCommandName = command.name.toLowerCase();
            ObjectArrayList usages = new ObjectArrayList();
            usages.add(((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().color(NamedTextColor.GRAY)).append(Component.text(prefix + actualCommandName, bot.colorPalette.secondary))).append(Component.text((String)(command.aliases.length > 0 && !command.aliases[0].isEmpty() ? " (" + String.join((CharSequence)", ", command.aliases) + ")" : ""), (TextColor)NamedTextColor.WHITE))).append(Component.text(" - "))).append(Component.translatable(String.format("commands.%s.description", actualCommandName))));
            usages.add(((TextComponent)Component.empty().append(Component.translatable("commands.help.trust_level", (TextColor)NamedTextColor.GREEN))).append(command.trustLevel.component.append(Component.text(" - ")).append(Component.text(command.trustLevel.level))));
            for (String usage : command.usages) {
                Component usageComponent = ((TextComponent)Component.empty().append(Component.text(prefix + actualCommandName, bot.colorPalette.secondary))).append(Component.text(" "));
                usageComponent = usageComponent.append(Component.text(usage, bot.colorPalette.string));
                usages.add(usageComponent);
            }
            return Component.join(JoinConfiguration.separator(Component.newline()), usages);
        }
        throw new CommandException(Component.translatable("commands.help.error.unknown_command"));
    }
}

