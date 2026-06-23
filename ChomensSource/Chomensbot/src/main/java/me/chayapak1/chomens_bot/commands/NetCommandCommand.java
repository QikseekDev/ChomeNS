/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.util.ArrayList;
import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.command.contexts.RemoteCommandContext;
import net.kyori.adventure.text.Component;

public class NetCommandCommand
extends Command {
    public NetCommandCommand() {
        super("netcmd", new String[]{"<servers separated by a comma> <command>"}, new String[]{"networkcommand", "irccommand", "remotecommand"}, TrustLevel.TRUSTED);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        List<Bot> bots;
        String rawServers = context.getString(false, true, true);
        List<Bot> allBots = context.bot.bots;
        if (rawServers.equals("all")) {
            bots = allBots;
        } else {
            bots = new ArrayList<Bot>();
            String[] servers = rawServers.split(",");
            for (Bot bot : allBots) {
                for (String server : servers) {
                    if (server.isBlank() || !bot.getServerString(true).toLowerCase().trim().contains(server.toLowerCase())) continue;
                    bots.add(bot);
                }
            }
        }
        if (bots.isEmpty()) {
            throw new CommandException(Component.translatable("commands.netcmd.error.no_servers_found"));
        }
        String command = context.getString(true, true);
        for (Bot bot : bots) {
            if (!bot.loggedIn) continue;
            RemoteCommandContext remoteContext = new RemoteCommandContext(bot, context);
            context.bot.commandHandler.executeCommand(command, remoteContext);
        }
        return null;
    }
}

