/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatMessageUtilities {
    private static final LegacyComponentSerializer SERIALIZER = ((LegacyComponentSerializer.Builder)LegacyComponentSerializer.legacySection().toBuilder()).extractUrls(Pattern.compile("((https?://(ww(w|\\d)\\.)?|ww(w|\\d))[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9]{1,63}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*))"), Style.style(NamedTextColor.BLUE, TextDecoration.UNDERLINED, HoverEvent.showText(Component.text("Click here to open the URL").color(NamedTextColor.BLUE)))).useUnusualXRepeatedCharacterHexFormat().build();

    public static Component applyChatMessageStyling(String message) {
        return SERIALIZER.deserialize(message);
    }
}

