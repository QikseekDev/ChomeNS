/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.lua54.Lua54;
import party.iroiro.luajava.value.LuaValue;

public class ServerEvalCommand
extends Command {
    public Lua lua;

    public ServerEvalCommand() {
        super("servereval", new String[]{"reset", "<code>"}, new String[0], TrustLevel.OWNER);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        String code = context.getString(true, true);
        if (code.equalsIgnoreCase("reset")) {
            if (this.lua != null) {
                this.lua.close();
            }
            this.lua = null;
            return Component.translatable("commands.servereval.reset", bot.colorPalette.defaultColor);
        }
        bot.executorService.execute(() -> {
            try {
                if (this.lua == null) {
                    this.lua = new Lua54();
                }
                this.lua.openLibraries();
                this.lua.set("lua", this.lua);
                this.lua.set("bot", bot);
                this.lua.set("context", context);
                this.lua.set("shell", new Shell());
                LuaValue[] values2 = this.lua.eval(code);
                StringBuilder output = new StringBuilder();
                if (values2.length != 1) {
                    output.append('[');
                    int i = 1;
                    for (LuaValue value : values2) {
                        output.append(this.getString(value));
                        if (i++ == values2.length) continue;
                        output.append(", ");
                    }
                    output.append(']');
                } else {
                    output.append(this.getString(values2[0]));
                }
                context.sendOutput(Component.text(output.toString(), (TextColor)NamedTextColor.GREEN));
            }
            catch (Exception e) {
                context.sendOutput(Component.text(e.toString(), (TextColor)NamedTextColor.RED));
            }
        });
        return null;
    }

    private String getString(LuaValue luaValue) {
        Object javaObject = luaValue.toJavaObject();
        if (javaObject == null) {
            return luaValue.toString();
        }
        return javaObject.toString();
    }

    public static final class Shell {
        public String execute(String[] command) throws Exception {
            int character;
            ProcessBuilder processBuilder = new ProcessBuilder(new String[0]);
            processBuilder.command(command);
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((character = stdoutReader.read()) != -1) {
                char[] chars = Character.toChars(character);
                String string = new String(chars);
                output.append(string);
            }
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((character = stderrReader.read()) != -1) {
                char[] chars = Character.toChars(character);
                String string = new String(chars);
                output.append("[STDERR] ").append(string);
            }
            process.waitFor(10L, TimeUnit.SECONDS);
            return output.toString();
        }
    }
}

