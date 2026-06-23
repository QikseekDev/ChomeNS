/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.song;

import net.kyori.adventure.text.Component;

public class SongLoaderException
extends Exception {
    public final Component message;

    public SongLoaderException(Component message) {
        this.message = message;
    }
}

