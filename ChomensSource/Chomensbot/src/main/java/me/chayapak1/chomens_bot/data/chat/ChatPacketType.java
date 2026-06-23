/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.chat;

public enum ChatPacketType {
    PLAYER("P"),
    DISGUISED("D"),
    SYSTEM("S");

    public final String shortName;

    private ChatPacketType(String shortName) {
        this.shortName = shortName;
    }
}

