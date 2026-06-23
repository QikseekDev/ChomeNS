/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.selfCare;

import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;

public class SelfData {
    public final int entityId;
    public GameMode gameMode;
    public int permissionLevel;

    public SelfData(int entityId) {
        this.entityId = entityId;
    }
}

