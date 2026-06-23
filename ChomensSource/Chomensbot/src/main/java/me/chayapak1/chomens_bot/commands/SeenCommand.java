/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import it.unimi.dsi.fastutil.Pair;
import java.time.ZoneId;
import java.util.ArrayList;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.plugins.DatabasePlugin;
import me.chayapak1.chomens_bot.util.TimeUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class SeenCommand
extends Command {
    public SeenCommand() {
        super("seen", new String[]{"<player>"}, new String[]{"lastseen"}, TrustLevel.PUBLIC);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        if (Main.database == null) {
            throw new CommandException(Component.translatable("commands.generic.error.database_disabled"));
        }
        Main.database.checkOverloaded();
        String player = context.getString(true, true);
        boolean online = false;
        ArrayList<TranslatableComponent> onlineComponents = new ArrayList<TranslatableComponent>();
        for (Bot eachBot : bot.bots) {
            if (eachBot.players.getEntry(player) == null) continue;
            online = true;
            onlineComponents.add(Component.translatable("commands.seen.error.currently_online", (TextColor)NamedTextColor.RED, Component.text(player), Component.text(eachBot.getServerString())));
        }
        if (online) {
            return Component.join(JoinConfiguration.newlines(), onlineComponents);
        }
        DatabasePlugin.EXECUTOR_SERVICE.execute(() -> {
            try {
                Pair<Long, String> pair = bot.playersDatabase.getPlayerLastSeen(player);
                if (pair == null) {
                    throw new CommandException(Component.translatable("commands.seen.error.never_seen", Component.text(player)));
                }
                long time = pair.left();
                String server = pair.right();
                String formattedTime = TimeUtilities.formatTime(time, "EEEE, MMMM d, yyyy, hh:mm:ss a Z", ZoneId.of("UTC"));
                context.sendOutput(Component.translatable("commands.seen.output", bot.colorPalette.defaultColor, Component.text(player, bot.colorPalette.username), Component.text(formattedTime, bot.colorPalette.string), Component.text(server, bot.colorPalette.string)));
            }
            catch (CommandException e) {
                context.sendOutput(e.message.color(NamedTextColor.RED));
            }
        });
        return null;
    }
}

