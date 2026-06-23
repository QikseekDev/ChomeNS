/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.command.contexts.ConsoleCommandContext;
import me.chayapak1.chomens_bot.command.contexts.DiscordCommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class ValidateCommand
extends Command {
    public ValidateCommand() {
        super("validate", new String[]{""}, new String[]{"checkhash"}, TrustLevel.TRUSTED);
    }

    @Override
    public Component execute(CommandContext context) {
        Component trustLevelComponent = context.trustLevel.component.append(Component.text(" - ")).append(Component.text(context.trustLevel.level));
        if (context instanceof DiscordCommandContext) {
            return Component.translatable("commands.validate.discord", (TextColor)NamedTextColor.GREEN, trustLevelComponent);
        }
        if (context instanceof ConsoleCommandContext) {
            return Component.translatable("commands.validate.console", (TextColor)NamedTextColor.GREEN);
        }
        return Component.translatable(context.sender.persistingData.authenticatedTrustLevel != TrustLevel.PUBLIC ? "commands.validate.player_authenticated" : "commands.validate.player", (TextColor)NamedTextColor.GREEN, trustLevelComponent);
    }
}

