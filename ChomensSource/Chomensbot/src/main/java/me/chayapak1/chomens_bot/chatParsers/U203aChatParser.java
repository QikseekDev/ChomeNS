/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.chatParsers;

import java.util.List;
import java.util.UUID;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.chat.ChatParser;
import me.chayapak1.chomens_bot.data.chat.PlayerMessage;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.ComponentUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.HoverEvent;

public class U203aChatParser
implements ChatParser {
    private final Bot bot;

    public U203aChatParser(Bot bot) {
        this.bot = bot;
    }

    @Override
    public PlayerMessage parse(Component message) {
        if (message instanceof TranslatableComponent) {
            return this.parse((TranslatableComponent)message);
        }
        return null;
    }

    public PlayerMessage parse(TranslatableComponent message) {
        PlayerEntry sender;
        List<TranslationArgument> args2 = message.arguments();
        if (args2.size() < 3 || !message.key().equals("[%s] %s \u203a %s") && !message.key().equals("%s %s \u203a %s")) {
            return null;
        }
        Component senderComponent = args2.get(1).asComponent();
        Component contents = args2.get(2).asComponent();
        HoverEvent<?> hoverEvent = senderComponent.hoverEvent();
        if (hoverEvent != null && hoverEvent.action().equals(HoverEvent.Action.SHOW_ENTITY)) {
            HoverEvent.ShowEntity entityInfo = (HoverEvent.ShowEntity)hoverEvent.value();
            UUID senderUUID = entityInfo.id();
            sender = this.bot.players.getEntry(senderUUID);
        } else {
            String stringUsername = ComponentUtilities.stringify(senderComponent);
            sender = this.bot.players.getEntry(stringUsername);
        }
        if (sender == null) {
            return null;
        }
        return new PlayerMessage(sender, senderComponent, contents);
    }
}

