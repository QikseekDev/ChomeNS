/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.command.contexts.PlayerCommandContext;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.MathUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class RandomTeleportCommand
extends Command {
    public RandomTeleportCommand() {
        super("rtp", new String[0], new String[]{"randomteleport"}, TrustLevel.PUBLIC, false, new ChatPacketType[]{ChatPacketType.DISGUISED});
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        context.checkOverloadArgs(0);
        Bot bot = context.bot;
        PlayerEntry sender = context.sender;
        String position = String.format("%d %d %d", MathUtilities.between(-1000000, 1000000), 100, MathUtilities.between(-1000000, 1000000));
        bot.core.run(String.format("essentials:teleport %s %s", sender.profile.getIdAsString(), position));
        if (context instanceof PlayerCommandContext) {
            PlayerCommandContext playerContext = (PlayerCommandContext)context;
            playerContext.sendOutput(Component.translatable("commands.rtp.teleporting_no_location", bot.colorPalette.defaultColor, Component.text(sender.profile.getName(), bot.colorPalette.username)));
            playerContext.sendOutput(Component.translatable("commands.rtp.to_sender", Style.style().color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC).build(), Component.text(position, (TextColor)NamedTextColor.GREEN)), true);
            return null;
        }
        return Component.translatable("commands.rtp.teleporting_location", bot.colorPalette.defaultColor, Component.text(sender.profile.getName(), bot.colorPalette.username), Component.text(position, (TextColor)NamedTextColor.GREEN));
    }
}

