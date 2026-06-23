/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.util.concurrent.CompletableFuture;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.eval.EvalOutput;
import me.chayapak1.chomens_bot.plugins.EvalPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class EvalCommand
extends Command {
    public EvalCommand() {
        super("eval", new String[]{"run <code>", "reset"}, new String[0], TrustLevel.PUBLIC, false, new ChatPacketType[]{ChatPacketType.DISGUISED});
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        String action;
        Bot bot = context.bot;
        if (!EvalPlugin.connected) {
            throw new CommandException(Component.translatable("commands.eval.error.offline"));
        }
        switch (action = context.getAction()) {
            case "run": {
                String command = context.getString(true, true);
                CompletableFuture<EvalOutput> future = bot.eval.run(command);
                if (future == null) {
                    return null;
                }
                future.thenApply(result -> {
                    if (result.isError()) {
                        context.sendOutput(Component.text(result.output(), (TextColor)NamedTextColor.RED));
                    } else {
                        context.sendOutput(Component.text(result.output()));
                    }
                    return result;
                });
                break;
            }
            case "reset": {
                EvalPlugin.reset();
                return Component.translatable("commands.eval.reset", bot.colorPalette.defaultColor);
            }
            default: {
                throw new CommandException(Component.translatable("commands.generic.error.invalid_action"));
            }
        }
        return null;
    }
}

