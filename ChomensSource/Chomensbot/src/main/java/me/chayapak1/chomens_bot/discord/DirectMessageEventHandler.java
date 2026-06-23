/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.discord;

import me.chayapak1.chomens_bot.Configuration;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.logging.LogType;
import me.chayapak1.chomens_bot.util.HashingUtilities;
import me.chayapak1.chomens_bot.util.I18nUtilities;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class DirectMessageEventHandler
extends ListenerAdapter {
    private static final String HASH_MESSAGE = "hash";
    private static final String KEY_MESSAGE = "key";
    private static final String FORCED_KEY_MESSAGE = "key force";
    private final JDA jda;
    private final Configuration.Discord options;

    public DirectMessageEventHandler(JDA jda, Configuration.Discord options) {
        this.jda = jda;
        this.options = options;
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Guild guild;
        if (!this.options.enableDiscordHashing || event.getChannelType() != ChannelType.PRIVATE) {
            return;
        }
        Message message = event.getMessage();
        if (!(message.getContentDisplay().equalsIgnoreCase(HASH_MESSAGE) || message.getContentDisplay().equalsIgnoreCase(KEY_MESSAGE) || message.getContentDisplay().equalsIgnoreCase(FORCED_KEY_MESSAGE))) {
            return;
        }
        try {
            guild = this.jda.getGuildById(this.options.serverId);
        }
        catch (NumberFormatException e) {
            LoggerUtilities.error(e);
            return;
        }
        if (guild == null) {
            return;
        }
        guild.retrieveMember(message.getAuthor()).queue(member -> {
            if (member == null) {
                return;
            }
            TrustLevel trustLevel = TrustLevel.fromDiscordRoles(member.getRoles());
            if (trustLevel == TrustLevel.PUBLIC) {
                LoggerUtilities.log(LogType.DISCORD, (Component)Component.translatable(I18nUtilities.get("hashing.discord_direct_message.error.no_roles.log"), Component.text(member.toString())));
                message.reply(I18nUtilities.get("hashing.discord_direct_message.error.no_roles")).queue();
                return;
            }
            switch (message.getContentDisplay().toLowerCase()) {
                case "hash": {
                    this.sendHash(trustLevel, message, (Member)member);
                    break;
                }
                case "key": {
                    this.sendKey(trustLevel, message, (Member)member, false);
                    break;
                }
                case "key force": {
                    this.sendKey(trustLevel, message, (Member)member, true);
                }
            }
        }, exception -> {
            if (!(exception instanceof ErrorResponseException)) {
                return;
            }
            ErrorResponseException error = (ErrorResponseException)exception;
            ErrorResponse errorResponse = error.getErrorResponse();
            if (errorResponse == ErrorResponse.UNKNOWN_MEMBER) {
                message.reply(String.format(I18nUtilities.get("hashing.discord_direct_message.error.not_in_guild"), guild.getName())).queue();
            } else if (errorResponse == ErrorResponse.UNKNOWN_USER) {
                LoggerUtilities.error(Component.translatable(I18nUtilities.get("hashing.discord_direct_message.error.log_unknown_user"), Component.text(message.getAuthor().toString())));
            }
        });
    }

    private void sendHash(TrustLevel trustLevel, Message message, Member member) {
        String result = HashingUtilities.generateDiscordHash(member.getIdLong(), trustLevel);
        message.reply(String.format(I18nUtilities.get("hashing.discord_direct_message.hash_generated"), new Object[]{trustLevel, result})).queue();
        LoggerUtilities.log(LogType.DISCORD, (Component)Component.translatable(I18nUtilities.get("hashing.discord_direct_message.hash_generated.log"), Component.text(result), Component.text(trustLevel.toString()), Component.text(member.getEffectiveName())));
    }

    private void sendKey(TrustLevel trustLevel, Message message, Member member, boolean force) {
        try {
            String generatedKey = HashingUtilities.KEY_MANAGER.generate(trustLevel, member.getId(), force, String.format(I18nUtilities.get("hashing.discord_direct_message.error.key_for_trust_level_already_exists"), new Object[]{trustLevel, FORCED_KEY_MESSAGE}));
            message.reply(String.format(I18nUtilities.get("hashing.discord_direct_message.key_generated"), new Object[]{trustLevel, generatedKey})).queue();
        }
        catch (IllegalStateException e) {
            message.reply(e.getMessage()).queue();
        }
    }
}

