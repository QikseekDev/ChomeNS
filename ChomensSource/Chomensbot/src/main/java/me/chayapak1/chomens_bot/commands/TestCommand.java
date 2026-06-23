/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class TestCommand
extends Command {
    public TestCommand() {
        super("test", new String[]{"[args]"}, new String[0], TrustLevel.PUBLIC);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        return Component.translatable("commands.test.output", (TextColor)NamedTextColor.GREEN, Component.text(context.sender.profile.getName()), Component.text(context.sender.profile.getIdAsString()), Component.text(context.prefix), Component.text(context.getString(true, false)));
    }
}

