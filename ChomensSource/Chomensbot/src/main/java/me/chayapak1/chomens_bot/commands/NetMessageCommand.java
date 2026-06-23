/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.util.ChatMessageUtilities;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class NetMessageCommand
extends Command {
    public NetMessageCommand() {
        super("netmsg", new String[]{"<message>"}, new String[]{"networkmessage", "irc"}, TrustLevel.PUBLIC, false, new ChatPacketType[]{ChatPacketType.SYSTEM, ChatPacketType.DISGUISED});
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        List<Bot> bots = bot.bots;
        String originServerName = bot.getServerString();
        String originServerAddress = bot.getServerString(false);
        TextComponent.Builder serverNameComponent = (TextComponent.Builder)Component.text().content(originServerName).color(NamedTextColor.GRAY);
        if (!bot.options.hidden) {
            ((TextComponent.Builder)serverNameComponent.clickEvent(ClickEvent.copyToClipboard(originServerAddress))).hoverEvent(HoverEvent.showText(((TextComponent)((TextComponent)Component.empty().append(Component.text(originServerAddress, (TextColor)NamedTextColor.GRAY))).append(Component.newline())).append(Component.translatable("commands.netmsg.hover.copy_server_to_clipboard", (TextColor)NamedTextColor.GREEN))));
        }
        String rawMessage = context.getString(true, true);
        Component stylizedMessage = ChatMessageUtilities.applyChatMessageStyling(rawMessage);
        TranslatableComponent component = Component.translatable("[%s]%s%s%s\u203a %s", (TextColor)NamedTextColor.DARK_GRAY, new ComponentLike[]{serverNameComponent, Component.space(), context.sender.displayName == null ? Component.text(context.sender.profile.getName(), (TextColor)NamedTextColor.GRAY) : context.sender.displayName.colorIfAbsent(NamedTextColor.GRAY), Component.space(), ((TextComponent)((TextComponent)((TextComponent)Component.empty().append(stylizedMessage)).color(NamedTextColor.GRAY)).clickEvent(ClickEvent.copyToClipboard(rawMessage))).hoverEvent(HoverEvent.showText(Component.translatable("commands.generic.click_to_copy_message", (TextColor)NamedTextColor.GREEN)))});
        for (Bot eachBot : bots) {
            eachBot.chat.tellraw(I18nUtilities.render(component));
        }
        return null;
    }
}

