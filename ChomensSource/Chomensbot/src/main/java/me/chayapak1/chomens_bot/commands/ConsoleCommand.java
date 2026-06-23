/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.util.ArrayList;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class ConsoleCommand
extends Command {
    public ConsoleCommand() {
        super("console", new String[]{"server <server>", "discord <message>", "logtoconsole <true|false>", "printdisconnectedreason <true|false>"}, new String[]{"csvr"}, TrustLevel.OWNER, true);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        String action;
        Bot bot = context.bot;
        switch (action = !context.userInputCommandName.equals(this.name) ? "server" : context.getString(false, true, true)) {
            case "server": {
                ArrayList<String> servers = new ArrayList<String>();
                for (Bot eachBot : bot.bots) {
                    servers.add(eachBot.getServerString(true));
                }
                String server = context.getString(true, true);
                if (server.equalsIgnoreCase("all")) {
                    Main.console.consoleServer = "all";
                    return Component.translatable("commands.console.server.set", bot.colorPalette.defaultColor, Component.translatable("commands.console.server.all_servers"));
                }
                try {
                    Main.console.consoleServer = servers.stream().filter(eachServer -> eachServer.toLowerCase().contains(server)).findFirst().orElse("all");
                    return Component.translatable("commands.console.server.set", bot.colorPalette.defaultColor, Component.text(Main.console.consoleServer));
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    throw new CommandException(Component.translatable("commands.console.server.error.invalid_server", Component.text(server)));
                }
            }
            case "discord": {
                if (Main.discord == null || Main.discord.jda == null) {
                    throw new CommandException(Component.translatable("commands.generic.error.discord_disabled"));
                }
                String channelId = context.bot.options.discordChannelId;
                if (channelId == null) {
                    return null;
                }
                String message = context.getString(true, true);
                Main.discord.sendMessageInstantly(message, channelId, true);
                return null;
            }
            case "logtoconsole": {
                boolean bool;
                context.checkOverloadArgs(2);
                bot.logger.logToConsole = bool = context.getBoolean(true).booleanValue();
                return Component.translatable("commands.console.logtoconsole.set", bot.colorPalette.defaultColor, bool ? Component.translatable("commands.generic.enabled", (TextColor)NamedTextColor.GREEN) : Component.translatable("commands.generic.disabled", (TextColor)NamedTextColor.RED));
            }
            case "printdisconnectedreason": {
                boolean bool;
                context.checkOverloadArgs(2);
                bot.printDisconnectedCause = bool = context.getBoolean(true).booleanValue();
                return Component.translatable("commands.console.printdisconnectedreason.set", bot.colorPalette.defaultColor, bool ? Component.translatable("commands.generic.enabled", (TextColor)NamedTextColor.GREEN) : Component.translatable("commands.generic.disabled", (TextColor)NamedTextColor.RED));
            }
        }
        return null;
    }
}

