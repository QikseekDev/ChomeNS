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
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class ListCommand
extends Command {
    public ListCommand() {
        super("list", new String[0], new String[]{"players"}, TrustLevel.PUBLIC);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        context.checkOverloadArgs(0);
        Bot bot = context.bot;
        List<PlayerEntry> list = bot.players.list;
        ArrayList<TranslatableComponent> playersComponent = new ArrayList<TranslatableComponent>();
        for (PlayerEntry entry : list) {
            if (entry == null) continue;
            Component hoverEvent = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.text(entry.profile.getName()).append(Component.newline())).append(Component.text(entry.profile.getIdAsString(), bot.colorPalette.uuid))).append(Component.newline())).append(Component.newline())).append(entry.persistingData.usernames.isEmpty() ? Component.translatable("commands.list.no_other_usernames", (TextColor)NamedTextColor.GRAY) : Component.translatable("commands.list.with_usernames", bot.colorPalette.secondary, Component.join(JoinConfiguration.commas(true), entry.persistingData.usernames.stream().map(Component::text).toList()).color(bot.colorPalette.string)))).append(Component.newline())).append(Component.translatable("commands.list.vanished", bot.colorPalette.secondary, Component.text(!entry.persistingData.listed, bot.colorPalette.string)))).append(Component.newline())).append(Component.translatable("commands.list.latency", bot.colorPalette.secondary, Component.text(entry.latency, bot.colorPalette.string).append(Component.text("ms"))))).append(Component.newline())).append(Component.translatable("commands.list.game_mode", bot.colorPalette.secondary, Component.text(entry.gamemode.name(), bot.colorPalette.string)))).append(Component.newline())).append(Component.translatable("commands.list.ip_address", bot.colorPalette.secondary, Component.text(entry.persistingData.ip == null ? "N/A" : entry.persistingData.ip, bot.colorPalette.string)))).append(Component.newline())).append(Component.newline())).append(Component.translatable("commands.generic.click_to_copy_username", (TextColor)NamedTextColor.GREEN))).append(Component.newline())).append(Component.translatable("commands.generic.shift_click_to_insert_uuid", (TextColor)NamedTextColor.GREEN));
            Component component = context.inGame ? Component.translatable("%s", entry.displayName == null ? Component.text(entry.profile.getName()) : entry.displayName) : Component.translatable("%s (%s - %s - %s)", (TextColor)NamedTextColor.DARK_GRAY, entry.displayName == null ? Component.text(entry.profile.getName(), (TextColor)NamedTextColor.WHITE) : entry.displayName.colorIfAbsent(NamedTextColor.WHITE), Component.text(entry.profile.getName(), (TextColor)NamedTextColor.WHITE), Component.text(entry.profile.getIdAsString(), bot.colorPalette.uuid), Component.text(entry.persistingData.ip == null ? "N/A" : entry.persistingData.ip, bot.colorPalette.string));
            component = component.hoverEvent(HoverEvent.showText(hoverEvent)).clickEvent(ClickEvent.copyToClipboard(entry.profile.getName())).insertion(entry.profile.getIdAsString());
            playersComponent.add((TranslatableComponent)component);
        }
        return ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().append(Component.translatable("commands.list.players_text", (TextColor)NamedTextColor.GREEN))).append(Component.text("(", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.text(list.size(), (TextColor)NamedTextColor.GRAY))).append(Component.text(")", (TextColor)NamedTextColor.DARK_GRAY))).append(Component.newline())).append(Component.join(JoinConfiguration.newlines(), playersComponent));
    }
}

