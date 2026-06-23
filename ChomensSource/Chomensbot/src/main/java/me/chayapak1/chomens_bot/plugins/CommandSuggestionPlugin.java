/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.util.ArrayList;
import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.plugins.CommandHandlerPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;

public class CommandSuggestionPlugin
implements Listener {
    private final Bot bot;
    private final String id;

    public CommandSuggestionPlugin(Bot bot) {
        this.bot = bot;
        this.id = bot.config.namespace + "_request_command_suggestion";
        bot.listener.addListener(this);
    }

    @Override
    public boolean onSystemMessageReceived(Component component, ChatPacketType packetType, String string, String ansi) {
        Component component2;
        TextComponent idComponent;
        List<Component> children = component.children();
        if (!(packetType == ChatPacketType.SYSTEM && component instanceof TextComponent && (idComponent = (TextComponent)component).content().equals(this.id) && children.size() == 1 && (component2 = children.getFirst()) instanceof TextComponent)) {
            return true;
        }
        TextComponent playerComponent = (TextComponent)component2;
        String player = playerComponent.content();
        ArrayList<Object> output = new ArrayList<Object>();
        output.add(Component.text(this.id));
        for (Command command : CommandHandlerPlugin.COMMANDS) {
            if (command.consoleOnly) continue;
            boolean hasAliases = command.aliases.length != 0;
            TextComponent.Builder outputComponent = (TextComponent.Builder)((TextComponent.Builder)Component.text().content(command.name).append((Component)Component.text(command.trustLevel.name()))).append((Component)Component.text(hasAliases));
            if (hasAliases) {
                for (String alias : command.aliases) {
                    outputComponent.append((Component)Component.text(alias));
                }
            }
            output.add(outputComponent.build());
        }
        this.bot.chat.tellraw(Component.join(JoinConfiguration.noSeparators(), output), player);
        return false;
    }
}

