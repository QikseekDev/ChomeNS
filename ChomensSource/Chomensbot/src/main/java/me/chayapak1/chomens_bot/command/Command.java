/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.command;

import java.util.Arrays;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import net.kyori.adventure.text.Component;

public abstract class Command {
    public final String name;
    public String[] usages;
    public final String[] aliases;
    public final TrustLevel trustLevel;
    public final boolean consoleOnly;
    public ChatPacketType[] disallowedPacketTypes;

    public Command(String name, String[] usages, String[] aliases, TrustLevel trustLevel) {
        this.name = name;
        this.usages = usages;
        this.aliases = aliases;
        this.trustLevel = trustLevel;
        this.consoleOnly = false;
    }

    public Command(String name, String[] usages, String[] aliases, TrustLevel trustLevel, boolean consoleOnly) {
        this.name = name;
        this.usages = usages;
        this.aliases = aliases;
        this.trustLevel = trustLevel;
        this.consoleOnly = consoleOnly;
    }

    public Command(String name, String[] usages, String[] aliases, TrustLevel trustLevel, boolean consoleOnly, ChatPacketType[] disallowedPacketTypes) {
        this.name = name;
        this.usages = usages;
        this.aliases = aliases;
        this.trustLevel = trustLevel;
        this.consoleOnly = consoleOnly;
        this.disallowedPacketTypes = disallowedPacketTypes;
    }

    public abstract Component execute(CommandContext var1) throws Exception;

    public String toString() {
        return "Command{name='" + this.name + "', usages=" + Arrays.toString(this.usages) + ", aliases=" + Arrays.toString(this.aliases) + ", trustLevel=" + String.valueOf((Object)this.trustLevel) + ", consoleOnly=" + this.consoleOnly + ", disallowedPacketTypes=" + Arrays.toString((Object[])this.disallowedPacketTypes) + "}";
    }
}

