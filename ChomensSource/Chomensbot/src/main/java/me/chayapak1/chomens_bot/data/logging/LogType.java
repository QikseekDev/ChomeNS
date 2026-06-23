/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.logging;

import me.chayapak1.chomens_bot.util.I18nUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum LogType {
    INFO(Component.translatable(I18nUtilities.get("log_type.info"), (TextColor)NamedTextColor.GREEN)),
    CHAT(Component.translatable(I18nUtilities.get("log_type.chat"), (TextColor)NamedTextColor.GOLD)),
    TRUSTED_BROADCAST(Component.translatable(I18nUtilities.get("log_type.trusted_broadcast"), (TextColor)NamedTextColor.AQUA)),
    ERROR(Component.translatable(I18nUtilities.get("log_type.error"), (TextColor)NamedTextColor.RED)),
    COMMAND_OUTPUT(Component.translatable(I18nUtilities.get("log_type.command_output"), (TextColor)NamedTextColor.LIGHT_PURPLE)),
    AUTH(Component.translatable(I18nUtilities.get("log_type.auth"), (TextColor)NamedTextColor.RED)),
    SIMPLE_VOICE_CHAT(Component.translatable(I18nUtilities.get("log_type.simple_voice_chat"), (TextColor)NamedTextColor.AQUA)),
    DISCORD(Component.translatable(I18nUtilities.get("log_type.discord"), (TextColor)NamedTextColor.BLUE));

    public final Component component;

    private LogType(Component component) {
        this.component = component;
    }
}

