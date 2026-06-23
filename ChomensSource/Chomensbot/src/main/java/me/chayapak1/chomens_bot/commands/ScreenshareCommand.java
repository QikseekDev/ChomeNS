/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.cloudburstmc.math.vector.Vector3i;

public class ScreenshareCommand
extends Command {
    public ScreenshareCommand() {
        super("screenshare", new String[]{"start <x> <y> <z>", "stop", "setres <width> <height>", "setfps <fps>"}, new String[0], TrustLevel.TRUSTED);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        String action = context.getString(false, true, true);
        try {
            switch (action) {
                case "start": {
                    context.checkOverloadArgs(4);
                    int x = context.getInteger(true);
                    int y = context.getInteger(true);
                    int z = context.getInteger(true);
                    bot.screenshare.start(Vector3i.from(x, y, z));
                    return Component.text("Started screen sharing").color(bot.colorPalette.defaultColor);
                }
                case "stop": {
                    context.checkOverloadArgs(1);
                    bot.screenshare.stop();
                    return Component.text("Stopped screen sharing").color(bot.colorPalette.defaultColor);
                }
                case "setres": {
                    context.checkOverloadArgs(3);
                    int width = context.getInteger(true);
                    int height = context.getInteger(true);
                    bot.screenshare.setScreenSize(width, height);
                    return ((TextComponent)Component.text("Set the resolution to ").append(Component.text(width + "x" + height, bot.colorPalette.string))).color(bot.colorPalette.defaultColor);
                }
                case "setfps": {
                    context.checkOverloadArgs(2);
                    int fps = context.getInteger(true);
                    bot.screenshare.setFPS(fps);
                    return ((TextComponent)Component.text("Set the FPS to ").append(Component.text(fps, bot.colorPalette.number))).color(bot.colorPalette.defaultColor);
                }
            }
            throw new CommandException(Component.translatable("commands.generic.error.invalid_action"));
        }
        catch (NumberFormatException e) {
            throw new CommandException(Component.text("Invalid integer"));
        }
    }
}

