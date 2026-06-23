/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.plugins.DatabasePlugin;
import me.chayapak1.chomens_bot.plugins.IPFilterPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class IPFilterCommand
extends Command {
    private static final String FORCE_IP_FLAG = "forceip";

    public IPFilterCommand() {
        super("ipfilter", new String[]{"add <player/ip> [reason]", "-forceip add ...", "remove <index>", "clear", "list"}, new String[]{"filterip", "banip", "ipban"}, TrustLevel.ADMIN);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        String action;
        Bot bot = context.bot;
        switch (action = context.getAction()) {
            case "add": {
                String ip;
                List<String> flags = context.getFlags(true, FORCE_IP_FLAG);
                String rawIP = context.getString(false, true);
                String reason = context.getString(true, false);
                PlayerEntry player = bot.players.getEntry(rawIP);
                String string = ip = !flags.contains(FORCE_IP_FLAG) && player != null && player.persistingData.ip != null ? player.persistingData.ip : rawIP;
                if (IPFilterPlugin.localList.containsKey(ip)) {
                    throw new CommandException(Component.translatable("commands.ipfilter.add.error.already_exists", Component.text(ip)));
                }
                DatabasePlugin.EXECUTOR_SERVICE.execute(() -> bot.ipFilter.add(ip, reason));
                if (reason.isEmpty()) {
                    return Component.translatable("commands.filter.add.no_reason", bot.colorPalette.defaultColor, Component.text(ip, bot.colorPalette.username));
                }
                return Component.translatable("commands.filter.add.reason", bot.colorPalette.defaultColor, Component.text(ip, bot.colorPalette.username), Component.text(reason, bot.colorPalette.string));
            }
            case "remove": {
                context.checkOverloadArgs(2);
                int index = context.getInteger(true);
                try {
                    String targetIP = new ArrayList<String>(IPFilterPlugin.localList.keySet()).get(index);
                    if (targetIP == null) {
                        throw new IllegalArgumentException();
                    }
                    DatabasePlugin.EXECUTOR_SERVICE.execute(() -> bot.ipFilter.remove(targetIP));
                    return Component.translatable("commands.ipfilter.remove.output", bot.colorPalette.defaultColor, Component.text(targetIP, bot.colorPalette.username));
                }
                catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
                    throw new CommandException(Component.translatable("commands.generic.error.invalid_index"));
                }
            }
            case "clear": {
                context.checkOverloadArgs(1);
                DatabasePlugin.EXECUTOR_SERVICE.execute(bot.ipFilter::clear);
                return Component.translatable("commands.ipfilter.clear.output", bot.colorPalette.defaultColor);
            }
            case "list": {
                context.checkOverloadArgs(1);
                ArrayList<TranslatableComponent> filtersComponents = new ArrayList<TranslatableComponent>();
                int index = 0;
                for (Map.Entry<String, String> entry : IPFilterPlugin.localList.entrySet()) {
                    String ip = entry.getKey();
                    String reason = entry.getValue();
                    Component reasonComponent = Component.empty().color(NamedTextColor.DARK_GRAY);
                    if (!reason.isEmpty()) {
                        reasonComponent = reasonComponent.append(Component.text("(")).append(Component.translatable("commands.ipfilter.list.reason", (TextColor)NamedTextColor.GRAY, Component.text(reason, bot.colorPalette.string))).append(Component.text(")"));
                    }
                    filtersComponents.add(Component.translatable("%s \u203a %s %s", (TextColor)NamedTextColor.DARK_GRAY, Component.text(index, bot.colorPalette.number), Component.text(ip, bot.colorPalette.username), reasonComponent));
                    ++index;
                }
                return ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().append(Component.translatable("commands.ipfilter.list.filtered_ips_text", (TextColor)NamedTextColor.GREEN))).append(Component.text("(", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.text(IPFilterPlugin.localList.size(), (TextColor)NamedTextColor.GRAY))).append(Component.text(")", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.newline())).append(Component.join(JoinConfiguration.newlines(), filtersComponents));
            }
        }
        throw new CommandException(Component.translatable("commands.generic.error.invalid_action"));
    }
}

