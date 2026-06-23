/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.discord;

import me.chayapak1.chomens_bot.data.logging.LogType;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public class MessageLogger
extends ListenerAdapter {
    private final JDA jda;

    public MessageLogger(JDA jda) {
        this.jda = jda;
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User author = event.getAuthor();
        if (author.getId().equals(this.jda.getSelfUser().getId())) {
            return;
        }
        Message message = event.getMessage();
        String source2 = event.isFromGuild() ? String.format("%s - %s (%s)", event.getGuild().getName(), event.getGuildChannel().getName(), event.getGuildChannel().getId()) : event.getChannelType().name();
        Component component = Component.translatable("[%s] %s \u203a %s", Component.text(source2, (TextColor)NamedTextColor.GRAY), Component.text(author.getName(), (TextColor)NamedTextColor.RED), Component.text(message.getContentRaw(), (TextColor)NamedTextColor.GRAY)).color(NamedTextColor.DARK_GRAY);
        LoggerUtilities.log(LogType.DISCORD, component);
    }
}

