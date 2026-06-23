/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class TranslateCommand
extends Command {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TRANSLATE_URL = "https://translate-pa.googleapis.com/v1/translate?params.client=gtx&dataTypes=TRANSLATION&key=AIzaSyDLEeFI5OtFBwYBIoK_jj5m32rZK5CkCXA&query.sourceLanguage=%s&query.targetLanguage=%s&query.text=%s";

    public TranslateCommand() {
        super("translate", new String[]{"<from> <to> <message>"}, new String[0], TrustLevel.PUBLIC, false, new ChatPacketType[]{ChatPacketType.SYSTEM, ChatPacketType.DISGUISED});
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        Bot bot = context.bot;
        String from = context.getString(false, true);
        String to = context.getString(false, true);
        String message = context.getString(true, true);
        bot.executorService.execute(() -> {
            try {
                URL url = new URI(String.format(TRANSLATE_URL, URLEncoder.encode(from, StandardCharsets.UTF_8), URLEncoder.encode(to, StandardCharsets.UTF_8), URLEncoder.encode(message, StandardCharsets.UTF_8))).toURL();
                Result result = OBJECT_MAPPER.readValue(url, Result.class);
                context.sendOutput(Component.translatable("commands.translate.result", bot.colorPalette.secondary, ((TextComponent)Component.text(result.translation(), (TextColor)NamedTextColor.GREEN).clickEvent(ClickEvent.copyToClipboard(result.translation()))).insertion(result.translation())));
            }
            catch (Exception e) {
                context.sendOutput(Component.text(e.toString(), (TextColor)NamedTextColor.RED));
            }
        });
        return null;
    }

    private record Result(@JsonValue String translation, @JsonValue String sourceLanguage, @JsonValue DetectedLanguages detectedLanguages) {

        private record DetectedLanguages(@JsonValue List<String> srclangs, @JsonValue List<String> extendedSrclangs) {
        }
    }
}

