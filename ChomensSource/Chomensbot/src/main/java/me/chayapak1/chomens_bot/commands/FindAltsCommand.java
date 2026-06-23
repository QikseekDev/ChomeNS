/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import it.unimi.dsi.fastutil.Pair;
import java.util.List;
import java.util.Map;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.plugins.DatabasePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class FindAltsCommand
extends Command {
    private static final String ALL_SERVER_FLAG = "allserver";
    private static final String ALL_SERVERS_FLAG = "allservers";
    private static final int LIMIT = 200;

    public FindAltsCommand() {
        super("findalts", new String[]{"-allservers <player|ip>", "<player|ip>"}, new String[]{"alts", "sameip"}, TrustLevel.PUBLIC, false, new ChatPacketType[]{ChatPacketType.DISGUISED});
    }

    @Override
    public Component execute(CommandContext context) throws Exception {
        Bot bot = context.bot;
        if (Main.database == null) {
            throw new CommandException(Component.translatable("commands.generic.error.database_disabled"));
        }
        Main.database.checkOverloaded();
        List<String> flags = context.getFlags(true, ALL_SERVER_FLAG, ALL_SERVERS_FLAG);
        boolean allServer = flags.contains(ALL_SERVER_FLAG) || flags.contains(ALL_SERVERS_FLAG);
        String player = context.getString(true, true);
        DatabasePlugin.EXECUTOR_SERVICE.execute(() -> {
            PlayerEntry playerInTheServer = bot.players.getEntry(player);
            String ipFromUsername = playerInTheServer == null || playerInTheServer.persistingData.ip == null ? bot.playersDatabase.getPlayerIP(player) : playerInTheServer.persistingData.ip;
            if (ipFromUsername == null) {
                context.sendOutput(this.handle(bot, player, player, allServer));
            } else {
                context.sendOutput(this.handle(bot, ipFromUsername, player, allServer));
            }
        });
        return null;
    }

    private Component handle(Bot bot, String targetIP, String player, boolean allServer) {
        Map<String, Pair<Long, String>> altsMap = bot.playersDatabase.findPlayerAlts(targetIP, allServer, 200);
        TextComponent playerComponent = Component.text(player, bot.colorPalette.username);
        boolean isIP = targetIP.equals(player);
        Component component = Component.translatable("commands.findalts.output", bot.colorPalette.defaultColor).arguments(Component.translatable(isIP ? "commands.findalts.ip" : "commands.findalts.player"), isIP ? playerComponent : Component.translatable("%s (%s)", playerComponent, Component.text(targetIP, bot.colorPalette.number))).appendNewline();
        List<String> sorted2 = altsMap.entrySet().stream().sorted((a, b) -> {
            long aTime = (Long)((Pair)a.getValue()).left();
            long bTime = (Long)((Pair)b.getValue()).left();
            return Long.compare(bTime, aTime);
        }).map(Map.Entry::getKey).toList();
        int i = 0;
        for (String username : sorted2) {
            component = component.append(Component.text(username).color((i++ & 1) == 0 ? bot.colorPalette.primary : bot.colorPalette.secondary)).appendSpace();
        }
        return component;
    }
}

