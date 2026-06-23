/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.chatParsers;

import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.chat.ChatParser;
import me.chayapak1.chomens_bot.data.chat.PlayerMessage;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.ComponentUtilities;
import me.chayapak1.chomens_bot.util.UUIDUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;

public class KaboomChatParser
implements ChatParser {
    private final Bot bot;

    public KaboomChatParser(Bot bot) {
        this.bot = bot;
    }

    @Override
    public PlayerMessage parse(Component message) {
        if (message instanceof TextComponent) {
            return this.parse((TextComponent)message);
        }
        return null;
    }

    public PlayerMessage parse(TextComponent message) {
        List<Component> children = message.children();
        if (!message.content().isEmpty() || !message.style().isEmpty() || children.size() < 3) {
            return null;
        }
        Component prefix = children.getFirst();
        Component displayName = Component.empty();
        Component contents = Component.empty();
        if (this.isSeperatorAt(children, 1)) {
            if (children.size() > 3) {
                contents = children.get(3);
            }
        } else if (this.isSeperatorAt(children, 2)) {
            displayName = children.get(1);
            if (children.size() > 4) {
                contents = children.get(4);
            }
        } else {
            return null;
        }
        String stringifiedDisplayName = ComponentUtilities.stringify(displayName);
        PlayerEntry sender = this.bot.players.getEntry(((TextComponent)Component.empty().append(prefix)).append(displayName));
        if (sender == null) {
            sender = this.bot.players.getEntry(prefix.append(displayName));
        }
        if (sender == null) {
            sender = new PlayerEntry(new GameProfile(UUIDUtilities.getOfflineUUID(stringifiedDisplayName), stringifiedDisplayName), GameMode.SURVIVAL, 0, displayName, 0L, null, new byte[0], true);
        }
        return new PlayerMessage(sender, displayName, contents);
    }

    private boolean isSeperatorAt(List<Component> children, int start) {
        return (children.get(start).equals(Component.text(":")) || children.get(start).equals(Component.text("\u00a7f:"))) && children.get(start + 1).equals(Component.space());
    }
}

