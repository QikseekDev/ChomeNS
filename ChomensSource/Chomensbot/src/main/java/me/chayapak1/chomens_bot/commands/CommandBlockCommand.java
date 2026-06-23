/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.cloudburstmc.math.vector.Vector3i;

public class CommandBlockCommand
extends Command {
    private static final Pattern USER_PATTERN = Pattern.compile("\\{username\\{([^{}]+(?:\\{[^{}]*}[^{}]*)*)}}");
    private static final Pattern UUID_PATTERN = Pattern.compile("\\{uuid\\{([^{}]+(?:\\{[^{}]*}[^{}]*)*)}}");

    public CommandBlockCommand() {
        super("cb", new String[]{"", "<command>", "..{username}..", "..{uuid}..", "..{username{regex}}..", "..{uuid{regex}}.."}, new String[]{"cmd", "commandblock", "run", "core"}, TrustLevel.PUBLIC);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        String command = context.getString(true, false);
        if (command.isEmpty()) {
            return this.getInfo(bot);
        }
        try {
            this.runCommand(bot, context, command, null);
        }
        catch (PatternSyntaxException e) {
            throw new CommandException(Component.text(e.toString()));
        }
        return null;
    }

    private Component getInfo(Bot bot) {
        Vector3i from = bot.core.from;
        Vector3i to = bot.core.to;
        Vector3i block = bot.core.block;
        int layers = Math.max(1, to.getY() - from.getY());
        StringBuilder commandBuilder = new StringBuilder("/");
        if (bot.serverFeatures.hasEssentials) {
            commandBuilder.append("essentials:");
        }
        commandBuilder.append("tp ").append(from.getX()).append(" ").append(from.getY()).append(" ").append(from.getZ());
        String command = commandBuilder.toString();
        return Component.translatable("commands.cb.info.output", bot.colorPalette.secondary, Component.translatable("commands.cb.info.size", bot.colorPalette.string, Component.text(256 * layers)), Component.text(layers, bot.colorPalette.string), Component.text(from.toString(), bot.colorPalette.string), Component.text(to.toString(), bot.colorPalette.string), Component.text(block.toString(), bot.colorPalette.string), Component.text(bot.world.currentDimension, bot.colorPalette.string), ((TranslatableComponent)Component.translatable("commands.cb.info.click_to_teleport", (TextColor)NamedTextColor.GREEN).hoverEvent(HoverEvent.showText(Component.text(command, bot.colorPalette.secondary)))).clickEvent(ClickEvent.runCommand(command)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void runCommand(Bot bot, CommandContext context, String command, PlayerEntry player) {
        Matcher userMatcher = USER_PATTERN.matcher(command);
        Matcher uuidMatcher = UUID_PATTERN.matcher(command);
        boolean userFound = userMatcher.find();
        boolean uuidFound = uuidMatcher.find();
        if (userFound || uuidFound) {
            Pattern pattern = userFound ? Pattern.compile(userMatcher.group(1)) : Pattern.compile(uuidMatcher.group(1));
            List<PlayerEntry> list = bot.players.list;
            synchronized (list) {
                for (PlayerEntry entry : bot.players.list) {
                    String replacedCommand;
                    String username = entry.profile.getName();
                    String uuid = entry.profile.getIdAsString();
                    if (!pattern.matcher(userFound ? username : uuid).matches() || (replacedCommand = (replacedCommand = userFound ? new StringBuilder(command).replace(userMatcher.start(), userMatcher.end(), username).toString() : new StringBuilder(command).replace(uuidMatcher.start(), uuidMatcher.end(), uuid).toString()).replace("{username}", username).replace("{uuid}", uuid)).contains("{username}") || replacedCommand.contains("{uuid}") || USER_PATTERN.matcher(username).find() || UUID_PATTERN.matcher(username).find()) continue;
                    this.runCommand(bot, context, replacedCommand, entry);
                }
            }
        } else if (command.contains("{username}") || command.contains("{uuid}")) {
            List<PlayerEntry> pattern = bot.players.list;
            synchronized (pattern) {
                for (PlayerEntry entry : bot.players.list) {
                    String username = entry.profile.getName();
                    String uuid = entry.profile.getIdAsString();
                    String replacedCommand = command.replace("{username}", username).replace("{uuid}", uuid);
                    if (replacedCommand.contains("{username}") || replacedCommand.contains("{uuid}") || USER_PATTERN.matcher(username).find() || UUID_PATTERN.matcher(username).find()) continue;
                    this.runCommand(bot, context, replacedCommand, entry);
                }
            }
        } else {
            CompletableFuture<Component> future = bot.core.runTracked(command);
            if (future == null) {
                return;
            }
            future.thenApply(output -> {
                if (player == null) {
                    context.sendOutput((Component)output);
                } else {
                    Component component = Component.translatable("[%s] %s", ((TextComponent)((TextComponent)((TextComponent)Component.text(player.profile.getName()).color(NamedTextColor.GRAY)).hoverEvent(HoverEvent.showText(((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.text(player.profile.getName()).append(Component.newline())).append(Component.text(player.profile.getIdAsString()).color(bot.colorPalette.uuid))).append(Component.newline())).append(Component.translatable("commands.generic.click_to_copy_username", (TextColor)NamedTextColor.GREEN))).append(Component.newline())).append(Component.translatable("commands.generic.shift_click_to_insert_uuid", (TextColor)NamedTextColor.GREEN))))).clickEvent(ClickEvent.copyToClipboard(player.profile.getName()))).insertion(player.profile.getIdAsString()), ((TextComponent)Component.empty().append((Component)output)).color(NamedTextColor.WHITE)).color(NamedTextColor.DARK_GRAY);
                    context.sendOutput(component);
                }
                return output;
            });
        }
    }
}

