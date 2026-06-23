/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.team;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.geysermc.mcprotocollib.protocol.data.game.scoreboard.CollisionRule;
import org.geysermc.mcprotocollib.protocol.data.game.scoreboard.NameTagVisibility;
import org.geysermc.mcprotocollib.protocol.data.game.scoreboard.TeamColor;

public class Team {
    public String teamName;
    public final List<String> players;
    public Component displayName;
    public boolean friendlyFire;
    public boolean seeFriendlyInvisibles;
    public NameTagVisibility nametagVisibility;
    public CollisionRule collisionRule;
    public TeamColor color;
    public Component prefix;
    public Component suffix;

    public Team(String teamName, List<String> players, Component displayName, boolean friendlyFire, boolean seeFriendlyInvisibles, NameTagVisibility nametagVisibility, CollisionRule collisionRule, TeamColor color, Component prefix, Component suffix) {
        this.teamName = teamName;
        this.players = players;
        this.displayName = displayName;
        this.friendlyFire = friendlyFire;
        this.seeFriendlyInvisibles = seeFriendlyInvisibles;
        this.nametagVisibility = nametagVisibility;
        this.collisionRule = collisionRule;
        this.color = color;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public Style colorToStyle() {
        return switch (this.color) {
            default -> throw new MatchException(null, null);
            case TeamColor.BLACK -> Style.style(NamedTextColor.BLACK);
            case TeamColor.DARK_BLUE -> Style.style(NamedTextColor.DARK_BLUE);
            case TeamColor.DARK_GREEN -> Style.style(NamedTextColor.DARK_GREEN);
            case TeamColor.DARK_AQUA -> Style.style(NamedTextColor.DARK_AQUA);
            case TeamColor.DARK_RED -> Style.style(NamedTextColor.DARK_RED);
            case TeamColor.DARK_PURPLE -> Style.style(NamedTextColor.DARK_PURPLE);
            case TeamColor.GOLD -> Style.style(NamedTextColor.GOLD);
            case TeamColor.GRAY -> Style.style(NamedTextColor.GRAY);
            case TeamColor.DARK_GRAY -> Style.style(NamedTextColor.DARK_GRAY);
            case TeamColor.BLUE -> Style.style(NamedTextColor.BLUE);
            case TeamColor.GREEN -> Style.style(NamedTextColor.GREEN);
            case TeamColor.AQUA -> Style.style(NamedTextColor.AQUA);
            case TeamColor.RED -> Style.style(NamedTextColor.RED);
            case TeamColor.LIGHT_PURPLE -> Style.style(NamedTextColor.LIGHT_PURPLE);
            case TeamColor.YELLOW -> Style.style(NamedTextColor.YELLOW);
            case TeamColor.WHITE -> Style.style(NamedTextColor.WHITE);
            case TeamColor.OBFUSCATED -> Style.style(TextDecoration.OBFUSCATED);
            case TeamColor.BOLD -> Style.style(TextDecoration.BOLD);
            case TeamColor.STRIKETHROUGH -> Style.style(TextDecoration.STRIKETHROUGH);
            case TeamColor.UNDERLINED -> Style.style(TextDecoration.UNDERLINED);
            case TeamColor.ITALIC -> Style.style(TextDecoration.ITALIC);
            case TeamColor.RESET -> Style.empty();
        };
    }

    public String toString() {
        return "Team{teamName='" + this.teamName + "', players=" + String.valueOf(this.players) + ", displayName=" + String.valueOf(this.displayName) + ", friendlyFire=" + this.friendlyFire + ", seeFriendlyInvisibles=" + this.seeFriendlyInvisibles + ", nametagVisibility=" + String.valueOf((Object)this.nametagVisibility) + ", collisionRule=" + String.valueOf((Object)this.collisionRule) + ", color=" + String.valueOf((Object)this.color) + ", prefix=" + String.valueOf(this.prefix) + ", suffix=" + String.valueOf(this.suffix) + "}";
    }
}

