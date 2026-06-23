/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import com.github.ricksbrown.cowsay.plugin.CowExecutor;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import net.kyori.adventure.text.Component;

public class CowsayCommand
extends Command {
    public CowsayCommand() {
        super("cowsay", new String[]{"<message>"}, new String[0], TrustLevel.PUBLIC);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        String message = context.getString(true, true);
        CowExecutor cowExecutor = new CowExecutor();
        cowExecutor.setMessage(message);
        String result = cowExecutor.execute();
        return Component.text(result);
    }
}

