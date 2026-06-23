/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.chat;

import me.chayapak1.chomens_bot.data.chat.PlayerMessage;
import net.kyori.adventure.text.Component;

public interface ChatParser {
    public PlayerMessage parse(Component var1);
}

