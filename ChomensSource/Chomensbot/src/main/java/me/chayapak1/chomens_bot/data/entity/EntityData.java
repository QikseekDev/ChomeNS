/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.data.entity;

import me.chayapak1.chomens_bot.data.entity.Rotation;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import org.cloudburstmc.math.vector.Vector3d;

public class EntityData {
    public PlayerEntry player;
    public Vector3d position;
    public Rotation rotation;

    public EntityData(PlayerEntry player, Vector3d position, Rotation rotation) {
        this.player = player;
        this.position = position;
        this.rotation = rotation;
    }

    public String toString() {
        return "EntityData{player=" + String.valueOf(this.player) + ", position=" + String.valueOf(this.position) + ", rotation=" + String.valueOf(this.rotation) + "}";
    }
}

