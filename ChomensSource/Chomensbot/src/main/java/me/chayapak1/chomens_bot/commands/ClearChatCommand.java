/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.util.UUID;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class ClearChatCommand
extends Command {
    public ClearChatCommand() {
        super("clearchat", new String[]{"[player]"}, new String[]{"cc"}, TrustLevel.PUBLIC, false, new ChatPacketType[]{ChatPacketType.DISGUISED});
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        String name = context.getString(true, false);
        if (!name.isEmpty()) {
            PlayerEntry entry = bot.players.getEntry(name);
            if (entry == null) {
                throw new CommandException(Component.translatable("commands.generic.error.invalid_player"));
            }
            UUID uuid = entry.profile.getId();
            bot.chat.tellraw(I18nUtilities.render(((TextComponent)Component.empty().append(Component.text("\n".repeat(1000)))).append(Component.translatable("commands.clearchat.specific", (TextColor)NamedTextColor.DARK_GREEN, context.displayName()))), uuid);
        } else {
            bot.chat.tellraw(I18nUtilities.render(((TextComponent)Component.empty().append(Component.text("\n".repeat(1000)))).append(Component.translatable("commands.clearchat.everyone", (TextColor)NamedTextColor.DARK_GREEN))));
        }
        return null;
    }
}

