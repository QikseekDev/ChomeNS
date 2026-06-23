/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.UUIDUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class UUIDCommand
extends Command {
    public UUIDCommand() {
        super("uuid", new String[]{"[username]"}, new String[0], TrustLevel.PUBLIC);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        String player = context.getString(true, false);
        if (!player.isEmpty()) {
            String uuid;
            String name;
            PlayerEntry entry = bot.players.getEntry(player);
            if (entry == null) {
                name = player;
                uuid = UUIDUtilities.getOfflineUUID(player).toString();
            } else {
                name = entry.profile.getName();
                uuid = entry.profile.getIdAsString();
            }
            return Component.translatable("commands.uuid.other", (TextColor)NamedTextColor.GREEN, Component.text(name), ((TextComponent)Component.text(uuid, bot.colorPalette.uuid).hoverEvent(HoverEvent.showText(Component.translatable("commands.generic.click_to_copy_uuid", (TextColor)NamedTextColor.GREEN)))).clickEvent(ClickEvent.copyToClipboard(uuid)));
        }
        PlayerEntry entry = context.sender;
        String uuid = entry.profile.getIdAsString();
        return Component.translatable("commands.uuid.self", (TextColor)NamedTextColor.GREEN, ((TextComponent)Component.text(uuid, bot.colorPalette.uuid).hoverEvent(HoverEvent.showText(Component.translatable("commands.generic.click_to_copy_uuid", (TextColor)NamedTextColor.GREEN)))).clickEvent(ClickEvent.copyToClipboard(uuid)));
    }
}

