/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.command;

import java.util.List;
import java.util.Map;
import me.chayapak1.chomens_bot.Configuration;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import net.dv8tion.jda.api.entities.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum TrustLevel {
    PUBLIC(0, Component.text(I18nUtilities.get("trust_level.public"), (TextColor)NamedTextColor.GREEN)),
    TRUSTED(1, Component.text(I18nUtilities.get("trust_level.trusted"), (TextColor)NamedTextColor.RED)),
    ADMIN(2, Component.text(I18nUtilities.get("trust_level.admin"), (TextColor)NamedTextColor.DARK_RED)),
    OWNER(3, Component.text(I18nUtilities.get("trust_level.owner"), (TextColor)NamedTextColor.LIGHT_PURPLE));

    public static final TrustLevel MAX;
    public final int level;
    public final Component component;

    private TrustLevel(int level, Component component) {
        this.level = level;
        this.component = component;
    }

    public static TrustLevel fromDiscordRoles(List<Role> roles) {
        if (Main.discord == null || Main.discord.options == null) {
            return PUBLIC;
        }
        Configuration.Discord options = Main.discord.options;
        Map<String, TrustLevel> roleToLevel = Map.of(options.ownerRoleName.toLowerCase(), OWNER, options.adminRoleName.toLowerCase(), ADMIN, options.trustedRoleName.toLowerCase(), TRUSTED);
        for (Role role : roles) {
            TrustLevel level = roleToLevel.get(role.getName().toLowerCase());
            if (level == null) continue;
            return level;
        }
        return PUBLIC;
    }

    static {
        MAX = TrustLevel.values()[TrustLevel.values().length - 1];
    }
}

