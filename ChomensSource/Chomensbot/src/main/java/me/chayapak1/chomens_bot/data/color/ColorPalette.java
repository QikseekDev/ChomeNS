/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.color;

import me.chayapak1.chomens_bot.Configuration;
import me.chayapak1.chomens_bot.util.ColorUtilities;
import net.kyori.adventure.text.format.TextColor;

public class ColorPalette {
    public final TextColor primary;
    public final TextColor secondary;
    public final TextColor defaultColor;
    public final TextColor username;
    public final TextColor uuid;
    public final TextColor string;
    public final TextColor number;
    public final TextColor ownerName;

    public ColorPalette(Configuration.ColorPalette configColorPalette) {
        this.primary = ColorUtilities.getColorByString(configColorPalette.primary);
        this.secondary = ColorUtilities.getColorByString(configColorPalette.secondary);
        this.defaultColor = ColorUtilities.getColorByString(configColorPalette.defaultColor);
        this.username = ColorUtilities.getColorByString(configColorPalette.username);
        this.uuid = ColorUtilities.getColorByString(configColorPalette.uuid);
        this.string = ColorUtilities.getColorByString(configColorPalette.string);
        this.number = ColorUtilities.getColorByString(configColorPalette.number);
        this.ownerName = ColorUtilities.getColorByString(configColorPalette.ownerName);
    }
}

