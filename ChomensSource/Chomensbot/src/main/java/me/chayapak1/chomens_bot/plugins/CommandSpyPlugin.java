/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.ComponentUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandSpyPlugin
implements Listener {
    private final Bot bot;

    public CommandSpyPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    @Override
    public boolean onSystemMessageReceived(Component component, ChatPacketType packetType, String string, String ansi) {
        TextComponent textComponent;
        List<Component> children;
        block5: {
            block4: {
                children = component.children();
                if (packetType != ChatPacketType.SYSTEM || !(component instanceof TextComponent)) break block4;
                textComponent = (TextComponent)component;
                if (children.size() == 2 && !textComponent.style().isEmpty() && (textComponent.color() == NamedTextColor.AQUA || textComponent.color() == NamedTextColor.YELLOW) && children.getFirst() instanceof TextComponent && children.getLast() instanceof TextComponent) break block5;
            }
            return true;
        }
        String username = textComponent.content();
        String command = ComponentUtilities.stringify(children.getLast());
        PlayerEntry sender = this.bot.players.getEntry(username, false, false);
        if (sender == null) {
            return true;
        }
        this.bot.listener.dispatch(listener -> listener.onCommandSpyMessageReceived(sender, command));
        return true;
    }
}

